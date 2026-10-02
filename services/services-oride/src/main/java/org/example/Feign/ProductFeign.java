package org.example.Feign;

import org.example.Feign.Fllback.ProductFllback;
import org.example.Product.Product;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 商品服务 Feign 客户端
 * 订单服务通过此接口调用商品服务
 */
@FeignClient(value = "services-product1", fallback = ProductFllback.class)
public interface ProductFeign {

    /**
     * 根据ID查询商品（下单时获取商品信息）
     * 远程对应：services-product1 → ProductContoller#getById
     * 远程路径：GET /api/product/{id}
     */
    @GetMapping("/api/product/{id}")
    Product getProductById(@PathVariable("id") Long id);

    /**
     * 扣减库存（下单时扣减商品库存）
     * 远程对应：services-product1 → ProductContoller#reduceStock
     * 远程路径：POST /api/product/reduceStock?id=&quantity=
     */
    @PostMapping("/api/product/reduceStock")
    boolean reduceStock(@RequestParam("id") Long id, @RequestParam("quantity") int quantity);
}
