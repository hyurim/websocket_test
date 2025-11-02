package com.hyuri.kanji_study.domain.notification.repository;

import com.hyuri.kanji_study.domain.notification.entity.NotificationEntity;
import com.hyuri.kanji_study.domain.user.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<NotificationEntity, Long> {
    // 특정 유저의 알림
    List<NotificationEntity> findByUserOrderByCreatedAtDesc(UserEntity user);

    // 안 읽은 알림 개수
    long countByUserAndIsReadFalse(UserEntity user);
}
