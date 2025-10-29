package com.hyuri.kanji_study.domain.chat.repository;

import com.hyuri.kanji_study.domain.chat.entity.ChatParticipantEntity;
import com.hyuri.kanji_study.domain.chat.entity.ChatParticipantId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ChatParticipantRepository extends JpaRepository<ChatParticipantEntity, ChatParticipantId> {
    boolean existsById(ChatParticipantId id);

    long countByIdRoomId(Long roomId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = """
    insert into chat_participant (room_id, user_id, joined_at)
    values (:roomId, :userId, now())
    on conflict (room_id, user_id) do nothing
""", nativeQuery = true)
    int insertIgnore(@Param("roomId") Long roomId, @Param("userId") Long userId);
}
