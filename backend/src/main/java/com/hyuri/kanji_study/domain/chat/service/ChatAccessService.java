package com.hyuri.kanji_study.domain.chat.service;

import com.hyuri.kanji_study.domain.chat.entity.ChatParticipantId;
import com.hyuri.kanji_study.domain.chat.repository.ChatParticipantRepository;
import com.hyuri.kanji_study.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class ChatAccessService {
    private final UserRepository userRepo;
    private final ChatParticipantRepository participantRepo;

    @Transactional(readOnly = true)
    public Long getUserIdByLoginId(String loginId) {
        return userRepo.findByLoginId(loginId)
                .orElseThrow(() -> new IllegalArgumentException("사용자 없음: " + loginId))
                .getUserId();
    }

    @Transactional(readOnly = true)
    public boolean isParticipant(Long roomId, Long userId) {
        return participantRepo.existsById(new ChatParticipantId(roomId, userId));
    }

    @Transactional
    public void join(Long roomId, Long userId) {
        if (!isParticipant(roomId, userId)) {
            var entity = com.hyuri.kanji_study.domain.chat.entity.ChatParticipantEntity.builder()
                    .id(new ChatParticipantId(roomId, userId))
                    .build();
            participantRepo.save(entity);
        }
    }

    @Transactional
    public void leave(Long roomId, Long userId) {
        participantRepo.deleteById(new ChatParticipantId(roomId, userId));
    }

}
