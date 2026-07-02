package com.genc.smpps;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class QualityServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(QualityServiceApplication.class, args);
    }
}
