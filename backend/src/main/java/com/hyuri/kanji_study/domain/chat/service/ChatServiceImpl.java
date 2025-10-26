package com.hyuri.kanji_study.domain.chat.service;

import com.hyuri.kanji_study.domain.chat.dto.ChatMessageRes;
import com.hyuri.kanji_study.domain.chat.dto.ChatSendReq;
import com.hyuri.kanji_study.domain.chat.entity.ChatMessageEntity;
import com.hyuri.kanji_study.domain.chat.repository.ChatMessageRepository;
import com.hyuri.kanji_study.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Service
public class ChatServiceImpl implements ChatService {
    private final ChatAccessService accessService;
    private final ChatMessageRepository messageRepo;
    private final UserRepository userRepo;

    @Override
    @Transactional
    public ChatMessageRes saveMessage(String senderLoginId, ChatSendReq req) {
        Long userId = accessService.getUserIdByLoginId(senderLoginId);
        // 방 참여자 검증
        if (!accessService.isParticipant(req.roomId(), userId)) {
            throw new IllegalArgumentException("방 참여자가 아님: room=" + req.roomId());
        }

        var saved = messageRepo.save(
                ChatMessageEntity.builder()
                        .roomId(req.roomId())
                        .senderId(userId)
                        .message(req.text())
                        .build()
        );
        return new ChatMessageRes(
                saved.getRoomId(),
                senderLoginId,
                saved.getMessage(),
                saved.getCreatedAt()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public Slice<ChatMessageRes> getHistory(Long roomId, Pageable pageable) {
        return messageRepo.findByRoomIdOrderByCreatedAtDesc(roomId, pageable)
                .map(e -> {
                    String senderLoginId = userRepo.findById(e.getSenderId())
                            .map(u -> u.getLoginId())
                            .orElse(null); // 삭제된 유저면 null 허용

                    return new ChatMessageRes(
                            e.getRoomId(),
                            senderLoginId,
                            e.getMessage(),
                            e.getCreatedAt()
                    );
                });
    }
}
