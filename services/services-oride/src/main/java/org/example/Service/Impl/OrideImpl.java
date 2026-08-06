package org.example.Service.Impl;

import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import lombok.extern.slf4j.Slf4j;
import org.example.Feign.ProductFeign;
import org.example.Oride.OridePOJO;
import org.example.Product.Product;
import org.example.Service.Orideservice;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Service
public class OrideImpl implements Orideservice {

    @Autowired
    LoadBalancerClient loadBalancerClient;
    @Autowired
    RestTemplate restemplate;
    @Autowired
    DiscoveryClient discoveryClient;
    @Autowired
    ProductFeign productFeign;
    @SentinelResource(value = "createOride",blockHandler= "createOrideFllback")//@SentinelResource注解用于在非Controller类中定义资源
    @Override
    public OridePOJO createOride(Long productId, Long userId) {
//        Product product = getProductFromRemoteWithLoadBalancezhujie(productId);
        //使用feign完成远程调用
        Product product = productFeign.getProductById(productId);
        OridePOJO oridePOJO = new OridePOJO();
        oridePOJO.setId(productId);
        //总金额
        oridePOJO.setTotalAmount(product.getPrice().multiply(new BigDecimal(product.getStock())));
        oridePOJO.setUserId(userId);
        oridePOJO.setNickname("张三");
        oridePOJO.setAddress("北京市朝阳区");
        //远程查询商品列表
        oridePOJO.setProductList(Arrays.asList(product));

        return oridePOJO;
    }

    //Sentinel blockHandler 方法 - 兜底回调
    public OridePOJO createOrideFllback(Long productId, Long userId, BlockException e){
       OridePOJO oridePOJO = new OridePOJO();
       oridePOJO.setId(0L);
       oridePOJO.setTotalAmount(new BigDecimal(0));
       oridePOJO.setUserId(userId);
       oridePOJO.setNickname("未知");
       oridePOJO.setAddress("异常信息"+e.getClass());
       return oridePOJO;
    }


    @Override
    public Product getProductFromRemote(Long productId) {
        List<ServiceInstance> instances = discoveryClient.getInstances("services-product1");
        if (instances == null || instances.isEmpty()) {
            throw new RuntimeException("商品服务未找到");
        }
        ServiceInstance instance = instances.get(0);
        String url = "http://"+instance.getHost()+":"+instance.getPort()+"/product/"+productId;
        log.info("远程调用商品服务，url:{}",url);
        return restemplate.getForObject(url,Product.class);
    }

    //负载均衡
    private Product getProductFromRemoteWithLoadBalance(Long productId) {
        //1.获取所有商品列表服务器和端口
       ServiceInstance choose = loadBalancerClient.choose("services-product1");
        String url = "http://"+choose.getHost()+":"+choose.getPort()+"/product/"+productId;
        log.info("远程调用商品服务，url:{}",url);
        Product product = restemplate.getForObject(url,Product.class);
        return product;
    }

    //基于注解负载均衡
    private Product getProductFromRemoteWithLoadBalancezhujie(Long productId) {
        String url = "http://services-product1/product/"+productId;
        log.info("远程调用商品服务，url:{}",url);
        //发送请求，services-product1会被动态替换
        Product product = restemplate.getForObject(url,Product.class);
        return product;
    }

    // Sentinel fallback 方法 - 兜底回调
    public OridePOJO createOrideFallback(Long productId, Long userId) {
        log.warn("-----Sentinel兜底回调---");
        OridePOJO oridePOJO = new OridePOJO();
        oridePOJO.setId(productId);
        oridePOJO.setTotalAmount(BigDecimal.ZERO);
        oridePOJO.setUserId(userId);
        oridePOJO.setNickname("张三");
        oridePOJO.setAddress("北京市朝阳区");
        oridePOJO.setProductList(null);
        return oridePOJO;
    }

    @Override
    public int getOrderCountByProduct(Long productId) {
        // 暂时返回模拟数据
        return 50 + (int)(productId % 30);
    }

    @Override
    public int getCommentCount(Long productId) {
        // 暂时返回模拟数据
        return 20 + (int)(productId % 15);
    }
}