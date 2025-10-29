package com.hyuri.kanji_study.domain.chat.repository;

import com.hyuri.kanji_study.domain.chat.entity.ChatRoomEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ChatRoomRepository extends JpaRepository<ChatRoomEntity, Long> {
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = """
    insert into chat_room (id, name, created_by, created_at)
    values (:roomId, :name, :createdBy, now())
    on conflict (id) do nothing
""", nativeQuery = true)
    int insertIgnore(@Param("roomId") Long roomId,
                     @Param("name") String name,
                     @Param("createdBy") Long createdBy);

}

