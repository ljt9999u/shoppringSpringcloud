package org.example.Feign;

import org.example.OrideMainApilication;
import org.example.Product.Product;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = OrideMainApilication.class)
public class ProductFeignTimeoutTest {

    @Autowired
    private ProductFeign productFeign;

    @Test
    public void testProductFeignTimeout() {
        try {
            System.out.println("开始调用 ProductFeign...");
            Product result = productFeign.getProductById(1L);
            System.out.println("调用成功，结果: " + result);
        } catch (Exception e) {
            System.out.println("调用失败，异常类型: " + e.getClass().getName());
            System.out.println("异常信息: " + e.getMessage());
        }
    }
}