package com.hyuri.kanji_study.domain.notification.service;

import com.hyuri.kanji_study.domain.notification.entity.NotificationEntity;

import java.util.List;

public interface NotificationService {

    // 알림 생성
    NotificationEntity pushNotification(Long userId, String type, String message);
    // 로그인한 유저의 알림 목록 조회
    List<NotificationEntity> getUserNotifications(String loginId);
    // 로그인한 유저 안읽은 알림 개수
    long getUnreadCount(String loginId);
    // 알림 읽음 처리
    void markAsRead(String loginId, Long notificationId);
}
