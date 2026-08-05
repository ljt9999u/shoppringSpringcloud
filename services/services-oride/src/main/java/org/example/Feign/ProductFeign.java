package org.example.Feign;

import com.alibaba.csp.sentinel.annotation.SentinelResource;
import org.example.Feign.Fllback.ProductFllback;
import org.example.Product.Product;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(value = "services-product1",fallback = ProductFllback.class)
public interface ProductFeign {
    //Controller接收请求，Feign发送请求
    @GetMapping("/api/product/product/{id}")
    Product getProductById(@PathVariable("id") Long id);
}
