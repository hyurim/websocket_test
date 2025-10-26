package com.hyuri.kanji_study.auth.controller;

import com.hyuri.kanji_study.auth.dto.AuthenticationDto.AuthRequest;
import com.hyuri.kanji_study.auth.dto.AuthenticationDto.AuthResponse;
import com.hyuri.kanji_study.domain.user.dto.UserDto;
import com.hyuri.kanji_study.domain.user.repository.UserRepository;
import com.hyuri.kanji_study.auth.jwt.JwtUtil;
import com.hyuri.kanji_study.auth.service.RefreshTokenService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class AuthApi {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final RefreshTokenService refreshTokenService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody @Valid AuthRequest authRequest,
                                   HttpServletResponse response
    ) {
        try {
            // 아이디, 비밀번호 맞는지 확인 (틀리면 예외, 맞으면 반환)
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(authRequest.loginId(), authRequest.password())
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);
            String accessToken = jwtUtil.generateToken(authRequest.loginId());
            String refreshToken = jwtUtil.generateRefreshToken(authRequest.loginId());
            // JWT 생성

            // Refresh Token
            refreshTokenService.save(authRequest.loginId(), refreshToken, jwtUtil.getRefreshExpirationMs());

            // HttpOnly 쿠키로 refresh 토큰 전달
//            boolean isProd = false; // 배포시 true 변경
            boolean isProd = true;

            ResponseCookie.ResponseCookieBuilder builder = ResponseCookie.from("refreshToken", refreshToken)
                    .httpOnly(true)
                    .path("/")
                    .maxAge(Duration.ofMillis(jwtUtil.getRefreshExpirationMs()));

            if (isProd) {
                builder.sameSite("None").secure(true);   // HTTPS 환경 (운영)
            } else {
                builder.sameSite("Lax").secure(false);   // HTTP 환경 (로컬)
            }

            response.addHeader(HttpHeaders.SET_COOKIE, builder.build().toString());
            // JWT 반환
            return ResponseEntity.ok(new AuthResponse(accessToken));
        } catch (org.springframework.security.core.AuthenticationException e) {
            log.error("로그인 처리 중 오류", e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("아이디 또는 비밀번호가 일치하지 않습니다.");
        } catch (Exception e) {
            log.error("로그인 처리 중 오류", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("로그인 처리 중 오류가 발생했습니다.");
        }
    }
    /** Access 토큰 재발급 */
    @PostMapping("/token/refresh")
    public ResponseEntity<?> refresh(@CookieValue(name = "refreshToken", required = false) String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("refresh token이 없습니다.");
        }

        try {
            String userId = jwtUtil.extractUsername(refreshToken);
            String saved = refreshTokenService.get(userId);
            if (saved == null || !saved.equals(refreshToken)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("refresh token이 유효하지 않습니다.");
            }

            String newAccess = jwtUtil.generateToken(userId);
            // 고민 해볼 내용 - 리프레시 토큰 회전 전략(새 refresh 발급 + Redis 갱신 + 쿠키 재세팅)

            return ResponseEntity.ok(Map.of("accessToken", newAccess));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("refresh token이 유효하지 않습니다.");
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@CookieValue(name = "refreshToken", required = false) String refreshToken,
                                    HttpServletResponse response) {
        if (refreshToken != null && !refreshToken.isBlank()) {
            try {
                String userId = jwtUtil.extractUsername(refreshToken);
                refreshTokenService.delete(userId);
            } catch (Exception ignore) {}
        }

//        boolean isProd = false; // 배포 시 true로 변경
        boolean isProd = true;
        // 쿠키 제거
        ResponseCookie.ResponseCookieBuilder expired = ResponseCookie.from("refreshToken", "")
                .httpOnly(true)
                .path("/")
                .maxAge(0);
        if (isProd) {
            expired.sameSite("None").secure(true);
        } else {
            expired.sameSite("Lax").secure(false);
        }
        response.addHeader(HttpHeaders.SET_COOKIE, expired.build().toString());

        return ResponseEntity.ok("Logged out");
    }

    @GetMapping("/me")
    public ResponseEntity<?> me(Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
        }

        String loginId = auth.getName();

        var user = userRepository.findByLoginId(loginId)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다."));

        // Entity → DTO 변환
        UserDto dto = new UserDto();
        dto.setUserId(user.getUserId());
        dto.setLoginId(user.getLoginId());
        dto.setNickname(user.getNickname());
        dto.setPassword(null);
        dto.setRole(user.getRole());
        dto.setCreatedAt(user.getCreatedAt());
        dto.setLastLoginAt(user.getLastLoginAt());

        return ResponseEntity.ok(dto);
    }
}
