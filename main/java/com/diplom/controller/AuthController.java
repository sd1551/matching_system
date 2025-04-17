package com.diplom.controller;

import com.diplom.service.UploadService;
import com.diplom.dto.AuthRequest;
import com.diplom.dto.AuthResponse;
import com.diplom.dto.RegisterRequest;
import com.diplom.entity.UserEntity;
import com.diplom.security.JwtTokenProvider;
import com.diplom.service.AuthService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Controller
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;
    private final JwtTokenProvider jwtTokenProvider;
    private final UploadService uploadService;

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    @GetMapping("/register")
    public String registerPage() {
        return "register";
    }

    @GetMapping("/upload")
    public String uploadPage() {
        return "upload";
    }

    @GetMapping("/create")
    public String createPage(@CookieValue(value = "token", required = false) String token) {
        if (token == null || !authService.validateToken(token)) {
            return "redirect:/auth/login";
        }

        String role = jwtTokenProvider.getRole(token);
        if (!"ADMIN".equals(role)) {
            return "redirect:/hub";
        }

        return "create";
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody RegisterRequest request, HttpServletResponse response) {
        String token = authService.register(request.getUsername(), request.getPassword());
        authService.setAuthCookie(response, token);
        return ResponseEntity.ok(new AuthResponse(token));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody AuthRequest request, HttpServletResponse response) {
        try {
            String token = authService.authenticate(request.getUsername(), request.getPassword());
            authService.setAuthCookie(response, token);
            return ResponseEntity.ok()
                    .header("Set-Cookie", "token=" + token + "; Path=/; HttpOnly; SameSite=Lax; Max-Age=3600")
                    .body(new AuthResponse(token));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new AuthResponse(null));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletResponse response) {
        authService.clearAuthCookie(response);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/upload")
    public ResponseEntity<String> uploadFile(
            @RequestParam("file") MultipartFile file,
            @CookieValue(value = "token", required = false) String token
    ) throws IOException {
        if (token == null || !authService.validateToken(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
        }

        UserEntity user = authService.getUserFromToken(token);
        String filePath = uploadService.uploadDocument(user.getId(), file);

        return ResponseEntity.ok("Файл успешно загружен: " + filePath);
    }

    @GetMapping("/check")
    public ResponseEntity<Map<String, String>> checkAuth(@CookieValue(name = "token", required = false) String token) {
        if (token != null && jwtTokenProvider.validateToken(token)) {
            String username = jwtTokenProvider.getUsername(token);
            String role = jwtTokenProvider.getRole(token);
            return ResponseEntity.ok(Map.of("username", username, "role", role));
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

}