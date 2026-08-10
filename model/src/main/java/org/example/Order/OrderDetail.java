package org.example.Order;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 订单详情实体类（对应 order_detail 表）
 */
@Data
public class OrderDetail {
    private Long id;
    private Long orderId;
    private Long productId;
    private String productName;     // 商品快照
    private String productImage;
    private Long specId;
    private String specName;        // 规格快照
    private BigDecimal price;
    private Integer quantity;
    private BigDecimal subtotal;
}
