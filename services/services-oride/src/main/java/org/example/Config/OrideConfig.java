package org.example.config;

import feign.Retryer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OrideConfig {
    //Feign重试(重复发送请求配置)
    @Bean
    Retryer retryer(){
        return new Retryer.Default(100, 1000, 3);
    }

}
