package com.genc.smpps.gateway.user;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class GatewayUserService implements ReactiveUserDetailsService {
    public static final String ADMIN = "ADMIN";
    public static final String PRODUCTION_PLANNER = "PRODUCTION_PLANNER";
    public static final String SHOP_FLOOR_SUPERVISOR = "SHOP_FLOOR_SUPERVISOR";
    public static final String QUALITY_INSPECTOR = "QUALITY_INSPECTOR";
    public static final String MAINTENANCE_ENGINEER = "MAINTENANCE_ENGINEER";

    private static final Set<String> ALLOWED_ROLES = Set.of(
            ADMIN,
            PRODUCTION_PLANNER,
            SHOP_FLOOR_SUPERVISOR,
            QUALITY_INSPECTOR,
            MAINTENANCE_ENGINEER
    );

    private static final List<UserAccount> PREDEFINED_USERS = List.of(
            new UserAccount("admin", "$2a$10$Kr4nameFCQuFdZoPub1PhOcN8OdE2IU3n.8ZXAblt9O.hHsnSVV7i", ADMIN, true),
            new UserAccount("planner", "$2a$10$zA61/k3B8EztufdpixfKjuMyWzJp/qGfdPlXigBn.a.ZxAwikf.1.", PRODUCTION_PLANNER, true),
            new UserAccount("supervisor", "$2a$10$7Gcv7mqPYXqhb9lAQvzKNeczE/vWQG5blgMMM4RmhfRYpuXdNgFXK", SHOP_FLOOR_SUPERVISOR, true),
            new UserAccount("quality", "$2a$10$hLQh5bGJ1leaK6K0JYbZZ.Sh8Et3F1dPZt4DuGU9T3w4eLxlAjMCa", QUALITY_INSPECTOR, true),
            new UserAccount("maintenance", "$2a$10$3pPGLBYSE2TaYneR.V0t8ehaxPFBti8U/B9l.0eDLs.2j8hCJ5IOS", MAINTENANCE_ENGINEER, true)
    );

    private final Map<String, UserAccount> users = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final PasswordEncoder passwordEncoder;
    private final Path storageFile;

    public GatewayUserService(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
        this.storageFile = resolveStorageFile();
        loadPredefinedUsers();
        loadUsers();
        saveUsers();
    }

    @Override
    public Mono<UserDetails> findByUsername(String username) {
        UserAccount account = users.get(normalizeUsername(username));
        if (account == null) {
            return Mono.error(new UsernameNotFoundException("User not found: " + username));
        }
        return Mono.just(toUserDetails(account));
    }

    public synchronized UserAccount createUser(CreateUserRequest request) {
        String username = cleanUsername(request.getUsername());
        String password = request.getPassword() == null ? "" : request.getPassword();
        String role = normalizeRole(request.getRole());

        validateUserCreation(username, password, role);

        String key = normalizeUsername(username);
        if (users.containsKey(key)) {
            throw new IllegalArgumentException("Username already exists");
        }

        UserAccount account = new UserAccount(username, passwordEncoder.encode(password), role, true);
        users.put(key, account);
        saveUsers();
        return account;
    }

    public List<Map<String, Object>> listUsers() {
        return users.values().stream()
                .sorted(Comparator.comparing(user -> normalizeUsername(user.getUsername())))
                .map(user -> Map.<String, Object>of(
                        "username", user.getUsername(),
                        "role", user.getRole(),
                        "enabled", user.isEnabled()
                ))
                .toList();
    }

    public List<String> allowedRoles() {
        return ALLOWED_ROLES.stream().sorted(Comparator.naturalOrder()).toList();
    }

    private void loadUsers() {
        if (!Files.exists(storageFile)) {
            return;
        }
        try {
            List<UserAccount> loadedUsers = objectMapper.readValue(storageFile.toFile(), new TypeReference<>() {});
            loadedUsers.forEach(account -> users.put(normalizeUsername(account.getUsername()), account));
        } catch (IOException ex) {
            throw new IllegalStateException("Unable to read gateway users from " + storageFile, ex);
        }
    }

    private void loadPredefinedUsers() {
        PREDEFINED_USERS.forEach(account -> users.put(normalizeUsername(account.getUsername()), account));
    }

    private Path resolveStorageFile() {
        Path workingDir = Path.of(System.getProperty("user.dir"));
        Path moduleFromAggregator = workingDir.resolve("api-gateway");
        if (Files.isDirectory(moduleFromAggregator)) {
            return moduleFromAggregator.resolve("data").resolve("registered-users.json");
        }

        Path moduleFromWorkspace = workingDir.resolve("smpps-microservices").resolve("api-gateway");
        if (Files.isDirectory(moduleFromWorkspace)) {
            return moduleFromWorkspace.resolve("data").resolve("registered-users.json");
        }

        return workingDir.resolve("data").resolve("registered-users.json");
    }


    private void saveUsers() {
        try {
            Files.createDirectories(storageFile.getParent());
            List<UserAccount> orderedUsers = new ArrayList<>(users.values());
            orderedUsers.sort(Comparator.comparing(user -> normalizeUsername(user.getUsername())));
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(storageFile.toFile(), orderedUsers);
        } catch (IOException ex) {
            throw new IllegalStateException("Unable to save gateway users to " + storageFile, ex);
        }
    }

    private UserDetails toUserDetails(UserAccount account) {
        return User.withUsername(account.getUsername())
                .password(account.getPassword())
                .roles(account.getRole())
                .disabled(!account.isEnabled())
                .build();
    }

    private void validateUserCreation(String username, String password, String role) {
        if (username.length() < 3 || username.length() > 50) {
            throw new IllegalArgumentException("Username must be between 3 and 50 characters");
        }
        if (!username.matches("[A-Za-z0-9._-]+")) {
            throw new IllegalArgumentException("Username can contain only letters, numbers, dot, underscore, and hyphen");
        }
        if (password.length() < 6 || password.length() > 72) {
            throw new IllegalArgumentException("Password must be between 6 and 72 characters");
        }
        if (!ALLOWED_ROLES.contains(role)) {
            throw new IllegalArgumentException("Invalid role selected");
        }
    }

    private String cleanUsername(String username) {
        return username == null ? "" : username.trim();
    }

    private String normalizeUsername(String username) {
        return cleanUsername(username).toLowerCase(Locale.ROOT);
    }

    private String normalizeRole(String role) {
        return role == null ? "" : role.trim().toUpperCase(Locale.ROOT);
    }
}

