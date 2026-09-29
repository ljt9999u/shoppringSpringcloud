package org.example.Controller;

import org.example.Product.Category;
import org.example.Service.CategoryService;
import org.example.common.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 商品分类控制器
 * 网关路由：/api/category/** -> services-product1
 */
@RestController
@RequestMapping("/api/category")
@CrossOrigin
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    /**
     * 分类树（首页/导航用）
     * GET /api/category/tree
     */
    @GetMapping("/tree")
    public Result<List<Category>> tree() {
        return Result.success(categoryService.tree());
    }

    /**
     * 启用分类平铺列表
     * GET /api/category/list
     */
    @GetMapping("/list")
    public Result<List<Category>> list() {
        return Result.success(categoryService.listEnabled());
    }

    /**
     * 全部分类（管理端，含禁用）
     * GET /api/category/listAll
     */
    @GetMapping("/listAll")
    public Result<List<Category>> listAll() {
        return Result.success(categoryService.listAll());
    }

    /**
     * 查询直接子分类
     * GET /api/category/children/{parentId}
     */
    @GetMapping("/children/{parentId}")
    public Result<List<Category>> children(@PathVariable Long parentId) {
        return Result.success(categoryService.listChildren(parentId));
    }

    /**
     * 根据ID查询分类
     * GET /api/category/{id}
     */
    @GetMapping("/{id}")
    public Result<Category> getById(@PathVariable Long id) {
        Category category = categoryService.getById(id);
        if (category == null) {
            return Result.fail("分类不存在");
        }
        return Result.success(category);
    }

    /**
     * 新增分类
     * POST /api/category/add
     */
    @PostMapping("/add")
    public Result<Category> add(@RequestBody Category category) {
        int rows = categoryService.add(category);
        if (rows <= 0) {
            return Result.fail("新增分类失败");
        }
        return Result.success(category);
    }

    /**
     * 更新分类
     * PUT /api/category/update
     */
    @PutMapping("/update")
    public Result<Boolean> update(@RequestBody Category category) {
        int rows = categoryService.update(category);
        if (rows <= 0) {
            return Result.fail("更新分类失败");
        }
        return Result.success(true);
    }

    /**
     * 修改分类状态
     * PUT /api/category/status/{id}?status=0
     */
    @PutMapping("/status/{id}")
    public Result<Boolean> updateStatus(@PathVariable Long id, @RequestParam int status) {
        boolean ok = categoryService.updateStatus(id, status);
        if (!ok) {
            return Result.fail("修改分类状态失败");
        }
        return Result.success(true);
    }

    /**
     * 删除分类（存在子分类时拒绝）
     * DELETE /api/category/{id}
     */
    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable Long id) {
        boolean ok = categoryService.delete(id);
        if (!ok) {
            return Result.fail("删除失败：该分类下存在子分类，请先删除子分类");
        }
        return Result.success(true);
    }
}
