package org.example.Service.Impl;

import lombok.extern.slf4j.Slf4j;
import org.example.Feign.UserFeign;
import org.example.Mapper.MerchantMapper;
import org.example.Merchant.Merchant;
import org.example.Merchant.UserAddress;
import org.example.Service.MerchantService;
import org.example.User.UserPOJO;
import org.example.common.PageResult;
import org.example.common.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 商家服务实现
 */
@Slf4j
@Service
public class MerchantServiceImpl implements MerchantService {

    @Autowired
    MerchantMapper merchantMapper;
    @Autowired
    UserFeign userFeign;

    // ========== 商家业务 ==========

    @Override
    public Merchant applyMerchant(Merchant merchant) {
        // 1. 远程调用用户服务，校验用户是否存在
        Result<UserPOJO> userResult = userFeign.getUserById(merchant.getUserId());
        if (userResult.getCode() != 200 || userResult.getData() == null) {
            throw new RuntimeException("用户不存在，入驻申请失败");
        }
        // 2. 校验该用户是否已入驻
        Merchant exist = merchantMapper.findByUserId(merchant.getUserId());
        if (exist != null) {
            throw new RuntimeException("该用户已提交过入驻申请");
        }
        // 3. 设置默认状态为待审核并保存
        if (merchant.getStatus() == null) {
            merchant.setStatus(0);
        }
        merchantMapper.insertMerchant(merchant);
        log.info("商家入驻申请成功，商家ID：{}，用户ID：{}", merchant.getId(), merchant.getUserId());
        return merchant;
    }

    @Override
    public Merchant getMerchantById(Long id) {
        Merchant merchant = merchantMapper.findById(id);
        if (merchant != null) {
            enrichMerchantUserInfo(merchant);
        }
        return merchant;
    }

    @Override
    public Merchant getMerchantByUserId(Long userId) {
        Merchant merchant = merchantMapper.findByUserId(userId);
        if (merchant != null) {
            enrichMerchantUserInfo(merchant);
        }
        return merchant;
    }

    /**
     * 远程调用用户服务，补充商家中的用户信息
     */
    private void enrichMerchantUserInfo(Merchant merchant) {
        try {
            Result<UserPOJO> userResult = userFeign.getUserById(merchant.getUserId());
            if (userResult.getCode() == 200 && userResult.getData() != null) {
                merchant.setUsername(userResult.getData().getUsername());
                merchant.setPhone(userResult.getData().getPhone());
            }
        } catch (Exception e) {
            log.warn("获取用户信息失败，merchantId={}, userId={}", merchant.getId(), merchant.getUserId());
        }
    }

    /**
     * 分页参数规范化
     */
    private int[] normalizePage(int pageNum, int pageSize) {
        if (pageNum < 1) pageNum = 1;
        if (pageSize < 1) pageSize = 10;
        if (pageSize > 100) pageSize = 100;
        return new int[]{pageNum, pageSize};
    }

    @Override
    public PageResult<Merchant> getMerchantPage(int pageNum, int pageSize) {
        int[] p = normalizePage(pageNum, pageSize);
        int offset = (p[0] - 1) * p[1];
        long total = merchantMapper.countAll();
        List<Merchant> list = merchantMapper.findPage(offset, p[1]);
        for (Merchant merchant : list) {
            enrichMerchantUserInfo(merchant);
        }
        return new PageResult<>(total, p[0], p[1], list);
    }

    @Override
    public PageResult<Merchant> getMerchantPageByStatus(int status, int pageNum, int pageSize) {
        int[] p = normalizePage(pageNum, pageSize);
        int offset = (p[0] - 1) * p[1];
        long total = merchantMapper.countByStatus(status);
        List<Merchant> list = merchantMapper.findPageByStatus(status, offset, p[1]);
        for (Merchant merchant : list) {
            enrichMerchantUserInfo(merchant);
        }
        return new PageResult<>(total, p[0], p[1], list);
    }

    @Override
    public boolean updateMerchant(Merchant merchant) {
        return merchantMapper.updateMerchant(merchant) > 0;
    }

    @Override
    public boolean auditMerchant(Long id, int status) {
        Merchant merchant = merchantMapper.findById(id);
        if (merchant == null) {
            return false;
        }
        return merchantMapper.updateStatus(id, status) > 0;
    }

    @Override
    public boolean deleteMerchant(Long id) {
        return merchantMapper.deleteById(id) > 0;
    }

    // ========== 收货地址业务 ==========

    /**
     * 新增收货地址：若设为默认，先清除该用户原有默认再插入，两步写必须同事务
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public UserAddress addAddress(UserAddress address) {
        // 如果设置为默认，先清除该用户其他默认地址
        if (address.getIsDefault() != null && address.getIsDefault() == 1) {
            merchantMapper.clearDefault(address.getUserId());
        }
        merchantMapper.insertAddress(address);
        return address;
    }

    @Override
    public UserAddress getAddressById(Long id) {
        return merchantMapper.findAddressById(id);
    }

    @Override
    public List<UserAddress> getAddressByUserId(Long userId) {
        return merchantMapper.findAddressByUserId(userId);
    }

    @Override
    public UserAddress getDefaultAddress(Long userId) {
        return merchantMapper.findDefaultAddress(userId);
    }

    @Override
    public boolean updateAddress(UserAddress address) {
        return merchantMapper.updateAddress(address) > 0;
    }

    /**
     * 设置默认地址：先清除用户所有默认，再设置当前为默认
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean setDefaultAddress(Long userId, Long addressId) {
        UserAddress address = merchantMapper.findAddressById(addressId);
        if (address == null || !address.getUserId().equals(userId)) {
            return false;
        }
        merchantMapper.clearDefault(userId);
        return merchantMapper.setDefault(addressId) > 0;
    }

    @Override
    public boolean deleteAddress(Long id) {
        return merchantMapper.deleteAddress(id) > 0;
    }
}
