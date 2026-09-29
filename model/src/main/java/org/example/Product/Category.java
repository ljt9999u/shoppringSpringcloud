package org.example.Product;

import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * 商品分类实体类 - 对应 category 表
 */
@Data
public class Category {
    private Long id;
    private Long parentId;        // 父分类ID 0为根
    private String name;          // 分类名称
    private String icon;          // 分类图标
    private Integer sort;         // 排序
    private Integer status;       // 状态 0禁用 1启用
    private Date createTime;

    // ========== 非数据库字段 - 组装分类树 ==========
    private List<Category> children;
}
