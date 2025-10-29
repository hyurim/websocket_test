package com.hyuri.kanji_study.domain.chat.controller;

import com.hyuri.kanji_study.domain.chat.dto.ChatHistoryRes;
import com.hyuri.kanji_study.domain.chat.dto.ChatMessageRes;
import com.hyuri.kanji_study.domain.chat.dto.ChatSendReq;
import com.hyuri.kanji_study.domain.chat.repository.ChatParticipantRepository;
import com.hyuri.kanji_study.domain.chat.service.ChatAccessService;
import com.hyuri.kanji_study.domain.chat.service.ChatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatRestController {

    private final ChatAccessService accessService;
    private final ChatService chatService;
    private final ChatParticipantRepository participantRepo;

    @PostMapping("/rooms/{roomId}/join")
    public ResponseEntity<?> join(@PathVariable Long roomId, Principal principal) {
        String loginId = principal.getName();                    // 1) 로그인 아이디
        Long userId = accessService.getUserIdByLoginId(loginId); // 2) 유저 ID 조회
        accessService.join(roomId, userId);                      // 3) 멱등 join
        return ResponseEntity.ok().build();
    }

    @PostMapping("/rooms/{roomId}/leave")
    public ResponseEntity<?> leave(@PathVariable Long roomId, Principal principal) {
        String loginId = principal.getName();
        Long userId = accessService.getUserIdByLoginId(loginId);
        accessService.leave(roomId, userId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/rooms/{roomId}/messages")
    public Slice<ChatHistoryRes> history(@PathVariable Long roomId,
                                         @RequestParam(defaultValue = "50") int size,
                                         @RequestParam(defaultValue = "0") int page) {
        return chatService.getHistory(roomId, PageRequest.of(page, size))
                .map(m -> new ChatHistoryRes(
                        m.roomId(),
                        m.sender(),
                        m.text(),
                        m.sentAt()));
    }
}