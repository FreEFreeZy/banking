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

@RequestMapping("/api/auth")
@RestController
public class AuthController {

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private UserService userService;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@RequestBody AuthRequest request, HttpServletResponse response) {
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

    @PostMapping("/register")
    public ResponseEntity<Map<String, String>> register(@RequestBody AuthRequest request) {
        if (userService.exists(request.getUsername())) {
            return ResponseEntity.status(401).body(Map.of("Response", "User already exists!"));
        }
        userService.save(new User(request.getUsername(), passwordEncoder.encode(request.getPassword()), Role.ROLE_USER));
        return ResponseEntity.status(200).body(Map.of("Response", "User registered!"));
    }
}
