package org.example.Merchant;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 收货地址实体类（对应 user_address 表）
 */
@Data
public class UserAddress {
    private Long id;
    private Long userId;
    private String consignee;        // 收货人
    private String phone;            // 联系电话
    private String province;
    private String city;
    private String district;
    private String detail;           // 详细地址
    private Integer isDefault;      // 0否 1是
    private LocalDateTime createTime;
}
