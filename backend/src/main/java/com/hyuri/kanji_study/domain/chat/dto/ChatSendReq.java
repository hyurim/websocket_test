package com.hyuri.kanji_study.domain.chat.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ChatSendReq(
        @NotNull @Min(1) Long roomId,
        @NotBlank String content
) {}
