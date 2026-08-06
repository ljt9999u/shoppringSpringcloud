package org.example.services;

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
}
