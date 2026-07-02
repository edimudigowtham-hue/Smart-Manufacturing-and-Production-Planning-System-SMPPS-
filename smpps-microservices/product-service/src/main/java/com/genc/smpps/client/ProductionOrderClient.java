package com.genc.smpps.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "production-order-service")
public interface ProductionOrderClient {
    @GetMapping("/api/orders/product/{productId}/exists")
    boolean existsByProduct(@PathVariable Integer productId);
}
