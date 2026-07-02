package com.genc.smpps.client;

import com.genc.smpps.dto.ProductionOrderDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "production-order-service")
public interface ProductionOrderClient {
    @GetMapping("/api/orders/{id}")
    ProductionOrderDto getOrder(@PathVariable Integer id);
}
