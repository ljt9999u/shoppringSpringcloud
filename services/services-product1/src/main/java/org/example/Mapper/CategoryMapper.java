package org.example.Mapper;

import org.apache.ibatis.annotations.*;
import org.example.Product.Category;

import java.util.List;

/**
 * 商品分类 Mapper
 */
@Mapper
public interface CategoryMapper {

    /**
     * 查询所有启用分类（按排序字段升序）
     */
    @Select("SELECT * FROM category WHERE status = 1 ORDER BY sort ASC, id ASC")
    List<Category> findAllEnabled();

    /**
     * 查询全部分类（含禁用，管理端用）
     */
    @Select("SELECT * FROM category ORDER BY sort ASC, id ASC")
    List<Category> findAll();

    /**
     * 根据ID查询
     */
    @Select("SELECT * FROM category WHERE id = #{id}")
    Category findById(Long id);

    /**
     * 查询某父分类下的直接子分类
     */
    @Select("SELECT * FROM category WHERE parent_id = #{parentId} AND status = 1 ORDER BY sort ASC, id ASC")
    List<Category> findByParentId(Long parentId);

    /**
     * 新增分类
     */
    @Insert("INSERT INTO category(parent_id, name, icon, sort, status) " +
            "VALUES(#{parentId}, #{name}, #{icon}, #{sort}, #{status})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Category category);

    /**
     * 更新分类
     */
    @Update("UPDATE category SET parent_id = #{parentId}, name = #{name}, icon = #{icon}, " +
            "sort = #{sort}, status = #{status} WHERE id = #{id}")
    int update(Category category);

    /**
     * 修改分类状态
     */
    @Update("UPDATE category SET status = #{status} WHERE id = #{id}")
    int updateStatus(@Param("id") Long id, @Param("status") int status);

    /**
     * 删除分类
     */
    @Delete("DELETE FROM category WHERE id = #{id}")
    int deleteById(Long id);

    /**
     * 统计某父分类下的子分类数量（删除前校验是否有子分类）
     */
    @Select("SELECT COUNT(*) FROM category WHERE parent_id = #{parentId}")
    int countByParentId(Long parentId);
}
