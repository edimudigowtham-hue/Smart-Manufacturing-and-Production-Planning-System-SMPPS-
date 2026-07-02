package com.genc.smpps.gateway.security;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
public class JwtService {
    private static final Base64.Encoder BASE64_URL_ENCODER = Base64.getUrlEncoder().withoutPadding();
    private static final Base64.Decoder BASE64_URL_DECODER = Base64.getUrlDecoder();
    private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() {};

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final byte[] secret;
    private final String issuer;
    private final long expirationMinutes;

    public JwtService(
            @Value("${smpps.jwt.secret}") String secret,
            @Value("${smpps.jwt.issuer}") String issuer,
            @Value("${smpps.jwt.expiration-minutes}") long expirationMinutes
    ) {
        this.secret = validateSecret(secret);
        this.issuer = issuer;
        this.expirationMinutes = expirationMinutes;
    }

    public String generateToken(UserDetails userDetails) {
        Instant now = Instant.now();
        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .map(authority -> authority.replaceFirst("^ROLE_", ""))
                .sorted()
                .toList();

        Map<String, Object> header = Map.of("alg", "HS256", "typ", "JWT");
        Map<String, Object> claims = new LinkedHashMap<>();
        claims.put("iss", issuer);
        claims.put("sub", userDetails.getUsername());
        claims.put("iat", now.getEpochSecond());
        claims.put("exp", now.plusSeconds(expirationMinutes * 60).getEpochSecond());
        claims.put("roles", roles);

        String unsignedToken = encodeJson(header) + "." + encodeJson(claims);
        return unsignedToken + "." + sign(unsignedToken);
    }

    public JwtUser parseToken(String token) {
        try {
            String[] parts = token.split("\\.", -1);
            if (parts.length != 3) {
                throw new IllegalArgumentException("JWT must contain header, payload, and signature");
            }

            String unsignedToken = parts[0] + "." + parts[1];
            if (!MessageDigest.isEqual(sign(unsignedToken).getBytes(StandardCharsets.UTF_8), parts[2].getBytes(StandardCharsets.UTF_8))) {
                throw new IllegalArgumentException("Invalid JWT signature");
            }

            Map<String, Object> claims = objectMapper.readValue(BASE64_URL_DECODER.decode(parts[1]), MAP_TYPE);
            if (!issuer.equals(claims.get("iss"))) {
                throw new IllegalArgumentException("Invalid JWT issuer");
            }

            long expiresAt = numberClaim(claims, "exp");
            if (Instant.now().getEpochSecond() >= expiresAt) {
                throw new IllegalArgumentException("JWT has expired");
            }

            String username = String.valueOf(claims.get("sub"));
            if (username == null || username.isBlank() || "null".equals(username)) {
                throw new IllegalArgumentException("JWT subject is missing");
            }

            Object rolesClaim = claims.get("roles");
            if (!(rolesClaim instanceof List<?> roleValues)) {
                throw new IllegalArgumentException("JWT roles claim is missing");
            }

            List<String> roles = roleValues.stream()
                    .map(String::valueOf)
                    .filter(role -> !role.isBlank())
                    .sorted()
                    .toList();

            return new JwtUser(username, roles);
        } catch (Exception ex) {
            throw new IllegalArgumentException("Invalid JWT", ex);
        }
    }

    private String encodeJson(Map<String, Object> value) {
        try {
            return BASE64_URL_ENCODER.encodeToString(objectMapper.writeValueAsBytes(value));
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to encode JWT JSON", ex);
        }
    }

    private String sign(String value) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret, "HmacSHA256"));
            return BASE64_URL_ENCODER.encodeToString(mac.doFinal(value.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to sign JWT", ex);
        }
    }

    private byte[] validateSecret(String value) {
        byte[] bytes = value == null ? new byte[0] : value.getBytes(StandardCharsets.UTF_8);
        if (bytes.length < 32) {
            throw new IllegalStateException("smpps.jwt.secret must contain at least 32 UTF-8 bytes");
        }
        return bytes;
    }

    private long numberClaim(Map<String, Object> claims, String name) {
        Object value = claims.get(name);
        if (value instanceof Number number) {
            return number.longValue();
        }
        throw new IllegalArgumentException("JWT claim is not numeric: " + name);
    }

    public record JwtUser(String username, List<String> roles) {
    }
}


