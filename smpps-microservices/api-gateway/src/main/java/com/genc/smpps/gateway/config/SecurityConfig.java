package com.genc.smpps.gateway.config;

import com.genc.smpps.gateway.security.JwtAuthenticationWebFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.HttpStatusServerEntryPoint;
import org.springframework.security.web.server.authorization.HttpStatusServerAccessDeniedHandler;
import org.springframework.security.web.server.context.NoOpServerSecurityContextRepository;
import org.springframework.security.web.server.savedrequest.NoOpServerRequestCache;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {
    private static final String ADMIN = "ADMIN";
    private static final String PRODUCTION_PLANNER = "PRODUCTION_PLANNER";
    private static final String SHOP_FLOOR_SUPERVISOR = "SHOP_FLOOR_SUPERVISOR";
    private static final String QUALITY_INSPECTOR = "QUALITY_INSPECTOR";
    private static final String MAINTENANCE_ENGINEER = "MAINTENANCE_ENGINEER";
    private static final String LOGIN_PATH = "/login";
    private static final String LOGIN_HTML_PATH = "/login.html";

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http, JwtAuthenticationWebFilter jwtAuthenticationWebFilter) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
                .formLogin(ServerHttpSecurity.FormLoginSpec::disable)
                .logout(ServerHttpSecurity.LogoutSpec::disable)
                .securityContextRepository(NoOpServerSecurityContextRepository.getInstance())
                .requestCache(cache -> cache.requestCache(NoOpServerRequestCache.getInstance()))
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(new HttpStatusServerEntryPoint(HttpStatus.UNAUTHORIZED))
                        .accessDeniedHandler(new HttpStatusServerAccessDeniedHandler(HttpStatus.FORBIDDEN))
                )
                .authorizeExchange(auth -> auth
                        .pathMatchers("/register.html", "/api/auth/register", "/api/auth/roles").denyAll()
                        .pathMatchers(LOGIN_HTML_PATH, LOGIN_PATH, "/api/auth/login", "/register", "/css/**", "/js/**", "/partials/**", "/favicon.ico").permitAll()
                        .pathMatchers("/api/auth/me", "/api/dashboard").authenticated()
                        .pathMatchers("/api/admin/users", "/api/admin/users/**").hasRole(ADMIN)
                        .pathMatchers(HttpMethod.GET, "/api/products/**").hasAnyRole(ADMIN, PRODUCTION_PLANNER, SHOP_FLOOR_SUPERVISOR, QUALITY_INSPECTOR)
                        .pathMatchers("/api/products/**").hasAnyRole(ADMIN, PRODUCTION_PLANNER)
                        .pathMatchers(HttpMethod.POST, "/api/orders/*/release").hasAnyRole(ADMIN, PRODUCTION_PLANNER)
                        .pathMatchers(HttpMethod.POST, "/api/orders/*/schedule", "/api/orders/*/start").hasAnyRole(ADMIN, PRODUCTION_PLANNER, SHOP_FLOOR_SUPERVISOR)
                        .pathMatchers(HttpMethod.POST, "/api/orders/*/produced-quantity").hasAnyRole(ADMIN, PRODUCTION_PLANNER, SHOP_FLOOR_SUPERVISOR)
                        .pathMatchers(HttpMethod.POST, "/api/orders/*/complete").hasAnyRole(ADMIN, SHOP_FLOOR_SUPERVISOR)
                        .pathMatchers(HttpMethod.POST, "/api/orders/*/cancel").hasAnyRole(ADMIN, PRODUCTION_PLANNER, SHOP_FLOOR_SUPERVISOR)
                        .pathMatchers(HttpMethod.GET, "/api/orders/**").hasAnyRole(ADMIN, PRODUCTION_PLANNER, SHOP_FLOOR_SUPERVISOR, QUALITY_INSPECTOR)
                        .pathMatchers("/api/orders/**").hasAnyRole(ADMIN, PRODUCTION_PLANNER)
                        .pathMatchers(HttpMethod.GET, "/api/machines/**").hasAnyRole(ADMIN, PRODUCTION_PLANNER, SHOP_FLOOR_SUPERVISOR, MAINTENANCE_ENGINEER)
                        .pathMatchers("/api/machines/**").hasAnyRole(ADMIN, SHOP_FLOOR_SUPERVISOR, MAINTENANCE_ENGINEER)
                        .pathMatchers("/api/quality/**").hasAnyRole(ADMIN, QUALITY_INSPECTOR)
                        .pathMatchers("/api/maintenance/**").hasAnyRole(ADMIN, MAINTENANCE_ENGINEER)
                        .pathMatchers("/", "/index.html", "/dashboard", "/products", "/orders", "/machines", "/quality", "/maintenance").permitAll()
                        .anyExchange().authenticated()
                )
                .addFilterAt(jwtAuthenticationWebFilter, SecurityWebFiltersOrder.AUTHENTICATION)
                .build();
    }


    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
