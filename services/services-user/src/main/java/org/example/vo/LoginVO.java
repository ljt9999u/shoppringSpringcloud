package org.example.vo;

import lombok.Data;

/**
 * 登录返回 VO
 */
@Data
public class LoginVO {
    private String token;        // JWT token
    private Long userId;        // 用户ID
    private String username;    // 用户名
    private String phone;       // 手机号
    private String nickname;    // 昵称
    private String avatar;      // 头像URL
    private String roleCode;    // 角色码 USER/MERCHANT/ADMIN
}
