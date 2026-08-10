package org.example.Merchant;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 商家实体类（对应 merchant 表）
 */
@Data
public class Merchant {
    private Long id;
    private Long userId;              // 关联用户ID
    private String shopName;          // 店铺名称
    private String shopLogo;           // 店铺Logo
    private String businessLicense;   // 营业执照号
    private String contactPhone;      // 联系电话
    private Integer status;           // 审核状态 0待审核 1已通过 2已拒绝
    private LocalDateTime createTime;

    // 非数据库字段 - 关联用户信息
    private String username;          // 用户名（跨服务查询）
    private String phone;             // 用户手机号
}
