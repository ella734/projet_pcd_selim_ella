package com.medical.platform.controllers;

import com.medical.platform.dto.AuthRequest;
import com.medical.platform.dto.AuthResponse;
import com.medical.platform.entities.User;
import com.medical.platform.repositories.UserRepository;
import com.medical.platform.security.JwtService;
import com.medical.platform.security.TokenBlacklistService;
import com.medical.platform.security.UserInfoUserDetailsService;
import com.medical.platform.service.AuthRateLimiterService;
import com.medical.platform.service.AuthSessionService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(originPatterns = {"http://localhost:*", "http://127.0.0.1:*"}, allowCredentials = "true")
public class AuthController {

    private static final String AUTH_COOKIE = "medical_platform_token";

    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthRateLimiterService rateLimiterService;
    private final AuthSessionService authSessionService;
    private final UserInfoUserDetailsService userDetailsService;
    private final TokenBlacklistService tokenBlacklistService;

    public AuthController(
        JwtService jwtService,
        UserRepository userRepository,
        UserInfoUserDetailsService userDetailsService,
        PasswordEncoder passwordEncoder,
        AuthRateLimiterService rateLimiterService,
        AuthSessionService authSessionService,
        TokenBlacklistService tokenBlacklistService
    ) {
        this.jwtService = jwtService;
        this.userRepository = userRepository;
        this.userDetailsService = userDetailsService;
        this.passwordEncoder = passwordEncoder;
        this.rateLimiterService = rateLimiterService;
        this.authSessionService = authSessionService;
        this.tokenBlacklistService = tokenBlacklistService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> authenticateAndGetToken(
        @RequestBody AuthRequest authRequest,
        HttpServletResponse response,
        HttpServletRequest request
    ) {
        String rateLimitKey = resolveRateLimitKey(authRequest.getUsername(), request);
        if (rateLimiterService.isBlocked(rateLimitKey)) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body("Too many login attempts");
        }

        User user = userRepository.findByLoginU(authRequest.getUsername()).orElse(null);
        if (user == null) {
            rateLimiterService.recordFailure(rateLimitKey);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials");
        }

        UserDetails userDetails = userDetailsService.loadUserByUsername(authRequest.getUsername());

        if (!passwordEncoder.matches(authRequest.getPassword(), userDetails.getPassword())) {
            rateLimiterService.recordFailure(rateLimitKey);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials");
        }
        rateLimiterService.clear(rateLimitKey);

        Authentication authentication =
            new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        String token = jwtService.generateToken(authRequest.getUsername());
        response.addHeader(HttpHeaders.SET_COOKIE, buildAuthCookie(token, request).toString());

        return ResponseEntity.ok(authSessionService.buildResponse(user.getLoginU()));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletResponse response, HttpServletRequest request) {
        String token = extractTokenFromRequest(request);
        if (token != null) {
            try {
                long expiresAt = jwtService.extractExpiration(token).getTime();
                tokenBlacklistService.revoke(token, expiresAt);
            } catch (Exception ignored) {
                // token malformé ou déjà expiré — pas besoin de le blacklister
            }
        }
        SecurityContextHolder.clearContext();
        response.addHeader(HttpHeaders.SET_COOKIE, clearAuthCookie(request).toString());
        return ResponseEntity.noContent().build();
    }

    private String extractTokenFromRequest(HttpServletRequest request) {
        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        jakarta.servlet.http.Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (jakarta.servlet.http.Cookie cookie : cookies) {
                if (AUTH_COOKIE.equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

    @GetMapping("/me")
    public ResponseEntity<AuthResponse> me(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(authSessionService.buildResponse(userDetails.getUsername()));
    }

    private ResponseCookie buildAuthCookie(String token, HttpServletRequest request) {
        return ResponseCookie.from(AUTH_COOKIE, token)
            .httpOnly(true)
            .secure(isSecureRequest(request))
            .path("/")
            .sameSite("Lax")
            .maxAge(jwtService.getExpirationSeconds())
            .build();
    }

    private ResponseCookie clearAuthCookie(HttpServletRequest request) {
        return ResponseCookie.from(AUTH_COOKIE, "")
            .httpOnly(true)
            .secure(isSecureRequest(request))
            .path("/")
            .sameSite("Lax")
            .maxAge(0)
            .build();
    }

    private boolean isSecureRequest(HttpServletRequest request) {
        String forwardedProto = request.getHeader("X-Forwarded-Proto");
        return request.isSecure() || "https".equalsIgnoreCase(forwardedProto);
    }

    private String resolveRateLimitKey(String username, HttpServletRequest request) {
        String ip = request.getRemoteAddr();
        return (username == null ? "" : username.trim().toLowerCase()) + "|" + ip;
    }
}
