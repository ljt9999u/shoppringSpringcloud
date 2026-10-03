package org.example.Controller;

import org.example.common.Result;
import org.example.utils.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.example.User.UserPOJO;
import org.example.common.PageResult;
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
     * body: { "username": "张三", "password": "123456", "phone": "13800138000" }
     * 注意：角色由服务端强制分配为 USER，请求体传 roleCode 无效
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
     * 根据ID查询用户（供其他服务 Feign 调用）
     * GET /api/user/{id}
     */
    @GetMapping("/{id}")
    public Result<UserPOJO> findById(@PathVariable Long id) {
        UserPOJO user = userServices.findUserById(id);
        if (user == null) {
            return Result.fail("用户不存在");
        }
        // 脱敏：清空密码
        user.setPassword(null);
        return Result.success(user);
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

    /**
     * 修改个人资料（昵称、邮箱、头像、性别）
     * PUT /api/user/update
     * 登录用户ID取自网关透传头 X-User-Id，防止越权改他人资料
     */
    @PutMapping("/update")
    public Result<Boolean> updateProfile(@RequestBody UserPOJO userPOJO,
                                         HttpServletRequest request) {
        Long currentUserId = resolveUserId(request);
        if (currentUserId != null) {
            userPOJO.setId(currentUserId);
        }
        return userServices.updateProfile(userPOJO);
    }

    /**
     * 修改密码
     * PUT /api/user/password?oldPassword=xxx&newPassword=yyy
     * 登录用户ID取自网关透传头 X-User-Id
     */
    @PutMapping("/password")
    public Result<Boolean> updatePassword(@RequestParam String oldPassword,
                                          @RequestParam String newPassword,
                                          HttpServletRequest request) {
        Long currentUserId = resolveUserId(request);
        if (currentUserId == null) {
            return Result.fail(401, "未登录，无法修改密码");
        }
        return userServices.updatePassword(currentUserId, oldPassword, newPassword);
    }

    /**
     * 分页查询用户列表（管理端）
     * GET /api/user/page?pageNum=1&pageSize=10
     */
    @GetMapping("/page")
    public Result<PageResult<UserPOJO>> page(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        return userServices.page(pageNum, pageSize);
    }

    /**
     * 修改账号状态（管理端：0禁用 1启用）
     * PUT /api/user/status/{id}?status=0
     */
    @PutMapping("/status/{id}")
    public Result<Boolean> updateStatus(@PathVariable Long id, @RequestParam int status) {
        return userServices.updateStatus(id, status);
    }

    /**
     * 从网关透传头 X-User-Id 解析当前登录用户ID（直连服务时该头不存在，返回 null）
     */
    private Long resolveUserId(HttpServletRequest request) {
        String userId = request.getHeader("X-User-Id");
        if (userId != null && !userId.isEmpty()) {
            try {
                return Long.valueOf(userId);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }
}
