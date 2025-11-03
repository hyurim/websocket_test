package com.hyuri.kanji_study.domain.notification.service;

import com.hyuri.kanji_study.domain.notification.dto.NotificationDTO;
import com.hyuri.kanji_study.domain.notification.entity.NotificationEntity;
import com.hyuri.kanji_study.domain.notification.repository.NotificationRepository;
import com.hyuri.kanji_study.domain.user.entity.UserEntity;
import com.hyuri.kanji_study.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
@Transactional
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepo;
    private final UserRepository userRepo;


    public List<NotificationDTO> getNotifications(String loginId) {
        UserEntity user = getUserOrThrow(loginId);
        List<NotificationEntity> entities = notificationRepo.findByUserOrderByCreatedAtDesc(user);

        return entities.stream()
                .map(e -> NotificationDTO.builder()
                        .id(e.getNotificationId())
                        .type(e.getType())
                        .message(e.getMessage())
                        .isRead(e.isRead())
                        .createdAt(e.getCreatedAt())
                        .build())
                .toList();
    }


    private UserEntity getUserOrThrow(String loginId) {
        return userRepo.findByLoginId(loginId)
                .orElseThrow(() -> new RuntimeException("유저를 찾을 수 없습니다.: " + loginId));
    }

    // 알림 생성
    public NotificationEntity pushNotification(Long userId, String type, String message) {
        // userId로 유저를 로드
        UserEntity user = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("유저를 찾을 수 없습니다.: " + userId));

        NotificationEntity n = NotificationEntity.builder()
                .user(user)
                .type(type)
                .message(message)
                .isRead(false)
                .build();

        return notificationRepo.save(n);
    }

    // 로그인한 유저 알림 목록 조회
    @Transactional(readOnly = true)
    public List<NotificationEntity> getUserNotifications(String loginId) {
        UserEntity user = getUserOrThrow(loginId);
        return notificationRepo.findByUserOrderByCreatedAtDesc(user);
    }

    // 로그인한 유저의 안읽은 알림 개수
    @Transactional(readOnly = true)
    public long getUnreadCount(String loginId) {
        UserEntity user = getUserOrThrow(loginId);
        return notificationRepo.countByUserAndIsReadFalse(user);
    }

    // 알림 읽음 처리 (다른 사람 알림 건드리면 안됨)
    public void markAsRead(String loginId, Long notificationId) {
        UserEntity user = getUserOrThrow(loginId);

        NotificationEntity n = notificationRepo.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("알림을 찾을 수 없습니다.: " + notificationId));

        if (!n.getUser().getUserId().equals(user.getUserId())) {
            throw new RuntimeException("다른 사람 알림을 건들 수 없습니다.");
        }

        n.setRead(true);
        notificationRepo.save(n);
    }
}
