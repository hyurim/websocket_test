package com.hyuri.kanji_study.domain.user.service;

import com.hyuri.kanji_study.domain.user.dto.UserDto;
import com.hyuri.kanji_study.domain.user.entity.UserEntity;
import com.hyuri.kanji_study.domain.user.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Slf4j
@RequiredArgsConstructor
@Service
@Transactional
public class UserAccountServiceImpl implements UserAccountService {

    private final UserRepository userRepo;
    private final PasswordEncoder passwordEncoder;

    // user
    @Override
    public boolean idCheck(String searchId) {
        return !userRepo.existsByLoginId(searchId);
    }


    @Override
    public UserDto join(UserDto user) {

        if (userRepo.existsByLoginId(user.getLoginId())) {
            throw new IllegalArgumentException("이미 사용 중인 아이디입니다.");
        }

        UserEntity entity = UserEntity.builder()
                .loginId(user.getLoginId())
                .nickname(user.getNickname())
                .password(passwordEncoder.encode(user.getPassword()))
                .role(user.getRole())
                .build();

        userRepo.save(entity);
        return user;
    }


    // 현재는 필요 없음. (아이디 조회 로직)
    @Override
    public UserDto getMember(String loginId) {
        UserEntity entity = userRepo.findByLoginId(loginId)
                .orElseThrow(() -> new EntityNotFoundException(
                        loginId + " : 아이디가 없습니다."));
        UserDto dto = UserDto.builder()
                .loginId(entity.getLoginId())
                .nickname(entity.getNickname())
                .role(entity.getRole())
                .build();
        return dto;
    }

    @Transactional
    public void updateLoginSuccess(UserEntity user) {
        LocalDate today = LocalDate.now();

        if (user.getLastLoginAt() != null) {
            LocalDate lastLoginDate = user.getLastLoginAt().toLocalDate();

            // 어제 로그인했다면 streakDays +1
            if (lastLoginDate.plusDays(1).equals(today)) {
                user.setStreakDays(user.getStreakDays() + 1);
            }
            // 오늘 이미 로그인했으면 streakDays 유지
            else if (lastLoginDate.equals(today)) {
                // 아무 처리 안 함
            }
            // 며칠 건너뛰었을 경우 streak 리셋
            else {
                user.setStreakDays(1);
            }
        } else {
            // 첫 로그인
            user.setStreakDays(1);
        }

        // 마지막 로그인 시간 갱신
        user.setLastLoginAt(LocalDateTime.now());
        userRepo.save(user);
    }
}
