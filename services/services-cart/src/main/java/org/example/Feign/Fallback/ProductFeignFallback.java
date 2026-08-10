package org.example.Feign.Fallback;

import org.example.Feign.ProductFeign;
import org.example.Product.Product;
import org.example.common.Result;
import org.springframework.stereotype.Component;

/**
 * 商品服务 Feign 降级处理
 */
@Component
public class ProductFeignFallback implements ProductFeign {

    @Override
    public Result<Product> getProductById(Long id) {
        return Result.fail("商品服务暂时不可用");
    }
}
