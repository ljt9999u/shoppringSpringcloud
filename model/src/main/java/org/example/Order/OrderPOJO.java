package org.example.Order;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 订单实体类（对应 orders 表）
 */
@Data
public class OrderPOJO {
    private Long id;
    private String orderNo;          // 订单号
    private Long userId;
    private Long merchantId;         // 商家ID
    private BigDecimal totalAmount;  // 商品总金额
    private BigDecimal freight;      // 运费
    private BigDecimal payAmount;    // 实付金额
    private Integer status;          // 0待付款 1待发货 2待收货 3已完成 4已取消 5已退款
    private Long addressId;          // 收货地址ID
    private String remark;
    private LocalDateTime payTime;
    private LocalDateTime shipTime;
    private LocalDateTime receiveTime;
    private LocalDateTime createTime;

    // 关联数据
    private List<OrderDetail> detailList;  // 订单详情
    private String username;              // 用户名（跨服务查询）
    private Integer payMethod;            // 支付方式 1微信 2支付宝 3余额（从 payment 表回填）
}
