package org.example.Feign;

import org.example.OrideMainApilication;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = OrideMainApilication.class)
public class GaoDeFeignTest {

    @Autowired
    private GaoDefeign gaoDefeign;

    @Test
    public void testWalkRoute() {
        String key = "44507929acdb22481fef1c9e0c52c16e";
        String origin = "117.500244,40.417801";
        String destination = "117.501234,40.418901";
        
        String result = gaoDefeign.getgade(key, origin, destination);
        
        assertNotNull(result, "返回结果不应为空");
        assertTrue(result.contains("status"), "返回结果应包含 status 字段");
        System.out.println("高德地图步行路线查询结果:");
        System.out.println(result);
    }

    @Test
    public void testFeignClientInjected() {
        assertNotNull(gaoDefeign, "GaoDefeign Feign Client 应成功注入");
    }
}