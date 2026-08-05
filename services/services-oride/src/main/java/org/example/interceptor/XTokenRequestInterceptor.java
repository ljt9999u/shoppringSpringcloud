package org.example.interceptor;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;

//拦截器
@Component
public class XTokenRequestInterceptor implements RequestInterceptor {
    //请求拦截器 requestTemplate请求模板
    @Override
    public void apply(RequestTemplate requestTemplate) {
        requestTemplate.header("X-Token", UUID.randomUUID().toString());
    }
}
