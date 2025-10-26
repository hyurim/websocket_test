package com.hyuri.kanji_study.common.security;

import com.hyuri.kanji_study.domain.user.entity.UserEntity;
import com.hyuri.kanji_study.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {
    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String loginId) throws UsernameNotFoundException {
        log.info("로그인 시도: loginId = {}", loginId);

        // userId를 사용하여 사용자 조회 (문자열 기반 조회)
        UserEntity userEntity = userRepository.findByLoginId(loginId)
                .orElseThrow(() -> new UsernameNotFoundException("아이디를 찾을 수 없습니다: " + loginId));

        log.debug("조회된 사용자 정보: {}", userEntity);

        AuthenticatedUser authenticatedUser = AuthenticatedUser.builder()
                .loginId(userEntity.getLoginId())
                .password(userEntity.getPassword())
                .nickname(userEntity.getNickname())
                .role(userEntity.getRole() == null ? "USER" : userEntity.getRole())
                .build();

        log.debug("인증된 사용자 정보: {}", authenticatedUser);
        return authenticatedUser;
    }
}
