package com.hyuri.kanji_study.domain.chat.service;

import com.hyuri.kanji_study.domain.chat.entity.ChatParticipantEntity;
import com.hyuri.kanji_study.domain.chat.entity.ChatParticipantId;
import com.hyuri.kanji_study.domain.chat.entity.ChatRoomEntity;
import com.hyuri.kanji_study.domain.chat.repository.ChatParticipantRepository;
import com.hyuri.kanji_study.domain.chat.repository.ChatRoomRepository;
import com.hyuri.kanji_study.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
@RequiredArgsConstructor
@Service
public class ChatAccessService {
    private final UserRepository userRepo;
    private final ChatParticipantRepository participantRepo;
    private final ChatRoomRepository roomRepo;



    @Transactional(readOnly = true)
    public Long getUserIdByLoginId(String loginId) {
        log.info("[JOIN] loginId={}", loginId);
        return userRepo.findByLoginId(loginId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "사용자 없음: " + loginId))
                .getUserId();

    }

    @Transactional(readOnly = true)
    public boolean isParticipant(Long roomId, Long userId) {
        return participantRepo.existsById(new ChatParticipantId(roomId, userId));
    }

    @Transactional
    public void join(Long roomId, Long userId) {
        // 1) 방도 멱등하게 생성
        roomRepo.insertIgnore(roomId, "Room " + roomId, userId);

        // 2) 참여자 멱등 업서트 (영향 행 수로 신규/기존 판단)
        int inserted = participantRepo.insertIgnore(roomId, userId);

        if (inserted == 1) {
            log.info("[JOIN] userId={} joined roomId={}", userId, roomId);
        } else {
            log.info("[JOIN] already participant userId={} roomId={}", userId, roomId);
        }
    }

    @Transactional
    public void leave(Long roomId, Long userId) {
        participantRepo.deleteById(new ChatParticipantId(roomId, userId));
    }

}
