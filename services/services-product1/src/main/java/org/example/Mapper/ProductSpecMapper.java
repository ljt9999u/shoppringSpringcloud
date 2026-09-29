package org.example.Mapper;

import org.apache.ibatis.annotations.*;
import org.example.Product.ProductSpec;

import java.util.List;

/**
 * 商品规格 Mapper
 */
@Mapper
public interface ProductSpecMapper {

    /**
     * 查询某商品的全部规格
     */
    @Select("SELECT * FROM product_spec WHERE product_id = #{productId} ORDER BY id ASC")
    List<ProductSpec> findByProductId(Long productId);

    /**
     * 根据ID查询
     */
    @Select("SELECT * FROM product_spec WHERE id = #{id}")
    ProductSpec findById(Long id);

    /**
     * 新增规格
     */
    @Insert("INSERT INTO product_spec(product_id, spec_name, spec_value, stock, price) " +
            "VALUES(#{productId}, #{specName}, #{specValue}, #{stock}, #{price})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(ProductSpec spec);

    /**
     * 更新规格
     */
    @Update("UPDATE product_spec SET spec_name = #{specName}, spec_value = #{specValue}, " +
            "stock = #{stock}, price = #{price} WHERE id = #{id}")
    int update(ProductSpec spec);

    /**
     * 删除规格
     */
    @Delete("DELETE FROM product_spec WHERE id = #{id}")
    int deleteById(Long id);

    /**
     * 扣减规格库存（库存充足才扣减，防止超卖）
     */
    @Update("UPDATE product_spec SET stock = stock - #{quantity} WHERE id = #{id} AND stock >= #{quantity}")
    int reduceStock(@Param("id") Long id, @Param("quantity") int quantity);
}
