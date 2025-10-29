package com.hyuri.kanji_study.domain.chat.dto;

import java.time.Instant;

public record ChatHistoryRes(
        Long id,
        Long roomId,
        String sender,
        String message,
        Instant sentAt
) {}
