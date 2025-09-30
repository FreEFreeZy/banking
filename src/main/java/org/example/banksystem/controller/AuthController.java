package org.example.banksystem.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.example.banksystem.dto.AuthRequest;
import org.example.banksystem.entity.Role;
import org.example.banksystem.entity.User;
import org.example.banksystem.security.JwtTokenProvider;
import org.example.banksystem.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RequestMapping("/api/auth")
@RestController
@Tag(name = "Authentication", description = "API для аутентификации и регистрации пользователей")
public class AuthController {

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private UserService userService;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Operation(
            summary = "Вход в систему",
            description = "Аутентификация пользователя и получение JWT токена в cookie"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Успешная аутентификация",
                    content = @Content(schema = @Schema(implementation = java.util.Map.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Неверный пароль",
                    content = @Content(schema = @Schema(implementation = java.util.Map.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Пользователь не найден",
                    content = @Content(schema = @Schema(implementation = java.util.Map.class))
            )
    })
    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(
            @Parameter(
                    description = "Данные для аутентификации",
                    required = true,
                    schema = @Schema(implementation = AuthRequest.class)
            )
            @RequestBody AuthRequest request,
            HttpServletResponse response) {
        if (!userService.exists(request.getUsername())) {
            return ResponseEntity.status(404).body(Map.of("Response", "User not found"));
        }
        User user = userService.get(request.getUsername());
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            return ResponseEntity.status(401).body(Map.of("Response", "Wrong password"));
        }
        Cookie cookie = new Cookie("Authentication", jwtTokenProvider.createToken(user.getUsername(), user.getAuthorities()));
        cookie.setHttpOnly(true);
        cookie.setMaxAge(3600);
        cookie.setPath("/");
        cookie.setSecure(false);
        response.addCookie(cookie);
        return ResponseEntity.ok(Map.of("Response", "Success"));
    }

    @Operation(
            summary = "Регистрация нового пользователя",
            description = "Создание нового пользователя с ролью ROLE_USER"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Пользователь успешно зарегистрирован",
                    content = @Content(schema = @Schema(implementation = java.util.Map.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Пользователь уже существует",
                    content = @Content(schema = @Schema(implementation = java.util.Map.class))
            )
    })
    @PostMapping("/register")
    public ResponseEntity<Map<String, String>> register(
            @Parameter(
                    description = "Данные для регистрации",
                    required = true,
                    schema = @Schema(implementation = AuthRequest.class)
            )
            @RequestBody AuthRequest request) {
        if (userService.exists(request.getUsername())) {
            return ResponseEntity.status(401).body(Map.of("Response", "User already exists!"));
        }
        userService.save(new User(request.getUsername(), passwordEncoder.encode(request.getPassword()), Role.ROLE_USER));
        return ResponseEntity.status(200).body(Map.of("Response", "User registered!"));
    }
}