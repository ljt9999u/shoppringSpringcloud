package org.example.Mapper;

import org.apache.ibatis.annotations.*;
import org.example.Product.Brand;

import java.util.List;

/**
 * 品牌 Mapper
 */
@Mapper
public interface BrandMapper {

    /**
     * 查询全部品牌
     */
    @Select("SELECT * FROM brand ORDER BY id ASC")
    List<Brand> findAll();

    /**
     * 分页查询品牌
     */
    @Select("SELECT * FROM brand ORDER BY id ASC LIMIT #{offset}, #{pageSize}")
    List<Brand> findPage(@Param("offset") int offset, @Param("pageSize") int pageSize);

    /**
     * 品牌总数
     */
    @Select("SELECT COUNT(*) FROM brand")
    long count();

    /**
     * 根据ID查询
     */
    @Select("SELECT * FROM brand WHERE id = #{id}")
    Brand findById(Long id);

    /**
     * 新增品牌
     */
    @Insert("INSERT INTO brand(name, logo, description) VALUES(#{name}, #{logo}, #{description})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Brand brand);

    /**
     * 更新品牌
     */
    @Update("UPDATE brand SET name = #{name}, logo = #{logo}, description = #{description} WHERE id = #{id}")
    int update(Brand brand);

    /**
     * 删除品牌
     */
    @Delete("DELETE FROM brand WHERE id = #{id}")
    int deleteById(Long id);
}
