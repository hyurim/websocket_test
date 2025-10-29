package com.hyuri.kanji_study.domain.chat.controller;

import com.hyuri.kanji_study.domain.chat.dto.ChatMessageRes;
import com.hyuri.kanji_study.domain.chat.dto.ChatSendReq;
import com.hyuri.kanji_study.domain.chat.service.ChatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@RequiredArgsConstructor
@Controller
@Slf4j
public class ChatController {
    private final ChatService chatService;
    private final SimpMessagingTemplate template;

    @MessageMapping("/chat.send")
    public void send(@Valid ChatSendReq req,
                     Principal principal,
                     org.springframework.messaging.simp.SimpMessageHeaderAccessor accessor) {

        String sender = null;
        if (principal != null) {
            sender = principal.getName();
        } else if (accessor != null && accessor.getSessionAttributes() != null) {
            Object lid = accessor.getSessionAttributes().get("loginId");
            if (lid != null) sender = lid.toString();
        }

        if (sender == null) {
            log.warn("[WS] principal null & session loginId 없음 → 인증 필요");
            throw new IllegalStateException("웹소켓 인증 필요");
        }

        log.info("[WS] SEND room={}, sender={}, content={}", req.roomId(), sender, req.content());

        ChatMessageRes saved = chatService.saveMessage(sender, req);
        template.convertAndSend("/topic/rooms/" + req.roomId(), saved);
    }
}
