package org.example.Order;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 退款实体类（对应 refund 表）
 */
@Data
public class Refund {
    private Long id;
    private String orderNo;
    private Long userId;
    private BigDecimal refundAmount;
    private String reason;
    private Integer status;         // 0申请中 1已同意 2已拒绝 3已退款
    private LocalDateTime createTime;
}
