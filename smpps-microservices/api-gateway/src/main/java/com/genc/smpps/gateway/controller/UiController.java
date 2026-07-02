package com.genc.smpps.gateway.controller;

import java.net.URI;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import reactor.core.publisher.Mono;

@Controller
public class UiController {

    @GetMapping({"/", "/dashboard", "/products", "/orders", "/machines", "/quality", "/maintenance"})
    public Mono<ResponseEntity<Resource>> index() {
        return html("static/index.html");
    }

    @GetMapping("/login")
    public Mono<ResponseEntity<Resource>> login() {
        return html("static/login.html");
    }

    @GetMapping("/register")
    public Mono<ResponseEntity<Void>> register() {
        return Mono.just(ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create("/login?registration-disabled"))
                .build());
    }

    private Mono<ResponseEntity<Resource>> html(String path) {
        return Mono.just(ResponseEntity.ok()
                .contentType(MediaType.TEXT_HTML)
                .body(new ClassPathResource(path)));
    }
}


