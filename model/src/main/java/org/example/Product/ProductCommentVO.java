package org.example.Product;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 商品评价展示对象
 * 在评价实体基础上携带用户展示信息（来自 user 表关联查询），
 * 避免前端再根据 userId 单独请求用户信息
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ProductCommentVO extends ProductComment {
    private String username;   // 用户名
    private String nickname;   // 昵称（展示优先取昵称）
    private String avatar;     // 头像URL
    private String productName;   // 商品名称（商家评价管理列表用）
    private String productImage;  // 商品主图（商家评价管理列表用）
}
