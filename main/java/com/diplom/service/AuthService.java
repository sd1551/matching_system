package com.diplom.service;

import com.diplom.KafkaProducerService;
import com.diplom.entity.UserEntity;
import com.diplom.repo.UserRepository;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import com.diplom.entity.Role;
import com.diplom.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final KafkaProducerService kafkaProducerService;
    private final FileStorageService fileStorageService;

    public String register(String username, String password) {
        if (userRepository.findByUsername(username).isPresent()) {
            throw new IllegalArgumentException("User already exists");
        }

        UserEntity user = new UserEntity();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole(Role.USER);
        userRepository.save(user);

        return jwtTokenProvider.generateToken(username, user.getRole().name());
    }

    public String authenticate(String username, String password) {
        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("Invalid password");
        }

        return jwtTokenProvider.generateToken(username, user.getRole().name());
    }

    public void setAuthCookie(HttpServletResponse response, String token) {
        Cookie cookie = new Cookie("token", token);
        cookie.setHttpOnly(true);
        cookie.setSecure(false);
        cookie.setPath("/");
        cookie.setMaxAge(3600);
        response.addCookie(cookie);
    }

    public void clearAuthCookie(HttpServletResponse response) {
        Cookie cookie = new Cookie("token", null);
        cookie.setHttpOnly(true);
        cookie.setSecure(false);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        response.addCookie(cookie);
    }

    public boolean validateToken(String token) {
        return jwtTokenProvider.validateToken(token);
    }

    public String getUsernameFromToken(String token) {
        return jwtTokenProvider.getUsername(token);
    }

    public void sendFileUploadEvent(String username, MultipartFile file) throws IOException {
        // Сохраняем файл на диск и получаем путь
        String filePath = fileStorageService.storeFile(file);

        // Отправляем событие в Kafka с путем к файлу и его именем
        kafkaProducerService.sendFileUploadEvent(username, filePath, file.getOriginalFilename());
    }

    public UserEntity getUserFromToken(String token) {
        String username = jwtTokenProvider.getUsername(token);
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

}
