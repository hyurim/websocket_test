package com.hyuri.kanji_study.domain.chat.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record JoinRoomReq(
        @NotNull
        @Min(1) Long roomId
) {}
