package org.example.Feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

// 如果调用自己的业务api直接去controller把方法复制过来然后就配置即可
// 注意：本接口调用的是第三方高德地图 REST API（非本项目微服务），不存在对应的远程 Controller 方法
@FeignClient(value="gaode",url = "https://restapi.amap.com")
public interface GaoDefeign {
    /**
     * 高德步行路线规划（第三方接口，非本项目 Controller）
     * 远程地址：GET https://restapi.amap.com/v3/direction/walking
     */
    @GetMapping("/v3/direction/walking")
    String getgade(@RequestParam("key") String key,
                 @RequestParam("origin") String origin,
                 @RequestParam("destination") String destination);
}
