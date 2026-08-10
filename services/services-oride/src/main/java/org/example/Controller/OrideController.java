package org.example.Controller;
import com.alibaba.csp.sentinel.annotation.SentinelResource;
import lombok.extern.slf4j.Slf4j;
import org.example.Oride.OridePOJO;
import org.example.Order.*;
import org.example.Service.Impl.OrideImpl;
import org.example.common.PageResult;
import org.example.common.Result;
import org.example.priperties.OrideProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;


@RequestMapping("/api/oride")
@Slf4j
@RefreshScope
@RestController
@CrossOrigin
public class OrideController {
    @Autowired
    OrideImpl orideImpl;

    @Autowired
    OrideProperties orideProperties;



    @GetMapping("/config")
    public String config(){
        return "oride.timeout: " + orideProperties.getTimeout() + ", oride.auto-confirm: " + orideProperties.getAutoConfirm();
    }

    // ========== 原有测试接口 ==========

    @SentinelResource(value = "createOride")
    @GetMapping("/create")
    public OridePOJO createOride(@RequestParam("userId") Long userId,
                                 @RequestParam("productId") Long productId){
        OridePOJO oride = orideImpl.createOride(1L,1L);
        return  oride;
    }

    @SentinelResource(value = "seckill",fallback = "seckillFllback")
    @GetMapping("/seckill")
    public OridePOJO seckill(@RequestParam("userId") Long userId,
                             @RequestParam("productId") Long productId){
        OridePOJO oride = orideImpl.createOride(1L,1L);
        oride.setId(Long.MAX_VALUE);
        return  oride;
    }

    public OridePOJO seckillFllback( Long userId, Long productId){
       OridePOJO oride = new OridePOJO();
        oride.setId(Long.MAX_VALUE);
        oride.setTotalAmount(new BigDecimal(0));
        oride.setUserId(userId);
        oride.setNickname("未知");
        oride.setAddress("异常信息");
        oride.setProductList(null);
        return  oride;
    }

    @GetMapping("/redDB")
    public String redDB(){
        log.info("redDB");
        return "redDB";
    }

    @GetMapping("/health")
    public String health(){
        return "OK - services-oride is running";
    }

    // ========== 供商品服务调用的接口 ==========

    /**
     * 查询商品订单数（供商品服务 Feign 调用）
     * GET /api/oride/count?productId=1
     */
    @GetMapping("/count")
    public Result<Integer> getOrderCount(@RequestParam("productId") Long productId) {
        int count = orideImpl.getOrderCountByProduct(productId);
        return Result.success(count);
    }

    /**
     * 查询商品评价数（供商品服务 Feign 调用）
     * GET /api/oride/commentCount?productId=1
     */
    @GetMapping("/commentCount")
    public Result<Integer> getCommentCount(@RequestParam("productId") Long productId) {
        int count = orideImpl.getCommentCount(productId);
        return Result.success(count);
    }

    // ========== 订单业务接口 ==========

    /**
     * 创建订单（下单）
     * POST /api/oride/order/create
     */
    @PostMapping("/order/create")
    public Result<OrderPOJO> createOrder(@RequestBody OrderPOJO order) {
        try {
            OrderPOJO created = orideImpl.createOrder(order);
            return Result.success(created);
        } catch (Exception e) {
            log.error("创建订单失败", e);
            return Result.fail("创建订单失败：" + e.getMessage());
        }
    }

    /**
     * 根据ID查询订单
     * GET /api/oride/order/{id}
     */
    @GetMapping("/order/{id}")
    public Result<OrderPOJO> getOrderById(@PathVariable Long id) {
        OrderPOJO order = orideImpl.getOrderById(id);
        if (order == null) {
            return Result.fail("订单不存在");
        }
        return Result.success(order);
    }

    /**
     * 根据订单号查询订单
     * GET /api/oride/order/no/{orderNo}
     */
    @GetMapping("/order/no/{orderNo}")
    public Result<OrderPOJO> getOrderByOrderNo(@PathVariable String orderNo) {
        OrderPOJO order = orideImpl.getOrderByOrderNo(orderNo);
        if (order == null) {
            return Result.fail("订单不存在");
        }
        return Result.success(order);
    }

    /**
     * 分页查询用户订单
     * GET /api/oride/order/user/{userId}?pageNum=1&pageSize=10
     */
    @GetMapping("/order/user/{userId}")
    public Result<PageResult<OrderPOJO>> getUserOrders(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        PageResult<OrderPOJO> page = orideImpl.getUserOrdersPage(userId, pageNum, pageSize);
        return Result.success(page);
    }

    /**
     * 分页查询商家订单
     * GET /api/oride/order/merchant/{merchantId}?pageNum=1&pageSize=10
     */
    @GetMapping("/order/merchant/{merchantId}")
    public Result<PageResult<OrderPOJO>> getMerchantOrders(
            @PathVariable Long merchantId,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        PageResult<OrderPOJO> page = orideImpl.getMerchantOrdersPage(merchantId, pageNum, pageSize);
        return Result.success(page);
    }

    /**
     * 取消订单
     * PUT /api/oride/order/cancel/{orderId}
     */
    @PutMapping("/order/cancel/{orderId}")
    public Result<Boolean> cancelOrder(@PathVariable Long orderId) {
        boolean success = orideImpl.cancelOrder(orderId);
        if (!success) {
            return Result.fail("取消订单失败，订单不存在或状态不允许取消");
        }
        return Result.success(true);
    }

    /**
     * 支付订单
     * POST /api/oride/order/pay?orderId=1&payMethod=1&tradeNo=xxx
     * payMethod: 1微信 2支付宝 3余额
     */
    @PostMapping("/order/pay")
    public Result<Boolean> payOrder(
            @RequestParam Long orderId,
            @RequestParam int payMethod,
            @RequestParam(required = false) String tradeNo) {
        boolean success = orideImpl.payOrder(orderId, payMethod, tradeNo);
        if (!success) {
            return Result.fail("支付失败，订单不存在或状态不允许支付");
        }
        return Result.success(true);
    }

    /**
     * 发货
     * POST /api/oride/order/ship?orderId=1&logisticsNo=SF123456&company=顺丰
     */
    @PostMapping("/order/ship")
    public Result<Boolean> shipOrder(
            @RequestParam Long orderId,
            @RequestParam String logisticsNo,
            @RequestParam String company) {
        boolean success = orideImpl.shipOrder(orderId, logisticsNo, company);
        if (!success) {
            return Result.fail("发货失败，订单不存在或状态不允许发货");
        }
        return Result.success(true);
    }

    /**
     * 确认收货
     * PUT /api/oride/order/receive/{orderId}
     */
    @PutMapping("/order/receive/{orderId}")
    public Result<Boolean> receiveOrder(@PathVariable Long orderId) {
        boolean success = orideImpl.receiveOrder(orderId);
        if (!success) {
            return Result.fail("收货失败，订单不存在或状态不允许收货");
        }
        return Result.success(true);
    }

    /**
     * 查询订单详情
     * GET /api/oride/order/detail/{orderId}
     */
    @GetMapping("/order/detail/{orderId}")
    public Result<List<OrderDetail>> getOrderDetails(@PathVariable Long orderId) {
        List<OrderDetail> details = orideImpl.getOrderDetails(orderId);
        return Result.success(details);
    }

    // ========== 支付查询 ==========

    /**
     * 查询支付记录
     * GET /api/oride/payment/{orderNo}
     */
    @GetMapping("/payment/{orderNo}")
    public Result<Payment> getPayment(@PathVariable String orderNo) {
        Payment payment = orideImpl.getPayment(orderNo);
        if (payment == null) {
            return Result.fail("支付记录不存在");
        }
        return Result.success(payment);
    }

    // ========== 退款 ==========

    /**
     * 申请退款
     * POST /api/oride/refund/apply
     */
    @PostMapping("/refund/apply")
    public Result<Boolean> applyRefund(@RequestBody Refund refund) {
        boolean success = orideImpl.applyRefund(refund);
        if (!success) {
            return Result.fail("退款申请失败，订单不存在");
        }
        return Result.success(true);
    }

    /**
     * 查询退款记录
     * GET /api/oride/refund/{orderNo}
     */
    @GetMapping("/refund/{orderNo}")
    public Result<Refund> getRefund(@PathVariable String orderNo) {
        Refund refund = orideImpl.getRefund(orderNo);
        if (refund == null) {
            return Result.fail("退款记录不存在");
        }
        return Result.success(refund);
    }

    /**
     * 处理退款申请（同意/拒绝）
     * PUT /api/oride/refund/handle/{refundId}?status=1
     * status: 1已同意 2已拒绝 3已退款
     */
    @PutMapping("/refund/handle/{refundId}")
    public Result<Boolean> handleRefund(
            @PathVariable Long refundId,
            @RequestParam int status) {
        boolean success = orideImpl.handleRefund(refundId, status);
        if (!success) {
            return Result.fail("处理退款失败");
        }
        return Result.success(true);
    }

    // ========== 物流 ==========

    /**
     * 查询物流
     * GET /api/oride/logistics/{orderNo}
     */
    @GetMapping("/logistics/{orderNo}")
    public Result<Logistics> getLogistics(@PathVariable String orderNo) {
        Logistics logistics = orideImpl.getLogistics(orderNo);
        if (logistics == null) {
            return Result.fail("物流信息不存在");
        }
        return Result.success(logistics);
    }

    /**
     * 更新物流状态
     * PUT /api/oride/logistics/update?orderNo=xxx&status=2
     * status: 0待发货 1已发货 2已签收
     */
    @PutMapping("/logistics/update")
    public Result<Boolean> updateLogisticsStatus(
            @RequestParam String orderNo,
            @RequestParam int status) {
        boolean success = orideImpl.updateLogisticsStatus(orderNo, status);
        if (!success) {
            return Result.fail("更新物流状态失败");
        }
        return Result.success(true);
    }

    // ========== 评价 ==========

    /**
     * 添加评价
     * POST /api/oride/comment/add
     */
    @PostMapping("/comment/add")
    public Result<Boolean> addComment(@RequestBody Comment comment) {
        boolean success = orideImpl.addComment(comment);
        if (!success) {
            return Result.fail("添加评价失败");
        }
        return Result.success(true);
    }

    /**
     * 分页查询商品评价
     * GET /api/oride/comment/product/{productId}?pageNum=1&pageSize=10
     */
    @GetMapping("/comment/product/{productId}")
    public Result<PageResult<Comment>> getProductComments(
            @PathVariable Long productId,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        PageResult<Comment> page = orideImpl.getProductCommentsPage(productId, pageNum, pageSize);
        return Result.success(page);
    }
}
