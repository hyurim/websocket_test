package com.hyuri.kanji_study.domain.chat.service;

import com.hyuri.kanji_study.domain.chat.dto.ChatMessageRes;
import com.hyuri.kanji_study.domain.chat.dto.ChatSendReq;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

public interface ChatService {
    ChatMessageRes saveMessage(String senderLoginId, ChatSendReq req);
    Slice<ChatMessageRes> getHistory(Long roomId, Pageable pageable);
}
