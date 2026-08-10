package org.example.Service;

import org.example.Merchant.Merchant;
import org.example.Merchant.UserAddress;
import org.example.common.PageResult;

import java.util.List;

/**
 * 商家服务接口
 */
public interface MerchantService {

    // ========== 商家业务 ==========

    /**
     * 商家入驻申请（创建商家记录，状态为待审核）
     * 远程调用用户服务校验用户是否存在
     */
    Merchant applyMerchant(Merchant merchant);

    /**
     * 根据ID查询商家（远程补充用户信息）
     */
    Merchant getMerchantById(Long id);

    /**
     * 根据用户ID查询商家
     */
    Merchant getMerchantByUserId(Long userId);

    /**
     * 分页查询所有商家
     */
    PageResult<Merchant> getMerchantPage(int pageNum, int pageSize);

    /**
     * 按状态分页查询商家
     * status: 0待审核 1已通过 2已拒绝
     */
    PageResult<Merchant> getMerchantPageByStatus(int status, int pageNum, int pageSize);

    /**
     * 更新商家信息
     */
    boolean updateMerchant(Merchant merchant);

    /**
     * 审核商家（管理员操作）
     * status: 0待审核 1已通过 2已拒绝
     */
    boolean auditMerchant(Long id, int status);

    /**
     * 删除商家
     */
    boolean deleteMerchant(Long id);

    // ========== 收货地址业务 ==========

    /**
     * 新增收货地址
     */
    UserAddress addAddress(UserAddress address);

    /**
     * 根据ID查询收货地址
     */
    UserAddress getAddressById(Long id);

    /**
     * 查询用户的所有收货地址
     */
    List<UserAddress> getAddressByUserId(Long userId);

    /**
     * 查询用户的默认收货地址
     */
    UserAddress getDefaultAddress(Long userId);

    /**
     * 更新收货地址
     */
    boolean updateAddress(UserAddress address);

    /**
     * 设置默认地址（先清除用户所有默认，再设置当前为默认）
     */
    boolean setDefaultAddress(Long userId, Long addressId);

    /**
     * 删除收货地址
     */
    boolean deleteAddress(Long id);
}
