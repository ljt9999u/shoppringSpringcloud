package org.example.Feign;

import org.example.Feign.Fllback.UserFeignFallback;
import org.example.User.UserPOJO;
import org.example.common.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 用户服务 Feign 客户端
 * 订单服务通过此接口调用用户服务
 */
@FeignClient(value = "services-user", fallback = UserFeignFallback.class)
public interface UserFeign {

    /**
     * 根据ID查询用户（下单时校验用户、查询时补充用户信息）
     * 远程对应：services-user → UserController#findById
     * 远程路径：GET /api/user/{id}
     */
    @GetMapping("/api/user/{id}")
    Result<UserPOJO> getUserById(@PathVariable("id") Long id);

    /**
     * 根据手机号查询用户
     * 远程对应：services-user → UserController#findByPhone
     * 远程路径：GET /api/user/findByPhone?phone=
     */
    @GetMapping("/api/user/findByPhone")
    Result<UserPOJO> getUserByPhone(@RequestParam("phone") String phone);
}
