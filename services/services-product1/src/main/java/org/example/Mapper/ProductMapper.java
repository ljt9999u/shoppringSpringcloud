package org.example.Mapper;

import org.apache.ibatis.annotations.*;
import org.example.Product.Product;

import java.util.List;

/**
 * 商品 Mapper
 */
@Mapper
public interface ProductMapper {

    /**
     * 根据ID查询商品
     */
    @Select("select * from product where id = #{id}")
    Product findById(Long id);

    /**
     * 查询所有上架商品
     * 限制最多 100 条，防止商品数据量过大时一次性加载导致 OOM；
     * 全量遍历请使用分页接口 findPageOnShelf
     */
    @Select("select * from product where status = 1 order by create_time desc limit 100")
    List<Product> findAllOnShelf();

    /**
     * 根据商品名称模糊查询
     * 限制最多 100 条，防止热门关键词命中过多商品导致 OOM；
     * 大量结果请使用分页接口 searchPageByName
     */
    @Select("select * from product where name like concat('%', #{keyword}, '%') and status = 1 limit 100")
    List<Product> searchByName(String keyword);

    // ========== 分页查询 ==========

    /**
     * 分页查询上架商品
     */
    @Select("select * from product where status = 1 order by create_time desc limit #{offset}, #{pageSize}")
    List<Product> findPageOnShelf(@Param("offset") int offset, @Param("pageSize") int pageSize);

    /**
     * 查询上架商品总数
     */
    @Select("select count(*) from product where status = 1")
    long countOnShelf();

    /**
     * 分页模糊搜索商品
     */
    @Select("select * from product where name like concat('%', #{keyword}, '%') and status = 1 limit #{offset}, #{pageSize}")
    List<Product> searchPageByName(@Param("keyword") String keyword, @Param("offset") int offset, @Param("pageSize") int pageSize);

    /**
     * 模糊搜索商品总数
     */
    @Select("select count(*) from product where name like concat('%', #{keyword}, '%') and status = 1")
    long countSearchByName(@Param("keyword") String keyword);

    /**
     * 按分类分页查询商品
     */
    @Select("select * from product where category_id = #{categoryId} and status = 1 order by create_time desc limit #{offset}, #{pageSize}")
    List<Product> findPageByCategory(@Param("categoryId") Long categoryId, @Param("offset") int offset, @Param("pageSize") int pageSize);

    /**
     * 按分类查询商品总数
     */
    @Select("select count(*) from product where category_id = #{categoryId} and status = 1")
    long countByCategory(@Param("categoryId") Long categoryId);

    /**
     * 根据分类查询商品
     * 限制最多 100 条，防止大分类下商品过多导致 OOM；
     * 大量结果请使用分页接口 findPageByCategory
     */
    @Select("select * from product where category_id = #{categoryId} and status = 1 limit 100")
    List<Product> findByCategory(Long categoryId);

    /**
     * 新增商品
     */
    @Insert("insert into product (merchant_id, category_id, brand_id, name, subtitle, main_image, detail, " +
            "price, original_price, stock, sales, status, reject_reason) " +
            "values (#{merchantId}, #{categoryId}, #{brandId}, #{name}, #{subtitle}, #{mainImage}, #{detail}, " +
            "#{price}, #{originalPrice}, #{stock}, 0, #{status}, #{rejectReason})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Product product);

    /**
     * 更新商品
     */
    @Update("update product set merchant_id = #{merchantId}, category_id = #{categoryId}, brand_id = #{brandId}, " +
            "name = #{name}, subtitle = #{subtitle}, main_image = #{mainImage}, detail = #{detail}, " +
            "price = #{price}, original_price = #{originalPrice}, stock = #{stock}, status = #{status}, " +
            "reject_reason = #{rejectReason} " +
            "where id = #{id}")
    int update(Product product);

    /**
     * 下架商品（逻辑删除）
     */
    @Update("update product set status = 0 where id = #{id}")
    int deleteById(Long id);

    /**
     * 扣减库存
     */
    @Update("update product set stock = stock - #{quantity} where id = #{id} and stock >= #{quantity}")
    int reduceStock(@Param("id") Long id, @Param("quantity") int quantity);

    // ========== 商家后台 ==========

    /**
     * 按商家分页查询商品（含下架/待审核，商家商品管理用）
     */
    @Select("<script>" +
            "select * from product where merchant_id = #{merchantId} " +
            "<if test='status != null'>and status = #{status} </if>" +
            "order by create_time desc limit #{offset}, #{pageSize}" +
            "</script>")
    List<Product> findPageByMerchant(@Param("merchantId") Long merchantId,
                                     @Param("status") Integer status,
                                     @Param("offset") int offset,
                                     @Param("pageSize") int pageSize);

    /**
     * 按商家查询商品总数
     */
    @Select("<script>" +
            "select count(*) from product where merchant_id = #{merchantId} " +
            "<if test='status != null'>and status = #{status} </if>" +
            "</script>")
    long countByMerchant(@Param("merchantId") Long merchantId, @Param("status") Integer status);

    // ========== 管理员后台：商品审核 ==========

    /**
     * 分页查询待审核商品（status = 2）
     */
    @Select("select * from product where status = 2 order by create_time asc limit #{offset}, #{pageSize}")
    List<Product> findPageAudit(@Param("offset") int offset, @Param("pageSize") int pageSize);

    /**
     * 待审核商品总数
     */
    @Select("select count(*) from product where status = 2")
    long countAudit();

    // ========== 管理员后台：全平台商品监管 ==========

    /**
     * 管理端分页查询全部状态商品（可按状态、名称过滤）
     */
    @Select("<script>" +
            "select * from product where 1 = 1 " +
            "<if test='status != null'>and status = #{status} </if>" +
            "<if test='keyword != null and keyword != \"\"'>and name like concat('%', #{keyword}, '%') </if>" +
            "order by create_time desc limit #{offset}, #{pageSize}" +
            "</script>")
    List<Product> findPageAdmin(@Param("status") Integer status,
                                @Param("keyword") String keyword,
                                @Param("offset") int offset,
                                @Param("pageSize") int pageSize);

    /**
     * 管理端商品总数
     */
    @Select("<script>" +
            "select count(*) from product where 1 = 1 " +
            "<if test='status != null'>and status = #{status} </if>" +
            "<if test='keyword != null and keyword != \"\"'>and name like concat('%', #{keyword}, '%') </if>" +
            "</script>")
    long countAdmin(@Param("status") Integer status, @Param("keyword") String keyword);

    /**
     * 审核商品：通过→status=1 并清空拒绝原因；拒绝→status=0 并写入拒绝原因
     */
    @Update("<script>" +
            "update product set status = #{status}, " +
            "<choose>" +
            "  <when test='status == 1'>reject_reason = null</when>" +
            "  <otherwise>reject_reason = #{rejectReason}</otherwise>" +
            "</choose> " +
            "where id = #{id}" +
            "</script>")
    int updateAuditStatus(@Param("id") Long id,
                          @Param("status") int status,
                          @Param("rejectReason") String rejectReason);
}
