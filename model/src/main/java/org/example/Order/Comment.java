package org.example.Order;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 商品评价实体类（对应 comment 表）
 */
@Data
public class Comment {
    private Long id;
    private Long orderId;
    private Long productId;
    private Long userId;
    private Integer rating;         // 评分 1-5
    private String content;
    private String images;          // 评价图片(逗号分隔)
    private Integer isAnonymous;    // 0否 1是
    private LocalDateTime createTime;
}
