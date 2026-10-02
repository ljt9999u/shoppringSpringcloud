# 微服务接口文档

## 一、服务总览

| 服务名 | Nacos 注册名 | 端口 | 网关前缀 | 职责说明 |
|--------|-------------|------|----------|----------|
| 网关 gateway | gateway | 800 | - | API 网关，统一入口、路由转发、跨域 |
| 用户服务 | services-user | 8006 | /api/user | 登录、注册、用户查询、JWT 鉴权 |
| 商品服务 | services-product1 | 8804 | /api/product | 商品 CRUD、分类搜索、库存扣减、跨服务调用 |
| 订单服务 | services-oride | 8004 | /api/oride | 下单、支付、发货、收货、退款、物流、评价 |
| 商家服务 | services-merchant | 8008 | /api/merchant | 商家入驻、审核、收货地址管理 |
| 购物车服务 | services-cart | 8010 | /api/cart | 加购、数量修改、勾选、结算、删除 |
| 公共模块 model | - | - | - | 实体类、工具类、统一响应、RestTemplate 配置（被各服务依赖） |

### 网关路由规则

所有外部请求统一访问网关 `http://网关IP:800`，由网关按 Path 转发到对应微服务：

| 网关路径 | 转发到 |
|----------|--------|
| /api/product/** | lb://services-product1 |
| /api/oride/** | lb://services-oride |
| /api/user/** | lb://services-user |
| /api/merchant/** | lb://services-merchant |
| /api/cart/** | lb://services-cart |

> 直接访问微服务端口也可（如 http://localhost:8006/api/user/health），生产环境建议走网关。

### 统一响应格式

所有业务接口返回 `Result<T>` 结构：

```json
{
  "code": 200,
  "message": "成功",
  "data": { }
}
```

- `code`：200 成功，500 失败，401 未登录
- `message`：提示信息
- `data`：业务数据（可为空）

### 鉴权说明（网关统一校验）

除白名单外，所有接口必须在请求头携带 JWT：

```
Authorization: Bearer <登录返回的 token>
```

- 网关校验通过后，会把 `X-User-Id`、`X-Username`、`X-Role-Code` 透传给下游服务
- 缺失/过期 token 由网关直接返回 `401`，不会转发到微服务
- **白名单（无需登录）**：
  - 所有 `/health`
  - `POST /api/user/login`、`POST /api/user/register`
  - `GET` 方式的 `/api/product/**`（商品浏览、图片、规格）
  - `GET /api/category/**`、`GET /api/brand/**`（分类、品牌浏览）
  - `GET /api/merchant/**`（店铺浏览）
  - `GET /api/oride/comment/**`（评价浏览）
  - `POST /api/oride/pay/alipay/notify`（支付宝服务器异步回调，验签保证安全）
- 购物车、下单、支付、资料修改、增删改类接口均需登录

分页接口的 `data` 为 `PageResult<T>`：

```json
{
  "code": 200,
  "message": "成功",
  "data": {
    "total": 100,
    "pageNum": 1,
    "pageSize": 10,
    "pages": 10,
    "list": [ ]
  }
}
```

---

## 高并发防护说明（缓存三防护 + 服务熔断）

为避免高并发下数据库被打挂、服务级联雪崩，项目对**热点读接口**和**跨服务调用**做了如下防护。防护代码集中在商品服务，熔断覆盖全部跨服务调用。

### 1. 防护总览（哪个模块用了什么方法）

| 模块 | 接口/场景 | 预防的问题 | 使用的方法 |
|------|-----------|-----------|-----------|
| services-product1 | `GET /api/product/{id}` 商品详情 | 缓存穿透 + 缓存击穿 + 缓存雪崩 | 空值哨兵 + setnx 互斥锁 + TTL 随机抖动 |
| services-product1 | `GET /api/category/tree` 分类树 | 缓存雪崩 | Redis 缓存 + TTL 随机抖动 |
| services-product1 | `GET /api/brand/list` 品牌列表 | 缓存雪崩 | Redis 缓存 + TTL 随机抖动 |
| services-product1 | 商品/分类/品牌 增删改、扣库存 | 缓存脏数据 | 写库成功后删除对应缓存（Cache Aside） |
| services-oride | Feign 调商品/用户服务 | 下游故障级联、线程拖垮 | Sentinel 熔断 + Fallback 降级 |
| services-cart | Feign 调商品服务 | 同上 | Sentinel 熔断 + Fallback 降级 |
| services-merchant | Feign 调用户服务 | 同上 | Sentinel 熔断 + Fallback 降级 |
| services-product1 | Feign 调订单服务 | 同上 | Sentinel 熔断 + Fallback 降级 |

> 商品分页/搜索（条件组合多、命中率低）**未做缓存**，避免浪费内存且命中率低。

### 2. 三类缓存问题与防护原理

| 问题 | 含义 | 本项目防护手段 |
|------|------|---------------|
| **缓存穿透** | 大量请求查询数据库中**根本不存在**的数据（如恶意刷 id=999999），缓存不命中全部压到 DB | DB 查不到时写入**空值哨兵**（`__NULL_CACHE__`），短 TTL 60 秒，期间相同请求直接返回 null，不查库 |
| **缓存击穿** | 某个**热点 key 过期的瞬间**，大量并发同时回源查 DB | **setnx 互斥锁**（`SET NX EX 10`）：只放 1 个请求查库重建缓存，其余自旋等待 + 双重检查；锁带 10 秒过期防死锁 |
| **缓存雪崩** | 大量 key **同一时刻集中过期**，瞬间全压 DB | 正常数据 TTL = 基准值 **+ 0~N 秒随机抖动**，把过期时间打散 |

### 3. 各缓存接口的 Key 与过期策略

| 接口 | 缓存 Key | 基准 TTL | 随机抖动 | 空值 TTL |
|------|----------|----------|----------|----------|
| 商品详情 | `product:detail:{id}` | 30 分钟 | + 0~5 分钟 | 60 秒 |
| 分类树 | `category:tree` | 60 分钟 | + 0~10 分钟 | 60 秒 |
| 品牌列表 | `brand:list` | 60 分钟 | + 0~10 分钟 | 60 秒 |

- 互斥锁 Key 形如 `lock:product:detail:{id}`，自动过期 10 秒
- 缓存由 [RedisCacheService.java](file:///d:/weifuwu/demo2/services/services-product1/src/main/java/org/example/cache/RedisCacheService.java) 统一实现，Key 常量见 [CacheKeys.java](file:///d:/weifuwu/demo2/services/services-product1/src/main/java/org/example/cache/CacheKeys.java)
- **容灾**：Redis 本身宕机时，所有缓存操作自动降级为直查数据库，不影响主业务可用性
- 依赖：商品服务引入 `spring-boot-starter-data-redis`，连接 `127.0.0.1:6379`（Lettuce）

### 4. 服务熔断与降级

四个服务的 `application.yaml` 均已开启：

```yaml
feign:
  sentinel:
    enabled: true
```

> ⚠️ 注意：各服务的 `@FeignClient(fallback=...)` 降级类此前虽已存在，但该开关默认关闭，熔断不生效；现已全部开启。

开启后下游服务故障/超时时，调用方走 Fallback 降级而非连环崩溃：

| 调用方 | 被调服务 | 降级类 | 降级行为 |
|--------|---------|--------|----------|
| services-oride | services-product1 | ProductFllback | 返回"商品服务不可用"占位商品，扣库存返回 false，下单安全拦截（不超卖） |
| services-oride | services-user | UserFeignFallback | 返回"用户服务暂时不可用"，下单前校验失败则终止 |
| services-cart | services-product1 | ProductFeignFallback | 返回"商品服务暂时不可用" |
| services-product1 | services-oride | OrderFeignFallback | 商品销量/订单数查询降级，不影响商品主流程 |

- 订单服务还配置了 Feign 超时（默认连接/读取 2 秒，调商品服务读取 5 秒），慢调用不会长期占用线程
- 如需按"异常比例 / 慢调用比例 / QPS"精细熔断，需启动 Sentinel Dashboard（`localhost:8080`）配置规则，当前为"抛异常/超时即降级"

---

## 二、用户服务 services-user

**端口**：8006 ｜ **网关前缀**：/api/user ｜ **启动类**：ApplicationUser

### 1. 健康检查

```
GET /api/user/health
```

返回字符串 `OK - services-user is running`。

### 2. 用户注册

```
POST /api/user/register
Content-Type: application/json

Body (UserPOJO):
{
  "username": "张三",
  "password": "123456",
  "phone": "13800138000",
  "roleCode": "USER"
}
```

响应：`Result<RegisterVO>`

### 3. 用户登录

```
POST /api/user/login
Content-Type: application/json

Body:
{
  "phone": "13800138000",
  "password": "123456"
}
```

响应：`Result<LoginVO>`（含 JWT token）

### 4. 根据 ID 查询用户

> 供其他服务 Feign 调用，返回数据已脱敏（password 置空）。

```
GET /api/user/{id}
```

| 参数 | 位置 | 类型 | 说明 |
|------|------|------|------|
| id | path | Long | 用户 ID |

响应：`Result<UserPOJO>`

### 5. 根据手机号查询用户

```
GET /api/user/findByPhone?phone=13800138000
```

| 参数 | 位置 | 类型 | 说明 |
|------|------|------|------|
| phone | query | String | 手机号 |

响应：`Result<UserPOJO>`

### 6. 获取当前登录用户信息

```
GET /api/user/info
Header: Authorization: Bearer <token>
```

从 JWT token 解析用户信息。响应：`Result<Map<String, Object>>`

### 7. 修改个人资料

```
PUT /api/user/update
Header: Authorization: Bearer <token>
Content-Type: application/json

Body:
{
  "nickname": "新昵称",
  "email": "a@b.com",
  "avatar": "http://.../a.png",
  "gender": 1
}
```

用户 ID 由网关注入，无需也无法在 body 指定（防越权）。响应：`Result<Boolean>`

### 8. 修改密码

```
PUT /api/user/password?oldPassword=123456&newPassword=654321
Header: Authorization: Bearer <token>
```

需校验旧密码，新密码至少 6 位。响应：`Result<Boolean>`

### 9. 分页查询用户（管理端）

```
GET /api/user/page?pageNum=1&pageSize=10
```

响应：`Result<PageResult<UserPOJO>>`（password 已脱敏）

### 10. 修改账号状态（管理端）

```
PUT /api/user/status/{id}?status=0
```

status：0 禁用 1 启用。响应：`Result<Boolean>`

---

## 三、商品服务 services-product1

**端口**：8804 ｜ **网关前缀**：/api/product ｜ **启动类**：ProductApplication

### 1. 根据 ID 查询商品

```
GET /api/product/{id}
```

| 参数 | 位置 | 类型 | 说明 |
|------|------|------|------|
| id | path | Long | 商品 ID |

响应：`Result<Product>`

> 🛡️ **高并发防护**：本接口为热点读，已做缓存三防护（详见文档开头"高并发防护说明"）——空值哨兵防穿透、setnx 互斥锁防击穿、30 分钟基准 TTL + 随机抖动防雪崩；缓存 Key `product:detail:{id}`，Redis 故障自动降级直查 DB。

### 2. 分页查询上架商品

```
GET /api/product/page?pageNum=1&pageSize=10
```

| 参数 | 位置 | 类型 | 默认值 | 说明 |
|------|------|------|--------|------|
| pageNum | query | int | 1 | 页码 |
| pageSize | query | int | 10 | 每页条数 |

响应：`Result<PageResult<Product>>`

### 3. 模糊搜索商品

```
GET /api/product/search?keyword=手机&pageNum=1&pageSize=10
```

| 参数 | 位置 | 类型 | 说明 |
|------|------|------|------|
| keyword | query | String | 搜索关键词 |
| pageNum | query | int | 页码 |
| pageSize | query | int | 每页条数 |

响应：`Result<PageResult<Product>>`

### 4. 按分类分页查询商品

```
GET /api/product/category?categoryId=1&pageNum=1&pageSize=10
```

| 参数 | 位置 | 类型 | 说明 |
|------|------|------|------|
| categoryId | query | Long | 分类 ID |
| pageNum | query | int | 页码 |
| pageSize | query | int | 每页条数 |

响应：`Result<PageResult<Product>>`

### 5. 新增商品

```
POST /api/product/add
Content-Type: application/json

Body (Product)
```

响应：`Result<Product>`

### 6. 更新商品

```
PUT /api/product/update
Content-Type: application/json

Body (Product)
```

响应：`Result`

### 7. 下架商品（逻辑删除）

```
DELETE /api/product/{id}
```

响应：`Result`

### 8. 扣减库存

> 供订单服务 Feign 调用。

```
POST /api/product/reduceStock?id=1&quantity=2
```

| 参数 | 位置 | 类型 | 说明 |
|------|------|------|------|
| id | query | Long | 商品 ID |
| quantity | query | int | 扣减数量 |

响应：`Result<Boolean>`，库存不足返回失败。

### 9. 商品详情（含商家信息）

> 跨服务调用：商品服务 → 用户服务（Feign）。

```
GET /api/product/detail/{id}
```

响应 `data` 结构：
```json
{
  "product": { },
  "sales": 100,
  "merchant": { }
}
```

### 10. 商品 + 订单数 + 评价数

> 跨服务调用：商品服务 → 订单服务（Feign）。

```
GET /api/product/withStats/{id}
```

响应 `data` 结构：
```json
{
  "product": { },
  "sales": 100,
  "orderCount": 5,
  "commentCount": 3
}
```

### 11. 跨服务连通性测试

```
GET /api/product/crossTest
```

返回各服务（product / user / order）的连通状态。响应：`Result<Map<String, String>>`

### 12. 健康检查

```
GET /api/product/health
```

### 13. 商品分类（/api/category）

> 独立前缀，网关路由 `/api/category/**` → 商品服务。GET 免登录，增删改需登录。
>
> 🛡️ **高并发防护**：`/tree` 分类树为读多写少热点，走 Redis 缓存防雪崩（基准 TTL 60 分钟 + 0~10 分钟随机抖动，Key `category:tree`）；分类的新增/修改/改状态/删除成功后会自动清除该缓存。

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /api/category/tree | 分类树（含 children 层级）🛡️Redis缓存 |
| GET | /api/category/list | 启用分类平铺列表 |
| GET | /api/category/listAll | 全部分类（含禁用，管理端） |
| GET | /api/category/children/{parentId} | 查询直接子分类 |
| GET | /api/category/{id} | 根据 ID 查询分类 |
| POST | /api/category/add | 新增分类（Body: Category） |
| PUT | /api/category/update | 更新分类（Body: Category） |
| PUT | /api/category/status/{id}?status= | 修改状态 0禁用 1启用 |
| DELETE | /api/category/{id} | 删除分类（有子分类时拒绝） |

### 14. 品牌（/api/brand）

> 网关路由 `/api/brand/**` → 商品服务。GET 免登录，增删改需登录。
>
> 🛡️ **高并发防护**：`/list` 全量品牌列表（下拉选择高频）走 Redis 缓存防雪崩（基准 TTL 60 分钟 + 0~10 分钟随机抖动，Key `brand:list`）；品牌的新增/修改/删除成功后会自动清除该缓存。

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /api/brand/list | 全部品牌 🛡️Redis缓存 |
| GET | /api/brand/page?pageNum=&pageSize= | 分页查询品牌 |
| GET | /api/brand/{id} | 根据 ID 查询品牌 |
| POST | /api/brand/add | 新增品牌（Body: Brand） |
| PUT | /api/brand/update | 更新品牌（Body: Brand） |
| DELETE | /api/brand/{id} | 删除品牌 |

### 15. 商品图片（/api/product/image）

> 商品详情轮播多图。GET 免登录，增删改需登录。

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /api/product/image/list/{productId} | 查询商品全部图片 |
| POST | /api/product/image/add | 新增单张（Body: ProductImage） |
| POST | /api/product/image/batch | 批量保存相册（Body: List） |
| PUT | /api/product/image/update | 更新图片 |
| DELETE | /api/product/image/{id} | 删除单张图片 |
| DELETE | /api/product/image/product/{productId} | 删除商品全部图片 |

### 16. 商品规格（/api/product/spec）

> 颜色/尺码等 SKU，含规格库存与加价。GET 免登录，增删改需登录。

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /api/product/spec/list/{productId} | 查询商品全部规格 |
| GET | /api/product/spec/{id} | 根据 ID 查询规格 |
| POST | /api/product/spec/add | 新增规格（Body: ProductSpec） |
| PUT | /api/product/spec/update | 更新规格 |
| DELETE | /api/product/spec/{id} | 删除规格 |
| POST | /api/product/spec/reduceStock?id=&quantity= | 扣减规格库存（防超卖） |

---

## 四、订单服务 services-oride

**端口**：8004 ｜ **网关前缀**：/api/oride ｜ **启动类**：OrideMainApilication

> 🛡️ **高并发防护（服务熔断）**：本服务通过 Feign 调用商品、用户服务，已开启 Sentinel 熔断（`feign.sentinel.enabled=true`）。下游故障/超时走 `ProductFllback`、`UserFeignFallback` 降级：商品不可用时扣库存返回 false 拦截下单（不超卖），用户不可用时终止下单。默认超时 2 秒（调商品读取 5 秒）。

> 订单服务集成 Sentinel 限流、Nacos 配置中心、OpenFeign 调用。

### 测试/配置接口

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /api/oride/health | 健康检查 |
| GET | /api/oride/config | 查看 Nacos 动态配置（timeout、auto-confirm） |
| GET | /api/oride/create?userId=&productId= | 创建订单测试（Sentinel 限流） |
| GET | /api/oride/seckill?userId=&productId= | 秒杀测试（含 fallback） |
| GET | /api/oride/redDB | 测试接口 |

### 供商品服务调用（Feign）

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /api/oride/count?productId= | 查询商品订单数 |
| GET | /api/oride/commentCount?productId= | 查询商品评价数 |

### 订单业务接口

#### 1. 创建订单（下单）

```
POST /api/oride/order/create
Content-Type: application/json
Body (OrderPOJO)
```

响应：`Result<OrderPOJO>`

#### 2. 根据 ID 查询订单

```
GET /api/oride/order/{id}
```

响应：`Result<OrderPOJO>`

#### 3. 根据订单号查询订单

```
GET /api/oride/order/no/{orderNo}
```

响应：`Result<OrderPOJO>`

#### 4. 分页查询用户订单

```
GET /api/oride/order/user/{userId}?pageNum=1&pageSize=10
```

响应：`Result<PageResult<OrderPOJO>>`

#### 5. 分页查询商家订单

```
GET /api/oride/order/merchant/{merchantId}?pageNum=1&pageSize=10
```

响应：`Result<PageResult<OrderPOJO>>`

#### 6. 取消订单

```
PUT /api/oride/order/cancel/{orderId}
```

响应：`Result<Boolean>`

#### 7. 支付订单

```
POST /api/oride/order/pay?orderId=1&payMethod=1&tradeNo=xxx
```

| 参数 | 类型 | 说明 |
|------|------|------|
| orderId | Long | 订单 ID |
| payMethod | int | 1微信 2支付宝 3余额 |
| tradeNo | String | 第三方交易号（可选） |

响应：`Result<Boolean>`

#### 8. 发货

```
POST /api/oride/order/ship?orderId=1&logisticsNo=SF123456&company=顺丰
```

响应：`Result<Boolean>`

#### 9. 确认收货

```
PUT /api/oride/order/receive/{orderId}
```

响应：`Result<Boolean>`

#### 10. 查询订单详情

```
GET /api/oride/order/detail/{orderId}
```

响应：`Result<List<OrderDetail>>`

### 支付宝支付（沙箱）

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /api/oride/pay/alipay/{orderNo} | 生成支付宝收银台 HTML 表单（需登录） |
| POST | /api/oride/pay/alipay/notify | 支付宝异步回调（支付宝服务器调用，白名单放行） |

#### 1. 生成支付表单

```
GET /api/oride/pay/alipay/{orderNo}
```

**路径参数**：

| 参数 | 类型 | 说明 |
|------|------|------|
| orderNo | String | 订单号（下单接口返回），订单必须为「待付款」状态 |

**响应**：`Result<String>`，`data` 为支付宝收银台 HTML 表单

```json
{
  "code": 200,
  "message": "成功",
  "data": "<form name=\"punchout_form\" method=\"post\" action=\"https://openapi-sandbox.dl.alipaydev.com/gateway.do\">...</form><script>document.forms[0].submit();</script>"
}
```

**前端用法**：将 `data` 写入页面（如 `document.write(form)` 或赋值给 div 的 `innerHTML` 后执行 script），浏览器自动提交表单跳转到支付宝沙箱收银台。

**失败场景**：

```json
{ "code": 500, "message": "订单不存在，orderNo=xxx", "data": null }
{ "code": 500, "message": "订单状态不允许支付，当前状态=1", "data": null }
```

#### 2. 异步回调（notify）

```
POST /api/oride/pay/alipay/notify
Content-Type: application/x-www-form-urlencoded
```

支付宝服务器在支付成功后调用，**无 JWT**（网关白名单放行），核心参数：

| 参数 | 说明 |
|------|------|
| out_trade_no | 商户订单号（= 系统 orderNo） |
| trade_no | 支付宝交易号（存入 payment.trade_no） |
| trade_status | 交易状态，仅 `TRADE_SUCCESS` / `TRADE_FINISHED` 触发更新 |
| total_amount | 实付金额（与订单 pay_amount 比对，不一致拒绝） |
| sign / sign_type | 签名，用支付宝公钥验签 |

**处理逻辑（4 层防护）**：

1. **验签**：使用支付宝公钥 RSA2 验签，防止伪造回调
2. **状态校验**：只处理 `TRADE_SUCCESS` / `TRADE_FINISHED`
3. **幂等**：支付记录已存在且 status=1（已支付）时直接返回 success
4. **金额校验**：回调金额与订单金额不一致则拒绝

**响应**：纯文本 `success` / `failure`。返回 `success` 后支付宝不再重发；返回 `failure` 支付宝会在 25 小时内多次重试。

**回调成功后的数据变更**：

- `orders.status`：0（待付款）→ 1（待发货），记录 `pay_time`
- `payment` 表：插入/更新支付记录，`pay_method=2`（支付宝），status=1（已支付）

#### 3. 沙箱配置说明（application.yaml）

```yaml
alipay:
  app-id: 9021000138629565                     # 沙箱应用 APPID
  merchant-private-key: xxx                    # 应用私钥（请求签名）
  alipay-public-key: xxx                       # 支付宝公钥（回调验签）
  gateway-url: https://openapi-sandbox.dl.alipaydev.com/gateway.do
  notify-url: http://你的公网地址/api/oride/pay/alipay/notify
  return-url: http://localhost:5173/pay/result # 支付完成前端跳转页
  sign-type: RSA2
```

- **notify-url 必须公网可访问**：本地开发用内网穿透（cpolar / natapp）将回调地址映射到公网
- 未配置公网回调时，可手动调用 `POST /api/oride/order/pay?orderId=&payMethod=2&tradeNo=` 模拟支付成功
- 沙箱买家账号在 [支付宝开放平台](https://open.alipay.com/develop/sandbox/app)「沙箱账号」中获取，付款不产生真实交易

#### 4. 完整支付流程

1. `POST /api/oride/order/create` 下单 → 返回 orderNo
2. `GET /api/oride/pay/alipay/{orderNo}` 获取收银台 HTML 表单
3. 前端输出表单 → 自动跳转支付宝沙箱收银台
4. 沙箱买家账号登录付款
5. 支付宝异步 POST 回调 → 验签 → 订单置为「待发货」→ 记录支付记录
6. 前端可通过 `GET /api/oride/payment/{orderNo}` 轮询支付结果

### 支付查询

```
GET /api/oride/payment/{orderNo}
```

响应：`Result<Payment>`

### 退款接口

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | /api/oride/refund/apply | 申请退款（Body: Refund） |
| GET | /api/oride/refund/{orderNo} | 查询退款记录 |
| PUT | /api/oride/refund/handle/{refundId}?status= | 处理退款：1已同意 2已拒绝 3已退款 |

### 物流接口

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /api/oride/logistics/{orderNo} | 查询物流 |
| PUT | /api/oride/logistics/update?orderNo=&status= | 更新物流状态：0待发货 1已发货 2已签收 |

### 评价接口

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | /api/oride/comment/add | 添加评价（Body: Comment） |
| GET | /api/oride/comment/product/{productId}?pageNum=&pageSize= | 分页查询商品评价 |

---

## 五、商家服务 services-merchant

**端口**：8008 ｜ **网关前缀**：/api/merchant ｜ **启动类**：MerchantApplication

> 🛡️ **高并发防护（服务熔断）**：本服务通过 Feign 调用用户服务，已开启 Sentinel 熔断（`feign.sentinel.enabled=true`）。用户服务故障时走 `UserFeignFallback` 降级返回，不被下游拖垮。

### 健康检查

```
GET /api/merchant/health
```

### 商家业务接口

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | /api/merchant/apply | 商家入驻申请（Body: Merchant） |
| GET | /api/merchant/{id} | 根据 ID 查询商家 |
| GET | /api/merchant/user/{userId} | 根据用户 ID 查询商家 |
| GET | /api/merchant/page?pageNum=&pageSize= | 分页查询所有商家 |
| GET | /api/merchant/pageByStatus?status=&pageNum=&pageSize= | 按状态分页：0待审核 1已通过 2已拒绝 |
| PUT | /api/merchant/update | 更新商家信息（Body: Merchant） |
| PUT | /api/merchant/audit/{id}?status= | 审核商家：1通过 2拒绝 |
| DELETE | /api/merchant/{id} | 删除商家 |

### 收货地址接口

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | /api/merchant/address/add | 新增地址（Body: UserAddress） |
| GET | /api/merchant/address/{id} | 根据 ID 查询地址 |
| GET | /api/merchant/address/user/{userId} | 查询用户所有地址 |
| GET | /api/merchant/address/default/{userId} | 查询默认地址 |
| PUT | /api/merchant/address/update | 更新地址（Body: UserAddress） |
| PUT | /api/merchant/address/default?userId=&addressId= | 设置默认地址 |
| DELETE | /api/merchant/address/{id} | 删除地址 |

---

## 六、购物车服务 services-cart

**端口**：8010 ｜ **网关前缀**：/api/cart ｜ **启动类**：CartApplication

> 🛡️ **高并发防护（服务熔断）**：本服务通过 Feign 调用商品服务，已开启 Sentinel 熔断（`feign.sentinel.enabled=true`）。商品服务故障时走 `ProductFeignFallback` 降级返回"商品服务暂时不可用"，不被下游拖垮。

### 健康检查

```
GET /api/cart/health
```

### 购物车业务接口

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | /api/cart/add | 加入购物车（Body: Cart） |
| PUT | /api/cart/quantity/{id}?quantity=2 | 修改购物车项数量 |
| PUT | /api/cart/checked/{id}?checked=1 | 修改勾选状态：0取消 1勾选 |
| PUT | /api/cart/selectAll?userId=&checked= | 全选/取消全选 |
| GET | /api/cart/list/{userId} | 查询购物车列表（含汇总） |
| GET | /api/cart/checkout/{userId} | 查询勾选项（结算页用） |
| GET | /api/cart/count/{userId} | 统计购物车种类数（角标） |
| DELETE | /api/cart/{id} | 删除单个购物车项 |
| DELETE | /api/cart/batch | 批量删除（Body: [1,2,3]） |
| DELETE | /api/cart/clear/{userId} | 清空购物车 |
| DELETE | /api/cart/checked/{userId} | 下单后删除已勾选项 |

### 购物车列表/结算返回结构

`data` 为 Map：
```json
{
  "items": [ ],
  "totalAmount": 99.00,
  "totalCount": 5,
  "validCount": 4
}
```

- `items`：购物车项列表
- `totalAmount`：已勾选且有效项的总价
- `totalCount`：购物车总种类数
- `validCount`：有效商品种类数

---

## 七、服务间调用关系（Feign）

| 调用方 | 被调用方 | 用途 | Feign 接口 |
|--------|----------|------|-----------|
| 商品服务 | 用户服务 | 获取商家信息 | UserFeign.getUserById |
| 商品服务 | 订单服务 | 获取订单数、评价数 | OrderFeign.getOrderCountByProduct / getCommentCount |
| 订单服务 | 商品服务 | 获取商品信息、扣库存 | ProductFeign |
| 订单服务 | 用户服务 | 获取用户信息 | UserFeign |
| 购物车服务 | 商品服务 | 校验商品、获取价格 | ProductFeign |
| 购物车服务 | 用户服务 | 获取用户信息 | UserFeign |
| 商家服务 | 用户服务 | 获取用户信息 | UserFeign |

---

## 八、技术栈说明

- **注册中心**：Nacos（192.168.28.1:8848）
- **网关**：Spring Cloud Gateway（端口 800）
- **服务间调用**：OpenFeign
- **限流熔断**：Alibaba Sentinel（dashboard localhost:8080）
- **配置中心**：Nacos Config（订单服务使用）
- **数据库**：MySQL（shopping 库）
- **ORM**：MyBatis
- **鉴权**：JWT（用户服务生成 token）
