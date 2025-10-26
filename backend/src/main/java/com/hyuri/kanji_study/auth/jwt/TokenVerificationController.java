package com.hyuri.kanji_study.auth.jwt;

import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class TokenVerificationController {

    private JwtUtil jwtUtil;

    @GetMapping("/verify-token")
    public ResponseEntity<?> verifyToken(@RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(401).body("Missing or invalid Authorization header");
        }
        // Bearer token에서 JWT만 추출
        String token = authHeader.substring(7);
        try {
            // 토큰에서 사용자 이름 추출
            String username = jwtUtil.extractLoginId(token);

            // 토큰 유효성 검사
            if (jwtUtil.validateToken(token, username)) {
                return ResponseEntity.ok("Token is valid");
            }
            return ResponseEntity.status(401).body("Invalid token");
        } catch (JwtException e) {
            return ResponseEntity.status(401).body("Invalid token or token parsing error");
        }
    }
}
