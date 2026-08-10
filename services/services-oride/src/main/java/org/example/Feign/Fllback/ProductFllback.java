package org.example.Feign.Fllback;

import org.example.Feign.ProductFeign;
import org.example.Product.Product;
import org.springframework.stereotype.Component;

/**
 * 商品服务 Feign 降级处理
 */
@Component
public class ProductFllback implements ProductFeign {

    @Override
    public Product getProductById(Long id) {
        Product product = new Product();
        product.setId(id);
        product.setName("【商品服务不可用】");
        product.setPrice(java.math.BigDecimal.ZERO);
        product.setStock(0);
        return product;
    }

    @Override
    public boolean reduceStock(Long id, int quantity) {
        return false;
    }
}
