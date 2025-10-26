package com.hyuri.kanji_study.domain.chat.controller;

import com.hyuri.kanji_study.domain.chat.dto.ChatMessageRes;
import com.hyuri.kanji_study.domain.chat.dto.ChatSendReq;
import com.hyuri.kanji_study.domain.chat.service.ChatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RequiredArgsConstructor
@RestController
@Slf4j
public class ChatController {
    private final ChatService chatService;
    private final SimpMessagingTemplate template;

    @MessageMapping("/chat.send")
    public void send(@Valid ChatSendReq req, Principal principal) {
        String sender = principal.getName(); // loginId
        ChatMessageRes saved = chatService.saveMessage(sender, req);
        template.convertAndSend("/topic/rooms/" + req.roomId(), saved);
    }
}
