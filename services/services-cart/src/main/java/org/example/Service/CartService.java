package org.example.Service;

import org.example.Cart.Cart;

import java.util.List;

/**
 * 购物车服务接口
 */
public interface CartService {

    /**
     * 加入购物车（同一用户同一商品+规格则合并数量）
     * 远程校验商品是否存在及库存
     */
    Cart addToCart(Cart cart);

    /**
     * 修改购物车项数量
     */
    boolean updateQuantity(Long id, int quantity);

    /**
     * 修改单个购物车项勾选状态
     * checked: 0取消勾选 1勾选
     */
    boolean updateChecked(Long id, int checked);

    /**
     * 全选/取消全选（更新用户所有购物车项的勾选状态）
     */
    boolean selectAll(Long userId, int checked);

    /**
     * 查询用户购物车列表（远程补充商品信息，标记失效项）
     */
    List<Cart> listByUserId(Long userId);

    /**
     * 查询用户勾选的购物车项（用于结算，远程补充商品信息）
     */
    List<Cart> listCheckedByUserId(Long userId);

    /**
     * 统计用户购物车商品种类数（购物车角标）
     */
    int countByUserId(Long userId);

    /**
     * 删除单个购物车项
     */
    boolean deleteById(Long id);

    /**
     * 批量删除购物车项
     */
    boolean batchDelete(List<Long> ids);

    /**
     * 清空用户购物车
     */
    boolean clearByUserId(Long userId);

    /**
     * 删除用户已勾选的购物车项（下单成功后调用）
     */
    boolean deleteCheckedByUserId(Long userId);
}
