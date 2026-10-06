package org.example.Controller;

import org.example.Product.ProductComment;
import org.example.Product.ProductCommentVO;
import org.example.Service.ProductCommentService;
import org.example.common.PageResult;
import org.example.common.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 商品评价控制器
 * 网关前缀 /api/product/**，GET 浏览类接口免登录；
 * 发表评价需登录，用户ID由网关注入的 X-User-Id 请求头透传
 */
@RestController
@RequestMapping("/api/product/comment")
public class ProductCommentController {

    @Autowired
    private ProductCommentService productCommentService;

    /**
     * 分页查询某商品的评价
     * GET /api/product/comment/list/{productId}?pageNum=1&pageSize=5
     */
    @GetMapping("/list/{productId}")
    public Result<PageResult<ProductCommentVO>> list(
            @PathVariable Long productId,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "5") int pageSize) {
        return Result.success(productCommentService.listPage(productId, pageNum, pageSize));
    }

    /**
     * 商家：分页查询本店所有商品的评价（含隐藏评价）
     * GET /api/product/comment/merchant/{merchantId}?pageNum=1&pageSize=10
     */
    @GetMapping("/merchant/{merchantId}")
    public Result<PageResult<ProductCommentVO>> listByMerchant(
            @PathVariable Long merchantId,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        return Result.success(productCommentService.listPageByMerchant(merchantId, pageNum, pageSize));
    }

    /**
     * 商品评价汇总（平均分、总数、好评率、星级分布、带图数）
     * GET /api/product/comment/summary/{productId}
     */
    @GetMapping("/summary/{productId}")
    public Result<Map<String, Object>> summary(@PathVariable Long productId) {
        return Result.success(productCommentService.summary(productId));
    }

    /**
     * 发表评价（必须购买后评价）
     * POST /api/product/comment/add
     * Body: { productId, orderId, star, content, commentImg }
     */
    @PostMapping("/add")
    public Result<Boolean> add(
            @RequestBody ProductComment comment,
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        try {
            boolean success = productCommentService.add(comment, userId);
            return success ? Result.success(true) : Result.fail("评价失败");
        } catch (IllegalArgumentException e) {
            return Result.fail(e.getMessage());
        }
    }

    /**
     * 商家回复评价
     * PUT /api/product/comment/reply?id=1&merchantReply=xxx
     */
    @PutMapping("/reply")
    public Result<Boolean> reply(@RequestParam Long id, @RequestParam String merchantReply) {
        try {
            boolean success = productCommentService.reply(id, merchantReply);
            return success ? Result.success(true) : Result.fail("回复失败，评价不存在");
        } catch (IllegalArgumentException e) {
            return Result.fail(e.getMessage());
        }
    }
}
