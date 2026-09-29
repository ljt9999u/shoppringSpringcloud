package org.example.Service;

import org.example.Product.Category;

import java.util.List;

/**
 * 商品分类服务
 */
public interface CategoryService {

    /**
     * 查询启用分类列表（平铺）
     */
    List<Category> listEnabled();

    /**
     * 查询全部分类（管理端，含禁用）
     */
    List<Category> listAll();

    /**
     * 查询分类树（parentId=0 为根，递归挂 children）
     */
    List<Category> tree();

    /**
     * 查询某分类的直接子分类
     */
    List<Category> listChildren(Long parentId);

    /**
     * 根据ID查询
     */
    Category getById(Long id);

    /**
     * 新增分类
     */
    int add(Category category);

    /**
     * 更新分类
     */
    int update(Category category);

    /**
     * 修改状态 0禁用 1启用
     */
    boolean updateStatus(Long id, int status);

    /**
     * 删除分类（存在子分类时拒绝）
     */
    boolean delete(Long id);
}
