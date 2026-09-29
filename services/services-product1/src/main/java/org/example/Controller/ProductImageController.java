package org.example.Controller;

import org.example.Product.ProductImage;
import org.example.Service.ProductImageService;
import org.example.common.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 商品图片控制器（商品详情轮播多图）
 * 挂在 /api/product/image 下，走网关 /api/product/** 路由
 */
@RestController
@RequestMapping("/api/product/image")
@CrossOrigin
public class ProductImageController {

    @Autowired
    private ProductImageService productImageService;

    /**
     * 查询某商品的全部图片
     * GET /api/product/image/list/{productId}
     */
    @GetMapping("/list/{productId}")
    public Result<List<ProductImage>> list(@PathVariable Long productId) {
        return Result.success(productImageService.listByProductId(productId));
    }

    /**
     * 新增单张图片
     * POST /api/product/image/add
     */
    @PostMapping("/add")
    public Result<ProductImage> add(@RequestBody ProductImage image) {
        int rows = productImageService.add(image);
        if (rows <= 0) {
            return Result.fail("新增图片失败");
        }
        return Result.success(image);
    }

    /**
     * 批量保存商品相册
     * POST /api/product/image/batch  body: [ {productId,imageUrl,sort}, ... ]
     */
    @PostMapping("/batch")
    public Result<Boolean> addBatch(@RequestBody List<ProductImage> list) {
        int rows = productImageService.addBatch(list);
        if (rows <= 0) {
            return Result.fail("批量保存图片失败");
        }
        return Result.success(true);
    }

    /**
     * 更新图片
     * PUT /api/product/image/update
     */
    @PutMapping("/update")
    public Result<Boolean> update(@RequestBody ProductImage image) {
        int rows = productImageService.update(image);
        if (rows <= 0) {
            return Result.fail("更新图片失败");
        }
        return Result.success(true);
    }

    /**
     * 删除单张图片
     * DELETE /api/product/image/{id}
     */
    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable Long id) {
        boolean ok = productImageService.delete(id);
        if (!ok) {
            return Result.fail("删除图片失败");
        }
        return Result.success(true);
    }

    /**
     * 删除某商品全部图片
     * DELETE /api/product/image/product/{productId}
     */
    @DeleteMapping("/product/{productId}")
    public Result<Boolean> deleteByProduct(@PathVariable Long productId) {
        productImageService.deleteByProductId(productId);
        return Result.success(true);
    }
}
