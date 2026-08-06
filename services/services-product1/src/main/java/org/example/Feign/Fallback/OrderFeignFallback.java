package org.example.Feign.Fallback;

import org.example.Feign.OrderFeign;
import org.example.common.Result;
import org.springframework.stereotype.Component;

/**
 * 订单服务 Feign 降级处理
 */
@Component
public class OrderFeignFallback implements OrderFeign {

    @Override
    public Result<Integer> getOrderCountByProduct(Long productId) {
        return Result.fail("订单服务暂时不可用，订单数获取失败");
    }

    @Override
    public Result<Integer> getCommentCount(Long productId) {
        return Result.fail("订单服务暂时不可用，评价数获取失败");
    }
}
