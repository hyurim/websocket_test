package com.hyuri.kanji_study.common.security;

import com.hyuri.kanji_study.auth.jwt.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketAuthChannelInterceptor implements ChannelInterceptor {

    private final JwtUtil jwtUtil;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor acc = StompHeaderAccessor.wrap(message);

        if (acc.getCommand() != null) {
            log.info("[WS] INBOUND {}", acc.getCommand());
        }

        // ① CONNECT: 토큰 검증 → Principal 설정 + 세션에 백업 저장
        if (StompCommand.CONNECT.equals(acc.getCommand())) {
            String raw = acc.getFirstNativeHeader("Authorization");
            log.info("[WS] CONNECT try: {}", raw);

            String token = (raw != null && raw.startsWith("Bearer ")) ? raw.substring(7) : null;
            if (token == null || token.isBlank()) {
                throw new IllegalArgumentException("Bearer <token>을 찾을 수 없음.");
            }

            String loginId = jwtUtil.extractLoginId(token);
            if (!jwtUtil.validateToken(token, loginId)) {
                throw new IllegalArgumentException("JWT가 유효하지 않거나 만료됨.");
            }

            Authentication auth = new UsernamePasswordAuthenticationToken(
                    loginId, null, List.of(new SimpleGrantedAuthority("ROLE_USER")));

            acc.setUser(auth); // ← 이후 세션에 유지되어야 함
            // 세션 백업 (혹시 환경에 따라 principal이 누락되는 경우 대비)
            acc.getSessionAttributes().put("loginId", loginId);

            log.info("[WS] CONNECT ok user={}", loginId);
        }

        // ② SEND: 드물게 principal이 비는 경우 세션 값으로 보정
        if (StompCommand.SEND.equals(acc.getCommand()) && acc.getUser() == null) {
            Object lid = acc.getSessionAttributes() != null ? acc.getSessionAttributes().get("loginId") : null;
            if (lid != null) {
                Authentication auth = new UsernamePasswordAuthenticationToken(
                        lid.toString(), null, List.of(new SimpleGrantedAuthority("ROLE_USER")));
                acc.setUser(auth);
                log.info("[WS] SEND backfill user={}", lid);
            } else {
                log.warn("[WS] SEND without principal and no session backup");
            }
        }

        return message;
    }
}