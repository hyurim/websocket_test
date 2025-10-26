package com.hyuri.kanji_study.auth.dto;

import lombok.Builder;

@Builder
public record AuthToken(
        String accessToken,
        String refreshTokenBase64
) {
    public AuthToken {
        assert accessToken != null : "";
         assert refreshTokenBase64 != null : "";
        assert !accessToken.isBlank() : "";
         assert !refreshTokenBase64.isBlank() : "";
        // 추후 테스트 코드 작성 필요.
    }
}