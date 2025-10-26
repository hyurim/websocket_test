package com.hyuri.kanji_study.domain.user.service;

import com.hyuri.kanji_study.domain.user.dto.UserDto;
import com.hyuri.kanji_study.domain.user.entity.UserEntity;

public interface UserAccountService {

    //user
    UserDto join(UserDto user);
    boolean idCheck(String searchId);
    UserDto getMember(String loginId);
    void updateLoginSuccess(UserEntity user);
}
