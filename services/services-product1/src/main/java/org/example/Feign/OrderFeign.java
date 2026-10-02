package org.example.Feign;

import org.example.Feign.Fallback.OrderFeignFallback;
import org.example.common.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 订单服务 Feign 客户端
 * 商品服务通过此接口调用订单服务
 */
@FeignClient(value = "services-oride", fallback = OrderFeignFallback.class)
public interface OrderFeign {

    /**
     * 获取商品的订单数量（商品表中没有这个数据，需要从订单服务统计）
     * 远程对应：services-oride → OrideController#getOrderCount
     * 远程路径：GET /api/oride/count?productId=
     */
    @GetMapping("/api/oride/count")
    Result<Integer> getOrderCountByProduct(@RequestParam("productId") Long productId);

    /**
     * 获取商品评价数（商品表中没有这个数据，需要从订单服务统计）
     * 远程对应：services-oride → OrideController#getCommentCount
     * 远程路径：GET /api/oride/commentCount?productId=
     */
    @GetMapping("/api/oride/commentCount")
    Result<Integer> getCommentCount(@RequestParam("productId") Long productId);
}
