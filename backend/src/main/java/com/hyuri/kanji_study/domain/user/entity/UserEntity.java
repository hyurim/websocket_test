package com.hyuri.kanji_study.domain.user.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.hyuri.kanji_study.domain.notification.entity.NotificationEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "users")
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")             // SERIAL PK
    private Long userId;

    @Column(name = "login_id", length = 50, unique = true)
    private String loginId;

    @Column(name = "password", length = 255)
    private String password;

    @Column(name = "nickname", length = 50)
    private String nickname;

    @Column(name = "streak_days")
    private Integer streakDays;

    @Column(name = "role", length = 20)   // ADMIN은 수동으로 만들 예정
    private String role;

    // DB DEFAULT CURRENT_TIMESTAMP 사용
    @Column(name = "created_at", insertable = false, updatable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime createdAt;

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    @PrePersist
    public void prePersist() {
        if (this.role == null || this.role.isBlank()) this.role = "USER";
        if (this.streakDays == null) this.streakDays = 0;
    }

    @OneToMany(mappedBy = "user")
    @ToString.Exclude
    @JsonIgnore
    private List<NotificationEntity> notifications = new ArrayList<>();
}

