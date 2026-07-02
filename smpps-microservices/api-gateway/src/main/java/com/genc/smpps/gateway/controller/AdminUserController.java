package com.genc.smpps.gateway.controller;

import com.genc.smpps.gateway.user.GatewayUserService;
import com.genc.smpps.gateway.user.CreateUserRequest;
import com.genc.smpps.gateway.user.UserAccount;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/admin/users")
public class AdminUserController {
    private final GatewayUserService userService;

    public AdminUserController(GatewayUserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public Map<String, Object> users() {
        return Map.of("users", userService.listUsers());
    }

    @GetMapping("/roles")
    public Map<String, Object> roles() {
        return Map.of("roles", userService.allowedRoles());
    }

    @PostMapping
    public Mono<ResponseEntity<Map<String, Object>>> createUser(@RequestBody CreateUserRequest request) {
        return Mono.fromCallable(() -> {
            UserAccount account = userService.createUser(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.<String, Object>of(
                    "message", "User created successfully",
                    "username", account.getUsername(),
                    "role", account.getRole()
            ));
        }).onErrorResume(IllegalArgumentException.class, ex -> Mono.just(
                ResponseEntity.badRequest().body(Map.<String, Object>of("message", ex.getMessage()))
        ));
    }
}



