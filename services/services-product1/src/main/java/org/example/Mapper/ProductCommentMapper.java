package org.example.Mapper;

import org.apache.ibatis.annotations.*;
import org.example.Product.ProductComment;
import org.example.Product.ProductCommentVO;

import java.util.List;
import java.util.Map;

/**
 * 商品评价 Mapper（对应 product_comment 表）
 */
@Mapper
public interface ProductCommentMapper {

    /**
     * 分页查询某商品的展示中评价（关联 user 表带出用户展示信息）
     */
    @Select("SELECT pc.*, u.username AS username, u.nickname AS nickname, u.avatar AS avatar " +
            "FROM product_comment pc " +
            "LEFT JOIN `user` u ON u.id = pc.user_id " +
            "WHERE pc.product_id = #{productId} AND pc.status = 1 " +
            "ORDER BY pc.create_time DESC " +
            "LIMIT #{offset}, #{pageSize}")
    List<ProductCommentVO> findPageByProduct(@Param("productId") Long productId,
                                             @Param("offset") int offset,
                                             @Param("pageSize") int pageSize);

    /**
     * 统计某商品展示中评价数
     */
    @Select("SELECT COUNT(*) FROM product_comment WHERE product_id = #{productId} AND status = 1")
    long countByProduct(Long productId);

    /**
     * 分页查询某商家旗下所有商品的评价（含隐藏评价，供商家评价管理）
     * 关联 product 带出商品名称/主图，关联 user 带出评价人信息
     */
    @Select("SELECT pc.*, u.username AS username, u.nickname AS nickname, u.avatar AS avatar, " +
            "p.name AS productName, p.main_image AS productImage " +
            "FROM product_comment pc " +
            "JOIN product p ON p.id = pc.product_id " +
            "LEFT JOIN `user` u ON u.id = pc.user_id " +
            "WHERE p.merchant_id = #{merchantId} " +
            "ORDER BY pc.create_time DESC " +
            "LIMIT #{offset}, #{pageSize}")
    List<ProductCommentVO> findPageByMerchant(@Param("merchantId") Long merchantId,
                                              @Param("offset") int offset,
                                              @Param("pageSize") int pageSize);

    /**
     * 统计某商家旗下所有商品的评价数（含隐藏）
     */
    @Select("SELECT COUNT(*) FROM product_comment pc " +
            "JOIN product p ON p.id = pc.product_id " +
            "WHERE p.merchant_id = #{merchantId}")
    long countByMerchant(Long merchantId);

    /**
     * 评价汇总：总数、平均分、好评数(4~5星)、带图评价数
     */
    @Select("SELECT COUNT(*) AS total, " +
            "IFNULL(ROUND(AVG(star), 1), 0) AS avgStar, " +
            "COALESCE(SUM(CASE WHEN star >= 4 THEN 1 ELSE 0 END), 0) AS goodCount, " +
            "COALESCE(SUM(CASE WHEN comment_img IS NOT NULL AND comment_img <> '' THEN 1 ELSE 0 END), 0) AS imgCount " +
            "FROM product_comment WHERE product_id = #{productId} AND status = 1")
    Map<String, Object> summaryByProduct(Long productId);

    /**
     * 各星级数量分布
     */
    @Select("SELECT star AS star, COUNT(*) AS cnt " +
            "FROM product_comment WHERE product_id = #{productId} AND status = 1 " +
            "GROUP BY star")
    List<Map<String, Object>> starDistribution(Long productId);

    /**
     * 新增评价
     */
    @Insert("INSERT INTO product_comment(product_id, user_id, order_id, star, content, comment_img, merchant_reply, status) " +
            "VALUES(#{productId}, #{userId}, #{orderId}, #{star}, #{content}, #{commentImg}, #{merchantReply}, #{status})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(ProductComment comment);

    /**
     * 商家回复评价
     */
    @Update("UPDATE product_comment SET merchant_reply = #{merchantReply} WHERE id = #{id}")
    int reply(@Param("id") Long id, @Param("merchantReply") String merchantReply);

    /**
     * 校验用户是否已购买（已支付）该商品：订单状态 1待发货 2待收货 3已完成 均视为已购买
     * 订单与商品服务共用 shopping 库，可直接关联 orders + order_detail 校验
     */
    @Select("SELECT COUNT(*) FROM orders o " +
            "JOIN order_detail od ON od.order_id = o.id " +
            "WHERE o.id = #{orderId} AND o.user_id = #{userId} " +
            "AND od.product_id = #{productId} AND o.status IN (1, 2, 3)")
    int countPaidOrderDetail(@Param("orderId") Long orderId,
                             @Param("userId") Long userId,
                             @Param("productId") Long productId);

    /**
     * 校验该订单的该商品是否已评价过（一单同一商品只能评价一次）
     */
    @Select("SELECT COUNT(*) FROM product_comment WHERE order_id = #{orderId} AND product_id = #{productId}")
    int countByOrderProduct(@Param("orderId") Long orderId,
                            @Param("productId") Long productId);
}
