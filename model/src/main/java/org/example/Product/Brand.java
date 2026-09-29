package org.example.Product;

import lombok.Data;

import java.util.Date;

/**
 * 品牌实体类 - 对应 brand 表
 */
@Data
public class Brand {
    private Long id;
    private String name;          // 品牌名称
    private String logo;          // 品牌Logo
    private String description;   // 品牌简介
    private Date createTime;
}
