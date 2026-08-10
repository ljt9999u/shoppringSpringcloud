package org.example.Feign.Fllback;

import org.example.Feign.UserFeign;
import org.example.User.UserPOJO;
import org.example.common.Result;
import org.springframework.stereotype.Component;

/**
 * 用户服务 Feign 降级处理
 */
@Component
public class UserFeignFallback implements UserFeign {

    @Override
    public Result<UserPOJO> getUserById(Long id) {
        return Result.fail("用户服务暂时不可用");
    }

    @Override
    public Result<UserPOJO> getUserByPhone(String phone) {
        return Result.fail("用户服务暂时不可用");
    }
}
