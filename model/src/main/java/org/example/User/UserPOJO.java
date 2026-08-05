package org.example.User;

import lombok.Data;

import java.util.Date;

/**
 * 用户实体类 - 对应 user 表
 */
@Data
public class UserPOJO {
    private Long id;
    private String username;
    private String password;
    private String phone;
    private String email;
    private String avatar;
    private String nickname;
    private Integer gender;
    private String roleCode;  // 角色码: USER用户 MERCHANT商家 ADMIN管理员
    private Integer status;     // 状态: 0禁用 1启用
    private Date createTime;
    private Date updateTime;
}
