package org.example.Feign;

import org.example.Feign.Fallback.UserFeignFallback;
import org.example.User.UserPOJO;
import org.example.common.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 用户服务 Feign 客户端
 * 商家服务通过此接口调用用户服务
 */
@FeignClient(value = "services-user", fallback = UserFeignFallback.class)
public interface UserFeign {

    /**
     * 根据ID查询用户（商家入驻时校验用户）
     * 远程对应：services-user → UserController#findById
     * 远程路径：GET /api/user/{id}
     */
    @GetMapping("/api/user/{id}")
    Result<UserPOJO> getUserById(@PathVariable("id") Long id);

    /**
     * 修改用户角色（商家入驻审核通过时升为 MERCHANT）
     * 远程对应：services-user → UserController#updateRole
     * 远程路径：PUT /api/user/role/{id}?roleCode=
     */
    @PutMapping("/api/user/role/{id}")
    Result<Boolean> updateUserRole(@PathVariable("id") Long id, @RequestParam("roleCode") String roleCode);
}
