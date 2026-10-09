package org.example.Mapper;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * 平台统计聚合 Mapper（学习项目：user/product/merchant/orders 同库，直接跨表聚合）
 */
public interface StatsMapper {

    @Select("select count(*) from user")
    long countUsers();

    @Select("select count(*) from product where status = 1")
    long countProductsOnSale();

    @Select("select count(*) from merchant where status = 1")
    long countMerchants();

    @Select("select count(*) from orders")
    long countOrders();

    @Select("select IFNULL(sum(pay_amount), 0) from orders where status in (1, 2, 3)")
    double sumRevenue();

    /** 用户注册趋势（近 N 个月，按月分组） */
    @Select("select DATE_FORMAT(create_time, '%Y-%m') as `month`, count(*) as cnt " +
            "from user where create_time >= #{start} group by `month`")
    List<Map<String, Object>> userTrend(@Param("start") String start);

    /** 商家入驻申请趋势 */
    @Select("select DATE_FORMAT(create_time, '%Y-%m') as `month`, count(*) as cnt " +
            "from merchant where create_time >= #{start} group by `month`")
    List<Map<String, Object>> merchantTrend(@Param("start") String start);

    /** 商品发布趋势 */
    @Select("select DATE_FORMAT(create_time, '%Y-%m') as `month`, count(*) as cnt " +
            "from product where create_time >= #{start} group by `month`")
    List<Map<String, Object>> productTrend(@Param("start") String start);

    /** 每月平台收益（已支付订单实付金额合计） */
    @Select("select DATE_FORMAT(create_time, '%Y-%m') as `month`, IFNULL(sum(pay_amount), 0) as amount " +
            "from orders where status in (1, 2, 3) and create_time >= #{start} group by `month`")
    List<Map<String, Object>> revenueTrend(@Param("start") String start);

    /** 在售商品分类占比（饼图） */
    @Select("select c.name as name, count(*) as value from product p " +
            "left join category c on p.category_id = c.id " +
            "where p.status = 1 group by p.category_id, c.name order by value desc")
    List<Map<String, Object>> categoryPie();

    // ========== 明细下钻（图表点击弹窗） ==========

    /** 某月注册用户明细 */
    @Select("select id, username, phone, DATE_FORMAT(create_time, '%Y-%m-%d %H:%i') as createTime " +
            "from user where create_time >= #{start} and create_time < #{end} " +
            "order by create_time desc limit 50")
    List<Map<String, Object>> userDetail(@Param("start") String start, @Param("end") String end);

    /** 某月商家申请明细 */
    @Select("select m.id, m.shop_name as shopName, u.username, m.contact_phone as contactPhone, " +
            "m.business_license as businessLicense, m.status, " +
            "DATE_FORMAT(m.create_time, '%Y-%m-%d %H:%i') as createTime " +
            "from merchant m left join user u on m.user_id = u.id " +
            "where m.create_time >= #{start} and m.create_time < #{end} " +
            "order by m.create_time desc limit 50")
    List<Map<String, Object>> merchantDetail(@Param("start") String start, @Param("end") String end);

    /** 某月商品发布明细 */
    @Select("select id, name, price, status, DATE_FORMAT(create_time, '%Y-%m-%d %H:%i') as createTime " +
            "from product where create_time >= #{start} and create_time < #{end} " +
            "order by create_time desc limit 50")
    List<Map<String, Object>> productDetail(@Param("start") String start, @Param("end") String end);

    /** 某月已支付订单明细（收益下钻） */
    @Select("select order_no as orderNo, pay_amount as payAmount, status, " +
            "DATE_FORMAT(create_time, '%Y-%m-%d %H:%i') as createTime " +
            "from orders where status in (1, 2, 3) and create_time >= #{start} and create_time < #{end} " +
            "order by create_time desc limit 50")
    List<Map<String, Object>> revenueDetail(@Param("start") String start, @Param("end") String end);

    /** 某分类在售商品明细（饼图下钻） */
    @Select("select p.id, p.name, p.price, p.stock, " +
            "DATE_FORMAT(p.create_time, '%Y-%m-%d %H:%i') as createTime " +
            "from product p left join category c on p.category_id = c.id " +
            "where p.status = 1 and c.name = #{name} " +
            "order by p.create_time desc limit 50")
    List<Map<String, Object>> categoryDetail(@Param("name") String name);
}
