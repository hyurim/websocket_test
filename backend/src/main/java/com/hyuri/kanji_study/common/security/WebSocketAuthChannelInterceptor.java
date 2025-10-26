package com.hyuri.kanji_study.common.security;

import com.hyuri.kanji_study.auth.jwt.JwtUtil;
import lombok.RequiredArgsConstructor;
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

@Component
@RequiredArgsConstructor
public class WebSocketAuthChannelInterceptor implements ChannelInterceptor {

    private final JwtUtil jwtUtil;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor acc = StompHeaderAccessor.wrap(message);

        if (StompCommand.CONNECT == acc.getCommand()) {
            String token = null;
            var authHeaders = acc.getNativeHeader("Authorization");
            if (authHeaders != null && !authHeaders.isEmpty()) {
                String h = authHeaders.get(0);
                if (h != null && h.startsWith("Bearer ")) token = h.substring(7);
            }
            if (token == null || token.isBlank()) {
                throw new IllegalArgumentException("Bearer <token>을 찾을 수 없음.");
            }

            try {

                String loginId = jwtUtil.extractLoginId(token); // or extractUsername(token)

                // 검증
                if (!jwtUtil.validateToken(token, loginId)) {
                    throw new IllegalArgumentException("JWT가 유효하지 않거나 만료됨.");
                }

                Authentication user = new UsernamePasswordAuthenticationToken(
                        loginId, null, List.of(new SimpleGrantedAuthority("ROLE_USER"))
                );
                acc.setUser(user);

            } catch (io.jsonwebtoken.JwtException e) {
                // 서명 불일치, 만료 등 JJWT 예외는 여기서 잡아 커넥션 거부
                throw new IllegalArgumentException("JWT 서명 불일치 혹은 만료됨. failed: " + e.getMessage(), e);
            }
        }
        return message;
    }
}
