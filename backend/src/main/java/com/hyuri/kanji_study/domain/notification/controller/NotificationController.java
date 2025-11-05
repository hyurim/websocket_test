package com.hyuri.kanji_study.domain.notification.controller;

import com.hyuri.kanji_study.domain.notification.dto.NotificationDto;
import com.hyuri.kanji_study.domain.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.List;

@RequiredArgsConstructor
@RestController
@Slf4j
@RequestMapping("/api/notifications")
public class NotificationController {
    private final NotificationService notificationService;

    // 로그인한 유저의 알림 목록 조회
    @GetMapping
    public ResponseEntity<List<NotificationDto>> getMyNotifications(
            Principal principal) {
        String loginId = principal.getName();
        List<NotificationDto> notifications = notificationService.getUserNotifications(loginId)
                .stream()
                .map(e -> NotificationDto.builder()
                        .id(e.getNotificationId())
                        .type(e.getType())
                        .message(e.getMessage())
                        .isRead(e.isRead())
                        .createdAt(e.getCreatedAt())
                        .build())
                .toList();

        return ResponseEntity.ok(notifications);
    }

    // 안 읽은 알림 개수 조회
    @GetMapping("/unread-count")
    public ResponseEntity<Long> getUnreadCount(
            Principal principal) {
        String loginId = principal.getName();

        long count = notificationService.getUnreadCount(loginId);
        return ResponseEntity.ok(count);
    }


    // 특정 알림 읽음 처리
    @PostMapping("/{notificationId}/read")
    public ResponseEntity<Void> markAsRead(
            Principal principal,
            @PathVariable Long notificationId
    ) {
        String loginId = principal.getName();
        notificationService.markAsRead(loginId, notificationId);
        return ResponseEntity.ok().build();
    }
}
