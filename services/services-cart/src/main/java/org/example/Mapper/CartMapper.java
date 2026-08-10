package org.example.Mapper;

import org.apache.ibatis.annotations.*;
import org.example.Cart.Cart;

import java.util.List;

/**
 * 购物车 Mapper 接口
 */
@Mapper
public interface CartMapper {

    /**
     * 新增购物车项
     */
    @Insert("INSERT INTO cart(user_id, product_id, spec_id, quantity, checked) " +
            "VALUES(#{userId}, #{productId}, #{specId}, #{quantity}, #{checked})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertCart(Cart cart);

    /**
     * 根据ID查询购物车项
     */
    @Select("SELECT * FROM cart WHERE id = #{id}")
    Cart findById(Long id);

    /**
     * 查询用户某商品+规格的购物车项（用于判断是否已加入过，合并数量）
     */
    @Select("SELECT * FROM cart WHERE user_id = #{userId} AND product_id = #{productId} " +
            "AND (spec_id <=> #{specId})")
    Cart findByUserProduct(@Param("userId") Long userId,
                           @Param("productId") Long productId,
                           @Param("specId") Long specId);

    /**
     * 查询用户的所有购物车项（按更新时间倒序）
     */
    @Select("SELECT * FROM cart WHERE user_id = #{userId} ORDER BY update_time DESC")
    List<Cart> findListByUserId(Long userId);

    /**
     * 查询用户勾选的购物车项（用于结算）
     */
    @Select("SELECT * FROM cart WHERE user_id = #{userId} AND checked = 1 ORDER BY update_time DESC")
    List<Cart> findCheckedByUserId(Long userId);

    /**
     * 统计用户购物车商品种类数（用于购物车角标）
     */
    @Select("SELECT COUNT(*) FROM cart WHERE user_id = #{userId}")
    int countByUserId(Long userId);

    /**
     * 增加数量（合并相同商品）
     */
    @Update("UPDATE cart SET quantity = quantity + #{quantity} WHERE id = #{id}")
    int addQuantity(@Param("id") Long id, @Param("quantity") int quantity);

    /**
     * 直接设置数量
     */
    @Update("UPDATE cart SET quantity = #{quantity} WHERE id = #{id}")
    int updateQuantity(@Param("id") Long id, @Param("quantity") int quantity);

    /**
     * 更新单个购物车项的勾选状态
     */
    @Update("UPDATE cart SET checked = #{checked} WHERE id = #{id}")
    int updateChecked(@Param("id") Long id, @Param("checked") int checked);

    /**
     * 全选/取消全选（更新用户所有购物车项的勾选状态）
     */
    @Update("UPDATE cart SET checked = #{checked} WHERE user_id = #{userId}")
    int updateAllChecked(@Param("userId") Long userId, @Param("checked") int checked);

    /**
     * 根据ID删除购物车项
     */
    @Delete("DELETE FROM cart WHERE id = #{id}")
    int deleteById(Long id);

    /**
     * 批量删除购物车项（下单后清空已结算项）
     */
    @Delete("<script>" +
            "DELETE FROM cart WHERE id IN " +
            "<foreach collection='ids' item='id' open='(' separator=',' close=')'>" +
            "#{id}" +
            "</foreach>" +
            "</script>")
    int batchDeleteByIds(@Param("ids") List<Long> ids);

    /**
     * 删除用户勾选的购物车项（结算后清空）
     */
    @Delete("DELETE FROM cart WHERE user_id = #{userId} AND checked = 1")
    int deleteCheckedByUserId(Long userId);

    /**
     * 清空用户购物车
     */
    @Delete("DELETE FROM cart WHERE user_id = #{userId}")
    int clearByUserId(Long userId);
}
