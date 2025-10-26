package com.hyuri.kanji_study.domain.chat.dto;

import java.time.Instant;

public record ChatMessageRes(
        Long roomId,
        String sender,   // loginId (Principal)
        String text,
        Instant sentAt
) {}