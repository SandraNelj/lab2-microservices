package org.example.authservice;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.web.bind.annotation.*;
import java.time.Instant;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final JwtEncoder jwtEncoder;
    private final PasswordEncoder passwordEncoder;
    private final UserDetailsService userDetailsService;

    public AuthController(JwtEncoder jwtEncoder,
                          PasswordEncoder passwordEncoder,
                          UserDetailsService userDetailsService) {
        this.jwtEncoder = jwtEncoder;
        this.passwordEncoder = passwordEncoder;
        this.userDetailsService = userDetailsService;
    }

    @PostMapping("/login")
    public Map<String, String> login(@RequestBody LoginRequest request) {
        UserDetails userDetails = userDetailsService.loadUserByUsername(request.username());

        if (!passwordEncoder.matches(request.password(), userDetails.getPassword())) {
            throw new RuntimeException("Invalid credentials");
        }

        Instant now = Instant.now();
        long expiry = 3600L;

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("http://127.0.0.1:9000")
                .issuedAt(now)
                .expiresAt(now.plusSeconds(expiry))
                .subject(request.username())
                .claim("scope", "user.read user.write message.read message.write")
                .build();

        String token = jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();

        return Map.of("access_token", token,
                "token_type", "Bearer",
                "expires_in", String.valueOf(expiry));
    }

    @PostMapping("/register")
    public Map<String, String> register(@RequestBody RegisterRequest request) {
        if (userDetailsService instanceof InMemoryUserDetailsManager manager) {
            if (manager.userExists(request.username())) {
                throw new RuntimeException("User already exists");
            }

            UserDetails newUser = User.builder()
                    .username(request.username())
                    .password(passwordEncoder.encode(request.password()))
                    .roles("USER")
                    .build();

            manager.createUser(newUser);

            return Map.of("message", "User registered successfully",
                    "username", request.username());
        }

        throw new RuntimeException("Registration not supported");
    }
}

record LoginRequest(String username, String password) {}
record RegisterRequest(String username, String password) {}