package org.example.Controller;

import org.example.common.Result;
import org.example.utils.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.example.User.UserPOJO;
import org.example.services.UserServices;
import org.example.vo.LoginVO;
import org.example.vo.RegisterVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 用户控制器 - 登录、注册、获取当前用户信息
 */
@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserServices userServices;

    /**
     * 健康检查 - 验证服务是否正常
     * GET /api/user/health
     */
    @GetMapping("/health")
    public String health() {
        return "OK - services-user is running";
    }

    /**
     * 用户注册
     * POST /api/user/register
     * body: { "username": "张三", "password": "123456", "phone": "13800138000", "roleCode": "USER" }
     */
    @PostMapping("/register")
    public Result<RegisterVO> register(@RequestBody UserPOJO userPOJO) {
        return userServices.register(userPOJO);
    }

    /**
     * 用户登录
     * POST /api/user/login
     * body: { "phone": "13800138000", "password": "123456" }
     */
    @PostMapping("/login")
    public Result<LoginVO> login(@RequestBody UserPOJO userPOJO) {
        return userServices.login(userPOJO);
    }

    /**
     * 根据手机号查询用户
     * GET /api/user/findByPhone?phone=13800138000
     */
    @GetMapping("/findByPhone")
    public Result<UserPOJO> findByPhone(@RequestParam String phone) {
        UserPOJO user = userServices.findUserByPhone(phone);
        if (user == null) {
            return Result.fail("用户不存在");
        }
        // 脱敏：清空密码
        user.setPassword(null);
        return Result.success(user);
    }

    /**
     * 获取当前登录用户信息（通过 JWT token 解析）
     * GET /api/user/info
     * Header: Authorization: Bearer xxx
     */
    @GetMapping("/info")
    public Result<Map<String, Object>> userInfo(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
            try {
                Map<String, Object> claims = JwtUtil.parseToken(token);
                return Result.success(claims);
            } catch (Exception e) {
                return Result.fail(401, "token 无效或已过期");
            }
        }
        return Result.fail(401, "未登录");
    }
}
