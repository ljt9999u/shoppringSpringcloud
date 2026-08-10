package org.example.Cart;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 购物车实体类（对应 cart 表）
 */
@Data
public class Cart {
    private Long id;
    private Long userId;            // 用户ID
    private Long productId;         // 商品ID
    private Long specId;            // 规格ID（可空）
    private Integer quantity;      // 购买数量
    private Integer checked;        // 是否勾选 0否 1是
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    // ========== 非数据库字段 - 跨服务查询商品信息 ==========
    private String productName;     // 商品名称
    private String productImage;    // 商品主图
    private BigDecimal price;       // 商品现价
    private Integer stock;          // 商品库存
    private Integer productStatus;  // 商品状态 0下架 1上架（用于判断是否失效）
    private BigDecimal subtotal;    // 小计 = price * quantity
    private Boolean valid;          // 购物车项是否有效（商品存在且上架）
}
