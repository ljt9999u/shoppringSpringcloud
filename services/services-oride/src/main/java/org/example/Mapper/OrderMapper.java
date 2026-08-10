package org.example.Mapper;

import org.apache.ibatis.annotations.*;
import org.example.Order.OrderPOJO;
import org.example.Order.OrderDetail;
import org.example.Order.Payment;
import org.example.Order.Refund;
import org.example.Order.Logistics;
import org.example.Order.Comment;

import java.util.List;

/**
 * 订单 Mapper 接口
 */
@Mapper
public interface OrderMapper {

    // ========== 订单 CRUD ==========

    /**
     * 插入订单
     */
    @Insert("INSERT INTO orders(order_no, user_id, merchant_id, total_amount, freight, pay_amount, " +
            "status, address_id, remark) " +
            "VALUES(#{orderNo}, #{userId}, #{merchantId}, #{totalAmount}, #{freight}, #{payAmount}, " +
            "#{status}, #{addressId}, #{remark})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertOrder(OrderPOJO order);

    /**
     * 根据ID查询订单
     */
    @Select("SELECT * FROM orders WHERE id = #{id}")
    OrderPOJO findById(Long id);

    /**
     * 根据订单号查询订单
     */
    @Select("SELECT * FROM orders WHERE order_no = #{orderNo}")
    OrderPOJO findByOrderNo(String orderNo);

    /**
     * 查询用户的所有订单
     */
    @Select("SELECT * FROM orders WHERE user_id = #{userId} ORDER BY create_time DESC")
    List<OrderPOJO> findByUserId(Long userId);

    /**
     * 分页查询用户订单
     */
    @Select("SELECT * FROM orders WHERE user_id = #{userId} ORDER BY create_time DESC LIMIT #{offset}, #{pageSize}")
    List<OrderPOJO> findPageByUserId(@Param("userId") Long userId, @Param("offset") int offset, @Param("pageSize") int pageSize);

    /**
     * 统计用户订单数
     */
    @Select("SELECT COUNT(*) FROM orders WHERE user_id = #{userId}")
    long countByUserId(Long userId);

    /**
     * 查询商家订单
     */
    @Select("SELECT * FROM orders WHERE merchant_id = #{merchantId} ORDER BY create_time DESC LIMIT #{offset}, #{pageSize}")
    List<OrderPOJO> findPageByMerchantId(@Param("merchantId") Long merchantId, @Param("offset") int offset, @Param("pageSize") int pageSize);

    /**
     * 统计商家订单数
     */
    @Select("SELECT COUNT(*) FROM orders WHERE merchant_id = #{merchantId}")
    long countByMerchantId(Long merchantId);

    /**
     * 更新订单状态
     */
    @Update("UPDATE orders SET status = #{status} WHERE id = #{orderId}")
    int updateStatus(@Param("orderId") Long orderId, @Param("status") int status);

    /**
     * 支付成功 - 更新订单状态和支付时间
     */
    @Update("UPDATE orders SET status = 1, pay_time = NOW() WHERE id = #{orderId}")
    int updatePaySuccess(Long orderId);

    /**
     * 发货 - 更新订单状态和发货时间
     */
    @Update("UPDATE orders SET status = 2, ship_time = NOW() WHERE id = #{orderId}")
    int updateShipSuccess(Long orderId);

    /**
     * 收货 - 更新订单状态和收货时间
     */
    @Update("UPDATE orders SET status = 3, receive_time = NOW() WHERE id = #{orderId}")
    int updateReceiveSuccess(Long orderId);

    /**
     * 统计商品的订单数量
     */
    @Select("SELECT COUNT(DISTINCT o.id) FROM orders o " +
            "JOIN order_detail od ON o.id = od.order_id " +
            "WHERE od.product_id = #{productId}")
    int countOrdersByProduct(Long productId);

    // ========== 订单详情 ==========

    /**
     * 批量插入订单详情
     */
    @Insert("<script>" +
            "INSERT INTO order_detail(order_id, product_id, product_name, product_image, spec_id, spec_name, price, quantity, subtotal) " +
            "VALUES " +
            "<foreach collection='list' item='item' separator=','>" +
            "(#{item.orderId}, #{item.productId}, #{item.productName}, #{item.productImage}, " +
            "#{item.specId}, #{item.specName}, #{item.price}, #{item.quantity}, #{item.subtotal})" +
            "</foreach>" +
            "</script>")
    int batchInsertDetail(@Param("list") List<OrderDetail> list);

    /**
     * 根据订单ID查询详情
     */
    @Select("SELECT * FROM order_detail WHERE order_id = #{orderId}")
    List<OrderDetail> findDetailByOrderId(Long orderId);

    // ========== 支付 ==========

    /**
     * 创建支付记录
     */
    @Insert("INSERT INTO payment(order_no, pay_method, pay_amount, trade_no, status) " +
            "VALUES(#{orderNo}, #{payMethod}, #{payAmount}, #{tradeNo}, #{status})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertPayment(Payment payment);

    /**
     * 根据订单号查询支付记录
     */
    @Select("SELECT * FROM payment WHERE order_no = #{orderNo}")
    Payment findPaymentByOrderNo(String orderNo);

    /**
     * 更新支付状态
     */
    @Update("UPDATE payment SET status = #{status}, pay_time = NOW() WHERE order_no = #{orderNo}")
    int updatePaymentStatus(@Param("orderNo") String orderNo, @Param("status") int status);

    // ========== 退款 ==========

    /**
     * 创建退款申请
     */
    @Insert("INSERT INTO refund(order_no, user_id, refund_amount, reason, status) " +
            "VALUES(#{orderNo}, #{userId}, #{refundAmount}, #{reason}, #{status})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertRefund(Refund refund);

    /**
     * 根据订单号查询退款
     */
    @Select("SELECT * FROM refund WHERE order_no = #{orderNo}")
    Refund findRefundByOrderNo(String orderNo);

    /**
     * 更新退款状态
     */
    @Update("UPDATE refund SET status = #{status} WHERE id = #{id}")
    int updateRefundStatus(@Param("id") Long id, @Param("status") int status);

    // ========== 物流 ==========

    /**
     * 创建物流记录
     */
    @Insert("INSERT INTO logistics(order_no, logistics_no, company, status) " +
            "VALUES(#{orderNo}, #{logisticsNo}, #{company}, #{status})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertLogistics(Logistics logistics);

    /**
     * 根据订单号查询物流
     */
    @Select("SELECT * FROM logistics WHERE order_no = #{orderNo}")
    Logistics findLogisticsByOrderNo(String orderNo);

    /**
     * 更新物流状态
     */
    @Update("UPDATE logistics SET status = #{status} WHERE order_no = #{orderNo}")
    int updateLogisticsStatus(@Param("orderNo") String orderNo, @Param("status") int status);

    // ========== 评价 ==========

    /**
     * 插入评价
     */
    @Insert("INSERT INTO comment(order_id, product_id, user_id, rating, content, images, is_anonymous) " +
            "VALUES(#{orderId}, #{productId}, #{userId}, #{rating}, #{content}, #{images}, #{isAnonymous})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertComment(Comment comment);

    /**
     * 查询商品评价
     */
    @Select("SELECT * FROM comment WHERE product_id = #{productId} ORDER BY create_time DESC LIMIT #{offset}, #{pageSize}")
    List<Comment> findCommentByProductId(@Param("productId") Long productId, @Param("offset") int offset, @Param("pageSize") int pageSize);

    /**
     * 统计商品评价数
     */
    @Select("SELECT COUNT(*) FROM comment WHERE product_id = #{productId}")
    int countCommentByProduct(Long productId);

    /**
     * 查询订单评价
     */
    @Select("SELECT * FROM comment WHERE order_id = #{orderId}")
    Comment findCommentByOrderId(Long orderId);
}
