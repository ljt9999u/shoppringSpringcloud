package org.example.services.UserServiceImpl;

import org.example.common.Result;
import org.example.utils.JwtUtil;
import org.example.utils.Md5Util;
import org.example.Mapper.UserMapper;
import org.example.User.UserPOJO;
import org.example.services.UserServices;
import org.example.vo.LoginVO;
import org.example.vo.RegisterVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * 用户服务实现类
 */
@Service
public class LoginServices implements UserServices {

    @Autowired
    private UserMapper userMapper;

    /**
     * 注册
     */
    @Override
    public Result<RegisterVO> register(UserPOJO userPOJO) {
        // 1. 校验手机号是否已注册
        UserPOJO existingUser = userMapper.findByPhone(userPOJO.getPhone());
        if (existingUser != null) {
            return Result.fail("该手机号已注册");
        }

        // 2. 校验用户名是否已存在
        UserPOJO existUser = userMapper.findByUsername(userPOJO.getUsername());
        if (existUser != null) {
            return Result.fail("用户名已存在");
        }

        // 3. 密码 MD5 加密
        String md5Password = Md5Util.getMD5String(userPOJO.getPassword());

        // 4. 设置默认值
        if (userPOJO.getRoleCode() == null || userPOJO.getRoleCode().isEmpty()) {
            userPOJO.setRoleCode("USER");  // 默认普通用户
        }
        userPOJO.setPassword(md5Password);
        userPOJO.setStatus(1);  // 默认启用

        // 5. 写入数据库
        int rows = userMapper.insert(userPOJO);
        if (rows <= 0) {
            return Result.fail("注册失败");
        }

        // 6. 封装 VO 返回
        RegisterVO vo = new RegisterVO();
        vo.setUserId(userPOJO.getId());
        vo.setUsername(userPOJO.getUsername());
        vo.setPhone(userPOJO.getPhone());
        vo.setRoleCode(userPOJO.getRoleCode());

        return Result.success(vo);
    }

    /**
     * 登录
     */
    @Override
    public Result<LoginVO> login(UserPOJO userPOJO) {
        // 1. 根据手机号查询用户
        UserPOJO dbUser = userMapper.findByPhone(userPOJO.getPhone());
        if (dbUser == null) {
            return Result.fail("用户不存在");
        }

        // 2. 校验账号状态
        if (dbUser.getStatus() != null && dbUser.getStatus() == 0) {
            return Result.fail("账号已被禁用");
        }

        // 3. 校验密码 (MD5)
        String inputPwd = userPOJO.getPassword();
        String md5Input = Md5Util.getMD5String(inputPwd);
        if (!md5Input.equals(dbUser.getPassword())) {
            return Result.fail("手机号或密码错误");
        }

        // 4. 生成 JWT token
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", dbUser.getId());
        claims.put("username", dbUser.getUsername());
        claims.put("phone", dbUser.getPhone());
        claims.put("roleCode", dbUser.getRoleCode());
        String token = JwtUtil.genToken(claims);

        // 5. 封装 VO 返回
        LoginVO vo = new LoginVO();
        vo.setToken(token);
        vo.setUserId(dbUser.getId());
        vo.setUsername(dbUser.getUsername());
        vo.setPhone(dbUser.getPhone());
        vo.setNickname(dbUser.getNickname());
        vo.setAvatar(dbUser.getAvatar());
        vo.setRoleCode(dbUser.getRoleCode());

        return Result.success(vo);
    }

    @Override
    public UserPOJO findUserByPhone(String phone) {
        return userMapper.findByPhone(phone);
    }
}
