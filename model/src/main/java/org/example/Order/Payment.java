package org.example.Order;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 支付记录实体类（对应 payment 表）
 */
@Data
public class Payment {
    private Long id;
    private String orderNo;
    private Integer payMethod;      // 1微信 2支付宝 3余额
    private BigDecimal payAmount;
    private String tradeNo;         // 第三方交易号
    private Integer status;         // 0待支付 1已支付 2已退款
    private LocalDateTime payTime;
    private LocalDateTime createTime;
}
