package com.hyuri.kanji_study.auth.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.Map;

@Component
public class JwtUtil {

    private final Key key;
    private final long expirationMillis;
    private final long refreshExpirationMillis;

    public JwtUtil(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.expiration-ms}") long expirationMillis,
            @Value("${jwt.refresh-expiration-ms}") long refreshExpirationMillis,
            @Value("${jwt.secret.is-base64:false}") boolean isBase64
    ) {
        byte[] keyBytes = isBase64
                ? Decoders.BASE64.decode(secret)                       // A) secret이 Base64일 때
                : secret.getBytes(StandardCharsets.UTF_8);             // B) 평문 시크릿(32바이트 이상 권장)
        this.key = Keys.hmacShaKeyFor(keyBytes);                       // HS256/384/512 자동결정
        this.expirationMillis = expirationMillis;
        this.refreshExpirationMillis = refreshExpirationMillis;
    }

    /** 토큰 생성 */
    public String generateToken(String loginId) {
        return generateToken(loginId, Map.of("role", "user"));
    }

    /** 토큰 생성 */
    public String generateToken(String loginId, Map<String, ?> payload) {
        Claims claims = Jwts.claims()
                .setSubject(loginId);

        Date now = new Date();
        Date exp = new Date(now.getTime() + expirationMillis);

        if (payload != null) claims.putAll(payload);
        claims.computeIfAbsent("role", k -> "user");

        return Jwts.builder()
                .setHeaderParam("type", "JWT")
                .setSubject(loginId)
                .addClaims(claims)
                .setIssuedAt(now)
                .setExpiration(exp)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    // JWT 토큰에서 아이디 추출
    public String extractLoginId(String token) {
        return getClaims(token).getSubject();
    }

    // 토큰 만료 여부 확인
    public boolean isTokenExpired(String token) {
        return getClaims(token).getExpiration().before(new Date());
    }

    // 토큰 유효성 검사
    public boolean validateToken(String token, String expectedLoginId) {
        try {
            return expectedLoginId.equals(extractLoginId(token)) && !isTokenExpired(token);
        } catch (JwtException | IllegalArgumentException e) {
            return false; // 파싱 실패 포함
        }
    }

    // 토큰에서 Claims 정보 추출
    private Claims getClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    // Refresh Token
    public String generateRefreshToken(String loginId) {
        Date now = new Date();
        Date exp = new Date(now.getTime() + refreshExpirationMillis); // 7일
        return Jwts.builder()
                .setSubject(loginId)
                .setIssuedAt(now)
                .setExpiration(exp)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public String extractUsername(String jwt) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(jwt)
                .getBody()
                .getSubject();
    }

    public long getRefreshExpirationMs() {
        return refreshExpirationMillis; // 생성자에서 주입 및 설정한 값 사용
    }
}
