package org.example.Mapper;

import org.apache.ibatis.annotations.*;
import org.example.Merchant.Merchant;
import org.example.Merchant.UserAddress;

import java.util.List;

/**
 * 商家 Mapper 接口
 */
@Mapper
public interface MerchantMapper {

    // ========== 商家 CRUD ==========

    /**
     * 商家入驻申请（创建商家记录）
     */
    @Insert("INSERT INTO merchant(user_id, shop_name, shop_logo, business_license, license_image, contact_phone, status) " +
            "VALUES(#{userId}, #{shopName}, #{shopLogo}, #{businessLicense}, #{licenseImage}, #{contactPhone}, #{status})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertMerchant(Merchant merchant);

    /**
     * 根据ID查询商家
     */
    @Select("SELECT * FROM merchant WHERE id = #{id}")
    Merchant findById(Long id);

    /**
     * 根据用户ID查询商家
     */
    @Select("SELECT * FROM merchant WHERE user_id = #{userId}")
    Merchant findByUserId(Long userId);

    /**
     * 分页查询所有商家
     */
    @Select("SELECT * FROM merchant ORDER BY create_time DESC LIMIT #{offset}, #{pageSize}")
    List<Merchant> findPage(@Param("offset") int offset, @Param("pageSize") int pageSize);

    /**
     * 统计商家总数
     */
    @Select("SELECT COUNT(*) FROM merchant")
    long countAll();

    /**
     * 按状态分页查询商家
     */
    @Select("SELECT * FROM merchant WHERE status = #{status} ORDER BY create_time DESC LIMIT #{offset}, #{pageSize}")
    List<Merchant> findPageByStatus(@Param("status") int status, @Param("offset") int offset, @Param("pageSize") int pageSize);

    /**
     * 按状态统计商家
     */
    @Select("SELECT COUNT(*) FROM merchant WHERE status = #{status}")
    long countByStatus(int status);

    /**
     * 更新商家信息
     */
    @Update("UPDATE merchant SET shop_name = #{shopName}, shop_logo = #{shopLogo}, " +
            "business_license = #{businessLicense}, license_image = #{licenseImage}, " +
            "contact_phone = #{contactPhone} WHERE id = #{id}")
    int updateMerchant(Merchant merchant);

    /**
     * 审核商家（更新审核状态）
     * status: 0待审核 1已通过 2已拒绝
     */
    @Update("UPDATE merchant SET status = #{status} WHERE id = #{id}")
    int updateStatus(@Param("id") Long id, @Param("status") int status);

    /**
     * 删除商家
     */
    @Delete("DELETE FROM merchant WHERE id = #{id}")
    int deleteById(Long id);

    // ========== 收货地址 CRUD ==========

    /**
     * 新增收货地址
     */
    @Insert("INSERT INTO user_address(user_id, consignee, phone, province, city, district, detail, is_default) " +
            "VALUES(#{userId}, #{consignee}, #{phone}, #{province}, #{city}, #{district}, #{detail}, #{isDefault})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertAddress(UserAddress address);

    /**
     * 根据ID查询收货地址
     */
    @Select("SELECT * FROM user_address WHERE id = #{id}")
    UserAddress findAddressById(Long id);

    /**
     * 查询用户的所有收货地址
     */
    @Select("SELECT * FROM user_address WHERE user_id = #{userId} ORDER BY is_default DESC, create_time DESC")
    List<UserAddress> findAddressByUserId(Long userId);

    /**
     * 查询用户的默认收货地址
     */
    @Select("SELECT * FROM user_address WHERE user_id = #{userId} AND is_default = 1 LIMIT 1")
    UserAddress findDefaultAddress(Long userId);

    /**
     * 更新收货地址
     */
    @Update("UPDATE user_address SET consignee = #{consignee}, phone = #{phone}, " +
            "province = #{province}, city = #{city}, district = #{district}, detail = #{detail} " +
            "WHERE id = #{id}")
    int updateAddress(UserAddress address);

    /**
     * 设置默认地址（先清除用户所有默认，再设置当前为默认）
     */
    @Update("UPDATE user_address SET is_default = 0 WHERE user_id = #{userId}")
    int clearDefault(Long userId);

    @Update("UPDATE user_address SET is_default = 1 WHERE id = #{id}")
    int setDefault(Long id);

    /**
     * 删除收货地址
     */
    @Delete("DELETE FROM user_address WHERE id = #{id}")
    int deleteAddress(Long id);
}
