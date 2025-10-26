package com.hyuri.kanji_study.domain.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserDto {
    private Long userId;                // 유저 고유 ID 값
    private String loginId;             // 유저 아이디
    private String nickname;            // 유저 닉네임
    private String password;            // 유저 패스워드
    private String role;                // 유저 권한
    private LocalDateTime createdAt;    // 만든 일 자
    private LocalDateTime lastLoginAt;  // 마지막 로그인 시간
}
