package org.example.Mapper;

import org.apache.ibatis.annotations.*;
import org.example.Product.ProductImage;

import java.util.List;

/**
 * 商品图片 Mapper
 */
@Mapper
public interface ProductImageMapper {

    /**
     * 查询某商品的全部图片（按排序升序）
     */
    @Select("SELECT * FROM product_image WHERE product_id = #{productId} ORDER BY sort ASC, id ASC")
    List<ProductImage> findByProductId(Long productId);

    /**
     * 根据ID查询
     */
    @Select("SELECT * FROM product_image WHERE id = #{id}")
    ProductImage findById(Long id);

    /**
     * 新增单张图片
     */
    @Insert("INSERT INTO product_image(product_id, image_url, sort) VALUES(#{productId}, #{imageUrl}, #{sort})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(ProductImage image);

    /**
     * 批量新增图片（保存商品相册）
     */
    @Insert("<script>" +
            "INSERT INTO product_image(product_id, image_url, sort) VALUES " +
            "<foreach collection='list' item='item' separator=','>" +
            "(#{item.productId}, #{item.imageUrl}, #{item.sort})" +
            "</foreach>" +
            "</script>")
    int insertBatch(@Param("list") List<ProductImage> list);

    /**
     * 更新图片
     */
    @Update("UPDATE product_image SET image_url = #{imageUrl}, sort = #{sort} WHERE id = #{id}")
    int update(ProductImage image);

    /**
     * 删除单张图片
     */
    @Delete("DELETE FROM product_image WHERE id = #{id}")
    int deleteById(Long id);

    /**
     * 删除某商品的全部图片
     */
    @Delete("DELETE FROM product_image WHERE product_id = #{productId}")
    int deleteByProductId(Long productId);
}
