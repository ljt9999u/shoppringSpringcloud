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
     */
    @Select("select * from product where status = 1 order by create_time desc")
    List<Product> findAllOnShelf();

    /**
     * 根据商品名称模糊查询
     */
    @Select("select * from product where name like concat('%', #{keyword}, '%') and status = 1")
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
     */
    @Select("select * from product where category_id = #{categoryId} and status = 1")
    List<Product> findByCategory(Long categoryId);

    /**
     * 新增商品
     */
    @Insert("insert into product (merchant_id, category_id, brand_id, name, subtitle, main_image, detail, " +
            "price, original_price, stock, sales, status) " +
            "values (#{merchantId}, #{categoryId}, #{brandId}, #{name}, #{subtitle}, #{mainImage}, #{detail}, " +
            "#{price}, #{originalPrice}, #{stock}, 0, #{status})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Product product);

    /**
     * 更新商品
     */
    @Update("update product set merchant_id = #{merchantId}, category_id = #{categoryId}, brand_id = #{brandId}, " +
            "name = #{name}, subtitle = #{subtitle}, main_image = #{mainImage}, detail = #{detail}, " +
            "price = #{price}, original_price = #{originalPrice}, stock = #{stock}, status = #{status} " +
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
}
