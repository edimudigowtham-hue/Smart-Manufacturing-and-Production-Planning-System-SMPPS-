package com.genc.smpps;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class ProductionOrderServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(ProductionOrderServiceApplication.class, args);
    }
}
