package com.hyuri.kanji_study.domain.chat.repository;

import com.hyuri.kanji_study.domain.chat.entity.ChatParticipantEntity;
import com.hyuri.kanji_study.domain.chat.entity.ChatParticipantId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatParticipantRepository extends JpaRepository<ChatParticipantEntity, ChatParticipantId> {
    boolean existsById(ChatParticipantId id);

    long countByIdRoomId(Long roomId);
}
