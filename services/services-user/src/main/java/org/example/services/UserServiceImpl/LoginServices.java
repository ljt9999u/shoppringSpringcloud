package org.example.services.UserServiceImpl;

import org.example.common.PageResult;
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
import java.util.List;
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
        // 安全：角色一律由服务端强制为普通用户，忽略客户端传入的 roleCode，防止越权注册管理员
        userPOJO.setRoleCode("USER");
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

    @Override
    public UserPOJO findUserById(Long id) {
        return userMapper.findById(id);
    }

    /**
     * 修改个人资料
     */
    @Override
    public Result<Boolean> updateProfile(UserPOJO userPOJO) {
        if (userPOJO.getId() == null) {
            return Result.fail("缺少用户ID");
        }
        UserPOJO dbUser = userMapper.findById(userPOJO.getId());
        if (dbUser == null) {
            return Result.fail("用户不存在");
        }
        int rows = userMapper.updateProfile(userPOJO);
        if (rows <= 0) {
            return Result.fail("修改资料失败");
        }
        return Result.success(true);
    }

    /**
     * 修改密码（校验旧密码）
     */
    @Override
    public Result<Boolean> updatePassword(Long userId, String oldPassword, String newPassword) {
        UserPOJO dbUser = userMapper.findById(userId);
        if (dbUser == null) {
            return Result.fail("用户不存在");
        }
        if (oldPassword == null || newPassword == null || newPassword.length() < 6) {
            return Result.fail("新密码不能为空且至少6位");
        }
        // 校验旧密码
        String md5Old = Md5Util.getMD5String(oldPassword);
        if (!md5Old.equals(dbUser.getPassword())) {
            return Result.fail("原密码不正确");
        }
        // 更新为新密码（MD5）
        int rows = userMapper.updatePassword(userId, Md5Util.getMD5String(newPassword));
        if (rows <= 0) {
            return Result.fail("修改密码失败");
        }
        return Result.success(true);
    }

    /**
     * 修改账号状态（管理端）
     */
    @Override
    public Result<Boolean> updateStatus(Long id, int status) {
        UserPOJO dbUser = userMapper.findById(id);
        if (dbUser == null) {
            return Result.fail("用户不存在");
        }
        int rows = userMapper.updateStatus(id, status);
        if (rows <= 0) {
            return Result.fail("修改状态失败");
        }
        return Result.success(true);
    }

    /**
     * 分页查询用户（管理端），返回数据脱敏
     */
    @Override
    public Result<PageResult<UserPOJO>> page(int pageNum, int pageSize) {
        if (pageNum < 1) pageNum = 1;
        if (pageSize < 1) pageSize = 10;
        if (pageSize > 100) pageSize = 100;
        int offset = (pageNum - 1) * pageSize;
        long total = userMapper.count();
        List<UserPOJO> list = userMapper.findPage(offset, pageSize);
        // 密码脱敏
        for (UserPOJO u : list) {
            u.setPassword(null);
        }
        return Result.success(new PageResult<>(total, pageNum, pageSize, list));
    }
}
