package org.example.Controller;
import com.alibaba.csp.sentinel.annotation.SentinelResource;
import lombok.extern.slf4j.Slf4j;
import org.example.Oride.OridePOJO;
import org.example.Service.Impl.OrideImpl;
import org.example.common.Result;
import org.example.priperties.OrideProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;


@RequestMapping("/api/oride")
@Slf4j
@RefreshScope
@RestController
@Controller
public class OrideController {
    @Autowired
    OrideImpl orideImpl;

    @Autowired
    OrideProperties orideProperties;



    @GetMapping("/config")
    public String config(){
        return "oride.timeout: " + orideProperties.getTimeout() + ", oride.auto-confirm: " + orideProperties.getAutoConfirm();
    }
    //创建订单
    @SentinelResource(value = "createOride")
    @GetMapping("/create")
    public OridePOJO createOride(@RequestParam("userId") Long userId,
                                 @RequestParam("productId") Long productId){
        OridePOJO oride = orideImpl.createOride(1L,1L);
        return  oride;
    }
    @SentinelResource(value = "seckill",fallback = "seckillFllback")
    @GetMapping("/seckill")
    public OridePOJO seckill(@RequestParam("userId") Long userId,
                                 @RequestParam("productId") Long productId){
        OridePOJO oride = orideImpl.createOride(1L,1L);
        oride.setId(Long.MAX_VALUE);
        return  oride;
    }

    public OridePOJO seckillFllback( Long userId, Long productId){
       OridePOJO oride = new OridePOJO();
        oride.setId(Long.MAX_VALUE);
        oride.setTotalAmount(new BigDecimal(0));
        oride.setUserId(userId);
        oride.setNickname("未知");
        oride.setAddress("异常信息");
        oride.setProductList(null);
        return  oride;
    }


    @GetMapping("/redDB")
    public String redDB(){
        log.info("redDB");
        return "redDB";
    }

    // ========== 供其他服务调用的接口 ==========

    /**
     * 查询商品订单数（供商品服务 Feign 调用）
     * GET /api/oride/count?productId=1
     */
    @GetMapping("/count")
    public Result<Integer> getOrderCount(@RequestParam("productId") Long productId) {
        int count = orideImpl.getOrderCountByProduct(productId);
        return Result.success(count);
    }

    /**
     * 查询商品评价数（供商品服务 Feign 调用）
     * GET /api/oride/commentCount?productId=1
     */
    @GetMapping("/commentCount")
    public Result<Integer> getCommentCount(@RequestParam("productId") Long productId) {
        int count = orideImpl.getCommentCount(productId);
        return Result.success(count);
    }

}
