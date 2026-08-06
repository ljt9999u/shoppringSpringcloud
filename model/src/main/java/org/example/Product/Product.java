package org.example.Product;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 商品实体类 - 对应 product 表
 */
@Data
public class Product {
    private Long id;
    private Long merchantId;         // 商家ID
    private Long categoryId;         // 分类ID
    private Long brandId;            // 品牌ID
    private String name;             // 商品名称
    private String subtitle;         // 副标题
    private String mainImage;        // 主图URL
    private String detail;           // 商品详情(富文本)
    private BigDecimal price;        // 现价
    private BigDecimal originalPrice; // 原价
    private Integer stock;           // 库存
    private Integer sales;           // 销量
    private Integer status;          // 状态: 0下架 1上架 2待审核
    private Date createTime;         // 创建时间
    private Date updateTime;         // 更新时间
}
