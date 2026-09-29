package org.example.Product;

import lombok.Data;

/**
 * 商品图片实体类 - 对应 product_image 表（商品详情轮播多图）
 */
@Data
public class ProductImage {
    private Long id;
    private Long productId;       // 商品ID
    private String imageUrl;      // 图片URL
    private Integer sort;         // 排序
}
