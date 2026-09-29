package org.example.Controller;

import org.example.Product.Brand;
import org.example.Service.BrandService;
import org.example.common.PageResult;
import org.example.common.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 品牌控制器
 * 网关路由：/api/brand/** -> services-product1
 */
@RestController
@RequestMapping("/api/brand")
@CrossOrigin
public class BrandController {

    @Autowired
    private BrandService brandService;

    /**
     * 全部品牌（下拉选择用）
     * GET /api/brand/list
     */
    @GetMapping("/list")
    public Result<List<Brand>> list() {
        return Result.success(brandService.listAll());
    }

    /**
     * 分页查询品牌
     * GET /api/brand/page?pageNum=1&pageSize=10
     */
    @GetMapping("/page")
    public Result<PageResult<Brand>> page(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        return Result.success(brandService.page(pageNum, pageSize));
    }

    /**
     * 根据ID查询品牌
     * GET /api/brand/{id}
     */
    @GetMapping("/{id}")
    public Result<Brand> getById(@PathVariable Long id) {
        Brand brand = brandService.getById(id);
        if (brand == null) {
            return Result.fail("品牌不存在");
        }
        return Result.success(brand);
    }

    /**
     * 新增品牌
     * POST /api/brand/add
     */
    @PostMapping("/add")
    public Result<Brand> add(@RequestBody Brand brand) {
        int rows = brandService.add(brand);
        if (rows <= 0) {
            return Result.fail("新增品牌失败");
        }
        return Result.success(brand);
    }

    /**
     * 更新品牌
     * PUT /api/brand/update
     */
    @PutMapping("/update")
    public Result<Boolean> update(@RequestBody Brand brand) {
        int rows = brandService.update(brand);
        if (rows <= 0) {
            return Result.fail("更新品牌失败");
        }
        return Result.success(true);
    }

    /**
     * 删除品牌
     * DELETE /api/brand/{id}
     */
    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable Long id) {
        boolean ok = brandService.delete(id);
        if (!ok) {
            return Result.fail("删除品牌失败");
        }
        return Result.success(true);
    }
}
