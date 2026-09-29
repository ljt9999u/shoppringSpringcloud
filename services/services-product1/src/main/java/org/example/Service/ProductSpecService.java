package org.example.Service;

import org.example.Product.ProductSpec;

import java.util.List;

/**
 * 商品规格服务
 */
public interface ProductSpecService {

    /**
     * 查询某商品的全部规格
     */
    List<ProductSpec> listByProductId(Long productId);

    /**
     * 根据ID查询
     */
    ProductSpec getById(Long id);

    /**
     * 新增规格
     */
    int add(ProductSpec spec);

    /**
     * 更新规格
     */
    int update(ProductSpec spec);

    /**
     * 删除规格
     */
    boolean delete(Long id);

    /**
     * 扣减规格库存
     */
    boolean reduceStock(Long id, int quantity);
}
