package com.hyuri.kanji_study.domain.chat.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class ChatParticipantId implements Serializable {
    @Column(name = "room_id")
    private Long roomId;
    @Column(name = "user_id")
    private Long userId;
}
