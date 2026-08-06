package org.example.Service;

import org.example.Product.Product;
import org.example.common.PageResult;

import java.util.List;

public interface ProductService {

    /**
     * 根据ID查询商品
     */
    Product getProduct(Long productId);

    /**
     * 查询所有上架商品
     */
    List<Product> listAllOnShelf();

    /**
     * 根据名称搜索商品
     */
    List<Product> searchByName(String keyword);

    /**
     * 根据分类查询商品
     */
    List<Product> listByCategory(Long categoryId);

    // ========== 分页查询 ==========

    /**
     * 分页查询上架商品
     */
    PageResult<Product> listPageOnShelf(int pageNum, int pageSize);

    /**
     * 分页模糊搜索商品
     */
    PageResult<Product> searchPageByName(String keyword, int pageNum, int pageSize);

    /**
     * 按分类分页查询商品
     */
    PageResult<Product> listPageByCategory(Long categoryId, int pageNum, int pageSize);

    // ========== 增删改 ==========

    /**
     * 新增商品
     */
    int addProduct(Product product);

    /**
     * 更新商品
     */
    int updateProduct(Product product);

    /**
     * 下架商品（逻辑删除）
     */
    int deleteProduct(Long id);

    /**
     * 扣减库存
     */
    boolean reduceStock(Long id, int quantity);
}
