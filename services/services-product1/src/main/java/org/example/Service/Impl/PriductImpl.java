package org.example.Service.Impl;

import org.example.Service.ProductService;
import org.example.Product.Product;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.concurrent.TimeUnit;

@Service
public class PriductImpl implements ProductService {
    @Override
    public Product getProduct(Long productId) {
       Product  product =new Product();
       product.setId( productId);
       product.setProductName("手机");
       product.setPrice(BigDecimal.valueOf(10));
       product.setNum(3);
//       try {
//           //模拟耗时操作
//           TimeUnit.SECONDS.sleep(100);
//       }catch (Exception e){
//       throw new RuntimeException(e);
//       }
        return product;
    }
}
