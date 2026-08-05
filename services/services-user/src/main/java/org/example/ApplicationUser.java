package org.example;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;


@SpringBootApplication
@EnableDiscoveryClient
@MapperScan("org.example.Mapper")  // 扫描 Mapper 接口
public class ApplicationUser {
    public static void main(String[] args) {
        SpringApplication.run(ApplicationUser.class, args);
    }
}
