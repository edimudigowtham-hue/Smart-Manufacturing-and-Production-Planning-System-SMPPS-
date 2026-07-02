package com.genc.smpps.gateway.controller;

import com.genc.smpps.gateway.security.JwtService;
import com.genc.smpps.gateway.user.GatewayUserService;
import com.genc.smpps.gateway.user.LoginRequest;
import java.security.Principal;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private static final Map<String, String> ROLE_DISPLAY_NAMES = Map.of(
            "ADMIN", "Admin",
            "PRODUCTION_PLANNER", "Production Planner",
            "SHOP_FLOOR_SUPERVISOR", "Shop Floor Supervisor",
            "QUALITY_INSPECTOR", "Quality Inspector",
            "MAINTENANCE_ENGINEER", "Maintenance Engineer"
    );

    private final GatewayUserService userService;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthController(GatewayUserService userService, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @PostMapping("/login")
    public Mono<ResponseEntity<Map<String, Object>>> login(@RequestBody LoginRequest request) {
        String rawPassword = request.getPassword() == null ? "" : request.getPassword();

        return userService.findByUsername(request.getUsername())
                .cast(UserDetails.class)
                .flatMap(user -> {
                    if (!user.isEnabled() || !passwordEncoder.matches(rawPassword, user.getPassword())) {
                        return Mono.just(invalidCredentials());
                    }

                    List<String> roles = rolesFromAuthorities(user.getAuthorities());
                    return Mono.just(ResponseEntity.ok(Map.<String, Object>of(
                            "token", jwtService.generateToken(user),
                            "tokenType", "Bearer",
                            "username", user.getUsername(),
                            "roles", roles,
                            "displayRoles", displayRoles(roles)
                    )));
                })
                .onErrorReturn(invalidCredentials());
    }

    @GetMapping("/me")
    public Map<String, Object> me(Principal principal) {
        Authentication authentication = (Authentication) principal;
        List<String> roles = rolesFromAuthorities(authentication.getAuthorities());
        return Map.of(
                "username", authentication.getName(),
                "roles", roles,
                "displayRoles", displayRoles(roles)
        );
    }

    private ResponseEntity<Map<String, Object>> invalidCredentials() {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.<String, Object>of("message", "Invalid username or password"));
    }

    private List<String> rolesFromAuthorities(Collection<? extends GrantedAuthority> authorities) {
        return authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .map(authority -> authority.replaceFirst("^ROLE_", ""))
                .sorted(Comparator.naturalOrder())
                .toList();
    }

    private List<String> displayRoles(List<String> roles) {
        return roles.stream().map(role -> ROLE_DISPLAY_NAMES.getOrDefault(role, role)).toList();
    }
}
