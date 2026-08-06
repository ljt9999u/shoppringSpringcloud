package org.example.Controller;

import org.example.Feign.OrderFeign;
import org.example.Feign.UserFeign;
import org.example.Product.Product;
import org.example.Service.ProductService;
import org.example.User.UserPOJO;
import org.example.common.PageResult;
import org.example.common.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 商品控制器 - 商品的增删改查 + 跨服务调用示例
 */
@RestController
@RequestMapping("/api/product")
public class ProductContoller {

    @Autowired
    private ProductService productService;

    @Autowired
    private UserFeign userFeign;

    @Autowired
    private OrderFeign orderFeign;

    /**
     * 根据ID查询商品
     * GET /api/product/{id}
     */
    @GetMapping("/{id}")
    public Result<Product> getById(@PathVariable("id") Long id) {
        Product product = productService.getProduct(id);
        if (product == null) {
            return Result.fail("商品不存在");
        }
        return Result.success(product);
    }

    /**
     * 分页查询上架商品
     * GET /api/product/page?pageNum=1&pageSize=10
     */
    @GetMapping("/page")
    public Result<PageResult<Product>> page(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        return Result.success(productService.listPageOnShelf(pageNum, pageSize));
    }

    /**
     * 分页模糊搜索商品
     * GET /api/product/search?keyword=手机&pageNum=1&pageSize=10
     */
    @GetMapping("/search")
    public Result<PageResult<Product>> search(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        return Result.success(productService.searchPageByName(keyword, pageNum, pageSize));
    }

    /**
     * 按分类分页查询商品
     * GET /api/product/category?categoryId=1&pageNum=1&pageSize=10
     */
    @GetMapping("/category")
    public Result<PageResult<Product>> category(
            @RequestParam Long categoryId,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        return Result.success(productService.listPageByCategory(categoryId, pageNum, pageSize));
    }

    /**
     * 新增商品
     * POST /api/product/add
     */
    @PostMapping("/add")
    public Result<Product> add(@RequestBody Product product) {
        int rows = productService.addProduct(product);
        if (rows <= 0) {
            return Result.fail("新增商品失败");
        }
        return Result.success(product);
    }

    /**
     * 更新商品
     * PUT /api/product/update
     */
    @PutMapping("/update")
    public Result update(@RequestBody Product product) {
        int rows = productService.updateProduct(product);
        if (rows <= 0) {
            return Result.fail("更新商品失败");
        }
        return Result.success();
    }

    /**
     * 下架商品（逻辑删除）
     * DELETE /api/product/{id}
     */
    @DeleteMapping("/{id}")
    public Result delete(@PathVariable Long id) {
        int rows = productService.deleteProduct(id);
        if (rows <= 0) {
            return Result.fail("下架商品失败");
        }
        return Result.success();
    }

    /**
     * 扣减库存（给订单服务调用）
     * POST /api/product/reduceStock?id=1&quantity=2
     */
    @PostMapping("/reduceStock")
    public Result<Boolean> reduceStock(@RequestParam Long id, @RequestParam int quantity) {
        boolean success = productService.reduceStock(id, quantity);
        if (!success) {
            return Result.fail("库存不足");
        }
        return Result.success(true);
    }

    // ========== 跨服务调用示例 ==========

    /**
     * 查询商品详情 + 商家信息（商品服务调用用户服务）
     * GET /api/product/detail/{id}
     */
    @GetMapping("/detail/{id}")
    public Result<Map<String, Object>> getProductDetail(@PathVariable Long id) {
        Product product = productService.getProduct(id);
        if (product == null) {
            return Result.fail("商品不存在");
        }

        Map<String, Object> result = new HashMap<>();
        result.put("product", product);
        // 销量直接从商品表获取，不需要跨服务调用
        result.put("sales", product.getSales());

        // 跨服务调用：通过 Feign 调用用户服务获取商家信息
        if (product.getMerchantId() != null) {
            Result<UserPOJO> userResult = userFeign.getUserById(product.getMerchantId());
            if (userResult.getCode() == 200 && userResult.getData() != null) {
                UserPOJO merchant = userResult.getData();
                merchant.setPassword(null); // 脱敏
                result.put("merchant", merchant);
            } else {
                result.put("merchantError", userResult.getMessage());
            }
        }

        return Result.success(result);
    }

    /**
     * 查询商品 + 订单数 + 评价数（商品服务调用订单服务）
     * GET /api/product/withStats/{id}
     */
    @GetMapping("/withStats/{id}")
    public Result<Map<String, Object>> getProductWithStats(@PathVariable Long id) {
        Product product = productService.getProduct(id);
        if (product == null) {
            return Result.fail("商品不存在");
        }

        Map<String, Object> result = new HashMap<>();
        result.put("product", product);
        // 销量直接从商品表获取
        result.put("sales", product.getSales());

        // 跨服务调用订单服务获取订单数和评价数（商品表中没有这些数据）
        Result<Integer> orderCountResult = orderFeign.getOrderCountByProduct(id);
        Result<Integer> commentResult = orderFeign.getCommentCount(id);

        result.put("orderCount", orderCountResult.getCode() == 200 ? orderCountResult.getData() : 0);
        result.put("commentCount", commentResult.getCode() == 200 ? commentResult.getData() : 0);

        return Result.success(result);
    }

    /**
     * 测试所有服务是否可用
     * GET /api/product/crossTest
     */
    @GetMapping("/crossTest")
    public Result<Map<String, String>> crossServiceTest() {
        Map<String, String> results = new HashMap<>();
        results.put("productService", "OK");

        // 测试用户服务
        try {
            Result<UserPOJO> userResult = userFeign.getUserById(1L);
            results.put("userService", userResult.getCode() == 200 ? "OK" : "FAIL: " + userResult.getMessage());
        } catch (Exception e) {
            results.put("userService", "ERROR: " + e.getMessage());
        }

        // 测试订单服务
        try {
            Result<Integer> orderResult = orderFeign.getOrderCountByProduct(1L);
            results.put("orderService", orderResult.getCode() == 200 ? "OK" : "FAIL: " + orderResult.getMessage());
        } catch (Exception e) {
            results.put("orderService", "ERROR: " + e.getMessage());
        }

        return Result.success(results);
    }

    /**
     * 健康检查
     * GET /api/product/health
     */
    @GetMapping("/health")
    public String health() {
        return "OK - services-product1 is running";
    }
}
