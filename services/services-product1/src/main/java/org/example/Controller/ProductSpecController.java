package org.example.Controller;

import org.example.Product.ProductSpec;
import org.example.Service.ProductSpecService;
import org.example.common.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 商品规格控制器（颜色、尺码等 SKU）
 * 挂在 /api/product/spec 下，走网关 /api/product/** 路由
 */
@RestController
@RequestMapping("/api/product/spec")
@CrossOrigin
public class ProductSpecController {

    @Autowired
    private ProductSpecService productSpecService;

    /**
     * 查询某商品的全部规格
     * GET /api/product/spec/list/{productId}
     */
    @GetMapping("/list/{productId}")
    public Result<List<ProductSpec>> list(@PathVariable Long productId) {
        return Result.success(productSpecService.listByProductId(productId));
    }

    /**
     * 根据ID查询规格
     * GET /api/product/spec/{id}
     */
    @GetMapping("/{id}")
    public Result<ProductSpec> getById(@PathVariable Long id) {
        ProductSpec spec = productSpecService.getById(id);
        if (spec == null) {
            return Result.fail("规格不存在");
        }
        return Result.success(spec);
    }

    /**
     * 新增规格
     * POST /api/product/spec/add
     */
    @PostMapping("/add")
    public Result<ProductSpec> add(@RequestBody ProductSpec spec) {
        int rows = productSpecService.add(spec);
        if (rows <= 0) {
            return Result.fail("新增规格失败");
        }
        return Result.success(spec);
    }

    /**
     * 更新规格
     * PUT /api/product/spec/update
     */
    @PutMapping("/update")
    public Result<Boolean> update(@RequestBody ProductSpec spec) {
        int rows = productSpecService.update(spec);
        if (rows <= 0) {
            return Result.fail("更新规格失败");
        }
        return Result.success(true);
    }

    /**
     * 删除规格
     * DELETE /api/product/spec/{id}
     */
    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable Long id) {
        boolean ok = productSpecService.delete(id);
        if (!ok) {
            return Result.fail("删除规格失败");
        }
        return Result.success(true);
    }

    /**
     * 扣减规格库存（下单时调用）
     * POST /api/product/spec/reduceStock?id=1&quantity=2
     */
    @PostMapping("/reduceStock")
    public Result<Boolean> reduceStock(@RequestParam Long id, @RequestParam int quantity) {
        boolean ok = productSpecService.reduceStock(id, quantity);
        if (!ok) {
            return Result.fail("规格库存不足");
        }
        return Result.success(true);
    }
}
