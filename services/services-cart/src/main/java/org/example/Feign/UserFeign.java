package org.example.Feign;

import org.example.Feign.Fallback.UserFeignFallback;
import org.example.User.UserPOJO;
import org.example.common.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * 用户服务 Feign 客户端
 * 购物车服务通过此接口调用用户服务，校验用户是否存在
 */
@FeignClient(value = "services-user", fallback = UserFeignFallback.class)
public interface UserFeign {

    /**
     * 根据ID查询用户（加购时校验用户）
     */
    @GetMapping("/api/user/{id}")
    Result<UserPOJO> getUserById(@PathVariable("id") Long id);
}
