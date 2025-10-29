package com.hyuri.kanji_study.domain.chat.dto;

import java.time.Instant;

public record ChatMessageRes(
        Long id,             // ← DB PK (중복 제거의 1차 키)
        Long roomId,
        String sender,       // loginId (또는 nickname)
        String text,
        Instant sentAt,      // 서버 기준 생성 시각
        String clientMsgId
) {}