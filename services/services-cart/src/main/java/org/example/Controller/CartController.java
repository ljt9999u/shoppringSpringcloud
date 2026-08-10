package org.example.Controller;

import lombok.extern.slf4j.Slf4j;
import org.example.Cart.Cart;
import org.example.Service.CartService;
import org.example.common.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 购物车控制器 - 加购、查询、勾选、结算
 */
@RestController
@RequestMapping("/api/cart")
@Slf4j
@CrossOrigin
public class CartController {

    @Autowired
    private CartService cartService;

    /**
     * 健康检查
     * GET /api/cart/health
     */
    @GetMapping("/health")
    public String health() {
        return "OK - services-cart is running";
    }

    /**
     * 加入购物车
     * POST /api/cart/add
     */
    @PostMapping("/add")
    public Result<Cart> add(@RequestBody Cart cart) {
        try {
            Cart created = cartService.addToCart(cart);
            return Result.success(created);
        } catch (Exception e) {
            log.error("加入购物车失败", e);
            return Result.fail("加入购物车失败：" + e.getMessage());
        }
    }

    /**
     * 修改购物车项数量
     * PUT /api/cart/quantity/{id}?quantity=2
     */
    @PutMapping("/quantity/{id}")
    public Result<Boolean> updateQuantity(@PathVariable Long id, @RequestParam int quantity) {
        try {
            boolean success = cartService.updateQuantity(id, quantity);
            if (!success) {
                return Result.fail("修改数量失败，购物车项不存在或数量非法");
            }
            return Result.success(true);
        } catch (RuntimeException e) {
            return Result.fail(e.getMessage());
        }
    }

    /**
     * 修改单个购物车项勾选状态
     * PUT /api/cart/checked/{id}?checked=1
     * checked: 0取消勾选 1勾选
     */
    @PutMapping("/checked/{id}")
    public Result<Boolean> updateChecked(@PathVariable Long id, @RequestParam int checked) {
        boolean success = cartService.updateChecked(id, checked);
        if (!success) {
            return Result.fail("修改勾选状态失败");
        }
        return Result.success(true);
    }

    /**
     * 全选/取消全选
     * PUT /api/cart/selectAll?userId=1&checked=1
     */
    @PutMapping("/selectAll")
    public Result<Boolean> selectAll(@RequestParam Long userId, @RequestParam int checked) {
        boolean success = cartService.selectAll(userId, checked);
        if (!success) {
            return Result.fail("全选操作失败");
        }
        return Result.success(true);
    }

    /**
     * 查询用户购物车列表（含商品信息及失效标记）
     * GET /api/cart/list/{userId}
     * 返回：items 列表 + totalAmount(勾选有效项总价) + totalCount(种类数)
     */
    @GetMapping("/list/{userId}")
    public Result<Map<String, Object>> list(@PathVariable Long userId) {
        List<Cart> items = cartService.listByUserId(userId);
        return Result.success(buildCartSummary(items));
    }

    /**
     * 查询用户勾选的购物车项（结算页用）
     * GET /api/cart/checkout/{userId}
     */
    @GetMapping("/checkout/{userId}")
    public Result<Map<String, Object>> checkout(@PathVariable Long userId) {
        List<Cart> items = cartService.listCheckedByUserId(userId);
        return Result.success(buildCartSummary(items));
    }

    /**
     * 统计购物车商品种类数（购物车角标）
     * GET /api/cart/count/{userId}
     */
    @GetMapping("/count/{userId}")
    public Result<Integer> count(@PathVariable Long userId) {
        return Result.success(cartService.countByUserId(userId));
    }

    /**
     * 删除单个购物车项
     * DELETE /api/cart/{id}
     */
    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable Long id) {
        boolean success = cartService.deleteById(id);
        if (!success) {
            return Result.fail("删除失败");
        }
        return Result.success(true);
    }

    /**
     * 批量删除购物车项
     * DELETE /api/cart/batch
     * body: [1,2,3]
     */
    @DeleteMapping("/batch")
    public Result<Boolean> batchDelete(@RequestBody List<Long> ids) {
        boolean success = cartService.batchDelete(ids);
        if (!success) {
            return Result.fail("批量删除失败");
        }
        return Result.success(true);
    }

    /**
     * 清空用户购物车
     * DELETE /api/cart/clear/{userId}
     */
    @DeleteMapping("/clear/{userId}")
    public Result<Boolean> clear(@PathVariable Long userId) {
        boolean success = cartService.clearByUserId(userId);
        if (!success) {
            return Result.fail("清空购物车失败");
        }
        return Result.success(true);
    }

    /**
     * 下单成功后删除用户已勾选的购物车项
     * DELETE /api/cart/checked/{userId}
     */
    @DeleteMapping("/checked/{userId}")
    public Result<Boolean> deleteChecked(@PathVariable Long userId) {
        boolean success = cartService.deleteCheckedByUserId(userId);
        if (!success) {
            return Result.fail("清理已结算项失败");
        }
        return Result.success(true);
    }

    /**
     * 构建购物车汇总：列表 + 有效勾选总价 + 种类数
     */
    private Map<String, Object> buildCartSummary(List<Cart> items) {
        Map<String, Object> result = new HashMap<>();
        BigDecimal totalAmount = BigDecimal.ZERO;
        int validCount = 0;
        for (Cart cart : items) {
            boolean valid = cart.getValid() != null && cart.getValid()
                    && cart.getChecked() != null && cart.getChecked() == 1;
            if (valid) {
                totalAmount = totalAmount.add(cart.getSubtotal());
            }
            if (cart.getValid() != null && cart.getValid()) {
                validCount++;
            }
        }
        result.put("items", items);
        result.put("totalAmount", totalAmount);
        result.put("totalCount", items.size());
        result.put("validCount", validCount);
        return result;
    }
}
