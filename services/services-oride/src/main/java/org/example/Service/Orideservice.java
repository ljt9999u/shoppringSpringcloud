package org.example.Service;

import org.example.Oride.OridePOJO;
import org.example.Order.*;
import org.example.common.PageResult;
import org.example.Product.Product;

public interface Orideservice {
    // ========== 原有方法 ==========
    OridePOJO createOride(Long productId, Long userId);

    Product getProductFromRemote(Long productId);

    /**
     * 查询商品订单数（供商品服务 Feign 调用）
     */
    int getOrderCountByProduct(Long productId);

    /**
     * 查询商品评价数（供商品服务 Feign 调用）
     */
    int getCommentCount(Long productId);

    // ========== 订单业务 ==========
    /**
     * 创建订单（下单）
     */
    OrderPOJO createOrder(OrderPOJO order);

    /**
     * 根据ID查询订单
     */
    OrderPOJO getOrderById(Long id);

    /**
     * 根据订单号查询订单
     */
    OrderPOJO getOrderByOrderNo(String orderNo);

    /**
     * 分页查询用户订单
     */
    PageResult<OrderPOJO> getUserOrdersPage(Long userId, int pageNum, int pageSize);

    /**
     * 分页查询商家订单
     */
    PageResult<OrderPOJO> getMerchantOrdersPage(Long merchantId, int pageNum, int pageSize);

    /**
     * 取消订单
     */
    boolean cancelOrder(Long orderId);

    /**
     * 支付订单
     */
    boolean payOrder(Long orderId, int payMethod, String tradeNo);

    /**
     * 发货
     */
    boolean shipOrder(Long orderId, String logisticsNo, String company);

    /**
     * 确认收货
     */
    boolean receiveOrder(Long orderId);

    /**
     * 查询订单详情列表
     */
    java.util.List<OrderDetail> getOrderDetails(Long orderId);

    // ========== 支付 ==========
    /**
     * 查询支付记录
     */
    Payment getPayment(String orderNo);

    // ========== 退款 ==========
    /**
     * 申请退款
     */
    boolean applyRefund(Refund refund);

    /**
     * 查询退款记录
     */
    Refund getRefund(String orderNo);

    /**
     * 处理退款申请（同意/拒绝）
     */
    boolean handleRefund(Long refundId, int status);

    // ========== 物流 ==========
    /**
     * 查询物流
     */
    Logistics getLogistics(String orderNo);

    /**
     * 更新物流状态
     */
    boolean updateLogisticsStatus(String orderNo, int status);

    // ========== 评价 ==========
    /**
     * 添加评价
     */
    boolean addComment(Comment comment);

    /**
     * 分页查询商品评价
     */
    PageResult<Comment> getProductCommentsPage(Long productId, int pageNum, int pageSize);
}
