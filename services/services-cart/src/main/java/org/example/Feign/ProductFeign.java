package org.example.Feign;

import org.example.Feign.Fallback.ProductFeignFallback;
import org.example.Product.Product;
import org.example.common.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * 商品服务 Feign 客户端
 * 购物车服务通过此接口调用商品服务，获取商品信息及校验库存
 */
@FeignClient(value = "services-product1", fallback = ProductFeignFallback.class)
public interface ProductFeign {

    /**
     * 根据ID查询商品（加购/查询购物车时获取商品信息与库存）
     */
    @GetMapping("/api/product/{id}")
    Result<Product> getProductById(@PathVariable("id") Long id);
}
