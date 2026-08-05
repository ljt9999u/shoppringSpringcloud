package org.example.vo;

import lombok.Data;

/**
 * 注册返回 VO
 */
@Data
public class RegisterVO {
    private Long userId;        // 用户ID
    private String username;     // 用户名
    private String phone;        // 手机号
    private String roleCode;     // 角色码 USER/MERCHANT/ADMIN
}
