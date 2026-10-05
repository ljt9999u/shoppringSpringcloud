package org.example.Product;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 商品评价实体类 - 对应 product_comment 表
 * 购买商品后才可评价，商家可回复
 */
@Data
public class ProductComment {
    private Long id;
    private Long productId;        // 商品ID
    private Long userId;           // 评论用户ID
    private Long orderId;          // 关联订单ID（必须购买后评价）
    private Integer star;          // 评分 1~5 星
    private String content;        // 评论内容
    private String commentImg;     // 评论图片（多张逗号分隔）
    private String merchantReply;  // 商家回复
    private Integer status;        // 1展示 0隐藏
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
