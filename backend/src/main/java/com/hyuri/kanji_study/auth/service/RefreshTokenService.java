package com.hyuri.kanji_study.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final StringRedisTemplate redisTemplate;

    public void save(String userId, String refreshToken, long expirationMillis) {
        redisTemplate.opsForValue().set(
                userId,
                refreshToken,
                expirationMillis,
                TimeUnit.MILLISECONDS);
    }

    public String get(String userId) {
        return redisTemplate.opsForValue().get(userId);
    }

    public void delete(String userId) {
        redisTemplate.delete(userId);
    }
}
