package org.example;

import org.example.priperties.OrideProperties;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableFeignClients
@EnableDiscoveryClient
@SpringBootApplication
@EnableConfigurationProperties(OrideProperties.class)
@MapperScan("org.example.Mapper")
public class OrideMainApilication {
    public static void main(String[] args) {
        SpringApplication.run(OrideMainApilication.class,args);

    }
}
