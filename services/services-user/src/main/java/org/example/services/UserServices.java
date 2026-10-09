package org.example.services;

import org.example.common.PageResult;
import org.example.common.Result;
import org.example.User.UserPOJO;
import org.example.vo.LoginVO;
import org.example.vo.RegisterVO;

public interface UserServices {

    /**
     * 注册
     */
    Result<RegisterVO> register(UserPOJO userPOJO);

    /**
     * 登录
     */
    Result<LoginVO> login(UserPOJO userPOJO);

    /**
     * 根据手机号查询用户
     */
    UserPOJO findUserByPhone(String phone);

    /**
     * 根据ID查询用户
     */
    UserPOJO findUserById(Long id);

    /**
     * 修改个人资料（昵称、邮箱、头像、性别）
     */
    Result<Boolean> updateProfile(UserPOJO userPOJO);

    /**
     * 修改密码（需校验旧密码）
     */
    Result<Boolean> updatePassword(Long userId, String oldPassword, String newPassword);

    /**
     * 修改账号状态（管理端：0禁用 1启用）
     */
    Result<Boolean> updateStatus(Long id, int status);

    /**
     * 修改用户角色（USER/MERCHANT/ADMIN，商家入驻审核通过时调用）
     */
    Result<Boolean> updateRole(Long id, String roleCode);

    /**
     * 分页查询用户（管理端）
     */
    Result<PageResult<UserPOJO>> page(int pageNum, int pageSize);
}
