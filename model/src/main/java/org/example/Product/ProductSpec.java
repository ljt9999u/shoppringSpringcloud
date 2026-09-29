package org.example.Product;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 商品规格实体类 - 对应 product_spec 表（颜色、尺码等 SKU）
 */
@Data
public class ProductSpec {
    private Long id;
    private Long productId;       // 商品ID
    private String specName;      // 规格名(如:颜色)
    private String specValue;     // 规格值(如:红色)
    private Integer stock;        // 规格库存
    private BigDecimal price;     // 加价
}
