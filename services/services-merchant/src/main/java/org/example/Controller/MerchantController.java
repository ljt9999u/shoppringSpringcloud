package org.example.Controller;

import lombok.extern.slf4j.Slf4j;
import org.example.Merchant.Merchant;
import org.example.Merchant.UserAddress;
import org.example.Service.MerchantService;
import org.example.common.PageResult;
import org.example.common.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 商家控制器 - 商家入驻、审核、收货地址管理
 */
@RestController
@RequestMapping("/api/merchant")
@Slf4j
@CrossOrigin
public class MerchantController {

    @Autowired
    private MerchantService merchantService;

    /**
     * 健康检查
     * GET /api/merchant/health
     */
    @GetMapping("/health")
    public String health() {
        return "OK - services-merchant is running";
    }

    // ========== 商家业务 ==========

    /**
     * 商家入驻申请
     * POST /api/merchant/apply
     */
    @PostMapping("/apply")
    public Result<Merchant> apply(@RequestBody Merchant merchant) {
        try {
            Merchant created = merchantService.applyMerchant(merchant);
            return Result.success(created);
        } catch (Exception e) {
            log.error("商家入驻申请失败", e);
            return Result.fail("商家入驻申请失败：" + e.getMessage());
        }
    }

    /**
     * 根据ID查询商家
     * GET /api/merchant/{id}
     */
    @GetMapping("/{id}")
    public Result<Merchant> getById(@PathVariable Long id) {
        Merchant merchant = merchantService.getMerchantById(id);
        if (merchant == null) {
            return Result.fail("商家不存在");
        }
        return Result.success(merchant);
    }

    /**
     * 根据用户ID查询商家
     * GET /api/merchant/user/{userId}
     */
    @GetMapping("/user/{userId}")
    public Result<Merchant> getByUserId(@PathVariable Long userId) {
        Merchant merchant = merchantService.getMerchantByUserId(userId);
        if (merchant == null) {
            return Result.fail("该用户尚未入驻");
        }
        return Result.success(merchant);
    }

    /**
     * 分页查询所有商家
     * GET /api/merchant/page?pageNum=1&pageSize=10
     */
    @GetMapping("/page")
    public Result<PageResult<Merchant>> getPage(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        PageResult<Merchant> page = merchantService.getMerchantPage(pageNum, pageSize);
        return Result.success(page);
    }

    /**
     * 按状态分页查询商家
     * GET /api/merchant/pageByStatus?status=0&pageNum=1&pageSize=10
     * status: 0待审核 1已通过 2已拒绝
     */
    @GetMapping("/pageByStatus")
    public Result<PageResult<Merchant>> getPageByStatus(
            @RequestParam int status,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        PageResult<Merchant> page = merchantService.getMerchantPageByStatus(status, pageNum, pageSize);
        return Result.success(page);
    }

    /**
     * 更新商家信息
     * PUT /api/merchant/update
     */
    @PutMapping("/update")
    public Result<Boolean> update(@RequestBody Merchant merchant) {
        boolean success = merchantService.updateMerchant(merchant);
        if (!success) {
            return Result.fail("更新商家信息失败");
        }
        return Result.success(true);
    }

    /**
     * 审核商家（管理员操作）
     * PUT /api/merchant/audit/{id}?status=1
     * status: 0待审核 1已通过 2已拒绝
     */
    @PutMapping("/audit/{id}")
    public Result<Boolean> audit(@PathVariable Long id, @RequestParam int status) {
        boolean success = merchantService.auditMerchant(id, status);
        if (!success) {
            return Result.fail("审核失败，商家不存在");
        }
        return Result.success(true);
    }

    /**
     * 删除商家
     * DELETE /api/merchant/{id}
     */
    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable Long id) {
        boolean success = merchantService.deleteMerchant(id);
        if (!success) {
            return Result.fail("删除商家失败");
        }
        return Result.success(true);
    }

    // ========== 收货地址业务 ==========

    /**
     * 新增收货地址
     * POST /api/merchant/address/add
     */
    @PostMapping("/address/add")
    public Result<UserAddress> addAddress(@RequestBody UserAddress address) {
        UserAddress created = merchantService.addAddress(address);
        return Result.success(created);
    }

    /**
     * 根据ID查询收货地址
     * GET /api/merchant/address/{id}
     */
    @GetMapping("/address/{id}")
    public Result<UserAddress> getAddressById(@PathVariable Long id) {
        UserAddress address = merchantService.getAddressById(id);
        if (address == null) {
            return Result.fail("地址不存在");
        }
        return Result.success(address);
    }

    /**
     * 查询用户的所有收货地址
     * GET /api/merchant/address/user/{userId}
     */
    @GetMapping("/address/user/{userId}")
    public Result<List<UserAddress>> getAddressByUserId(@PathVariable Long userId) {
        List<UserAddress> list = merchantService.getAddressByUserId(userId);
        return Result.success(list);
    }

    /**
     * 查询用户的默认收货地址
     * GET /api/merchant/address/default/{userId}
     */
    @GetMapping("/address/default/{userId}")
    public Result<UserAddress> getDefaultAddress(@PathVariable Long userId) {
        UserAddress address = merchantService.getDefaultAddress(userId);
        if (address == null) {
            return Result.fail("未设置默认地址");
        }
        return Result.success(address);
    }

    /**
     * 更新收货地址
     * PUT /api/merchant/address/update
     */
    @PutMapping("/address/update")
    public Result<Boolean> updateAddress(@RequestBody UserAddress address) {
        boolean success = merchantService.updateAddress(address);
        if (!success) {
            return Result.fail("更新地址失败");
        }
        return Result.success(true);
    }

    /**
     * 设置默认地址
     * PUT /api/merchant/address/default?userId=1&addressId=2
     */
    @PutMapping("/address/default")
    public Result<Boolean> setDefaultAddress(
            @RequestParam Long userId,
            @RequestParam Long addressId) {
        boolean success = merchantService.setDefaultAddress(userId, addressId);
        if (!success) {
            return Result.fail("设置默认地址失败，地址不存在或不属于该用户");
        }
        return Result.success(true);
    }

    /**
     * 删除收货地址
     * DELETE /api/merchant/address/{id}
     */
    @DeleteMapping("/address/{id}")
    public Result<Boolean> deleteAddress(@PathVariable Long id) {
        boolean success = merchantService.deleteAddress(id);
        if (!success) {
            return Result.fail("删除地址失败");
        }
        return Result.success(true);
    }
}
