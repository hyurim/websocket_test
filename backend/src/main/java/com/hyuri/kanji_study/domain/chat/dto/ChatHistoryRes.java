package com.hyuri.kanji_study.domain.chat.dto;

import java.time.Instant;

public record ChatHistoryRes(
        Long id,
        String senderLoginId,
        String text,
        Instant sentAt
) {}
