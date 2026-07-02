package com.genc.smpps.gateway.controller;

import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {
    private final WebClient webClient = WebClient.create();

    @GetMapping
    public Mono<Map<String, Integer>> dashboard() {
        Mono<Integer> products = count("http://localhost:8101/api/products");
        Mono<Integer> orders = count("http://localhost:8102/api/orders");
        Mono<Integer> machines = count("http://localhost:8103/api/machines/logs");
        Mono<Integer> quality = count("http://localhost:8104/api/quality/inspections");
        Mono<Integer> maintenance = count("http://localhost:8105/api/maintenance/work-orders");

        return Mono.zip(products, orders, machines, quality, maintenance)
                .map(tuple -> Map.of(
                        "productCount", tuple.getT1(),
                        "orderCount", tuple.getT2(),
                        "machineCount", tuple.getT3(),
                        "qualityCount", tuple.getT4(),
                        "maintenanceCount", tuple.getT5()
                ));
    }

    private Mono<Integer> count(String url) {
        return webClient.get().uri(url).retrieve()
                .bodyToMono(List.class)
                .map(List::size)
                .onErrorReturn(0);
    }
}
