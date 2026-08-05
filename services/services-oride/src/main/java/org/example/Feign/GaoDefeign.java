package org.example.Feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

//如果调用自己的业务api直接去controller把方法复制过来然后就配置即可
@FeignClient(value="gaode",url = "https://restapi.amap.com")
public interface GaoDefeign {
    @GetMapping("/v3/direction/walking")
    String getgade(@RequestParam("key") String key,
                 @RequestParam("origin") String origin,
                 @RequestParam("destination") String destination);
}
