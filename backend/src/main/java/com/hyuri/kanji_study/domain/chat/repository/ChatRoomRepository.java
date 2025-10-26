package com.hyuri.kanji_study.domain.chat.repository;

import com.hyuri.kanji_study.domain.chat.entity.ChatRoomEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatRoomRepository extends JpaRepository<ChatRoomEntity, Long> {}

