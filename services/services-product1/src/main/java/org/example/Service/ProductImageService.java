package org.example.Service;

import org.example.Product.ProductImage;

import java.util.List;

/**
 * 商品图片服务
 */
public interface ProductImageService {

    /**
     * 查询某商品的全部图片
     */
    List<ProductImage> listByProductId(Long productId);

    /**
     * 新增单张图片
     */
    int add(ProductImage image);

    /**
     * 批量新增图片
     */
    int addBatch(List<ProductImage> list);

    /**
     * 更新图片
     */
    int update(ProductImage image);

    /**
     * 删除单张图片
     */
    boolean delete(Long id);

    /**
     * 删除某商品全部图片
     */
    boolean deleteByProductId(Long productId);
}
