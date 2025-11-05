package com.hyuri.kanji_study.domain.notification.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class NotificationDto {
    private Long id;
    private String type;
    private String message;
    @JsonProperty("isRead")
    private boolean isRead;
    private LocalDateTime createdAt;
}
