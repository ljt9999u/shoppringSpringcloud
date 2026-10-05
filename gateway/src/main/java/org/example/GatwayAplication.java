package org.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@EnableDiscoveryClient
@SpringBootApplication
public class GatwayAplication {
    public static void main(String[] args) {
        SpringApplication.run(GatwayAplication.class, args);
//        System.out.println("max memory = " + Runtime.getRuntime().maxMemory()/1024/1024 + " MB");
    }

}
