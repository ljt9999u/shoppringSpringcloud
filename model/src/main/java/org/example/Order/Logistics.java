package org.example.Order;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 物流实体类（对应 logistics 表）
 */
@Data
public class Logistics {
    private Long id;
    private String orderNo;
    private String logisticsNo;      // 物流单号
    private String company;          // 物流公司
    private Integer status;         // 0待发货 1已发货 2已签收
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
