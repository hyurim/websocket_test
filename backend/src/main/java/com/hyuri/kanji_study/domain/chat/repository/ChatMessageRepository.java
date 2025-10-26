package com.hyuri.kanji_study.domain.chat.repository;

import com.hyuri.kanji_study.domain.chat.entity.ChatMessageEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatMessageRepository extends JpaRepository<ChatMessageEntity, Long> {
    Slice<ChatMessageEntity> findByRoomIdOrderByCreatedAtDesc(Long roomId, Pageable pageable);
}
