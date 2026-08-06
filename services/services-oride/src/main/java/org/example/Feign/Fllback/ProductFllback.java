package org.example.Feign.Fllback;


import org.example.Feign.ProductFeign;
import org.example.Product.Product;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

//商品服务熔断
public class ProductFllback implements ProductFeign {

    @Override
    public Product getProductById(Long id) {
        System.out.println("-----兜底回调---");
        Product product = new Product();
        product.setId(id);
        product.setName("商品服务已关闭");
        product.setPrice(BigDecimal.valueOf(0));
        product.setStock(0);
        return null;
    }
}
