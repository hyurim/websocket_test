// ChatRoom.jsx
import { useEffect, useRef, useState } from "react";
import { useParams } from "react-router-dom";
import { useAuth } from "../auth/useAuth";
import { joinRoom, leaveRoom, fetchHistory } from "../services/chat";
import { connectChat, sendChat } from "../services/stomp";

// --- 정규화 & 키 유틸 ---
function normalizeMsg(raw, fallbackRoomId) {
  // 서버/클라 케이스를 모두 흡수
  const roomId = raw.roomId ?? fallbackRoomId;
  const id = raw.messageId ?? raw.id ?? raw.clientMsgId ?? null;
  const ts = raw.createdAt ?? raw.ts ?? raw.time ?? null;
  const sender =
    raw.sender ??
    raw.senderId ??
    raw.user ??
    raw.nickname ??
    raw.loginId ??
    null;

  // 내용 필드 통일 (content | text | message)
  const text =
    raw.text ??
    raw.content ??
    raw.message ??
    (typeof raw.body === "string" ? raw.body : "");

  // 클라 임시 식별자 보강(없을 때만)
  const clientTmpId =
    raw.clientTmpId ?? (typeof crypto !== "undefined" && crypto.randomUUID ? crypto.randomUUID() : `tmp_${Math.random().toString(36).slice(2)}`);

  return { ...raw, roomId, id, ts, sender, text, clientTmpId };
}

function msgKey(m) {
  // 가능하면 서버가 주는 유일 id 사용
  if (m.id) return `${m.roomId}-${m.id}`;
  // 없으면 (room + ts + sender + clientTmpId)로 안정 키
  return `${m.roomId}-${m.ts ?? "t"}-${m.sender ?? "s"}-${m.clientTmpId}`;
}

export default function ChatRoom() {
  const { roomId: roomIdParam } = useParams();
  const roomId = Number(roomIdParam) || 1;

  const { accessToken, user, loading } = useAuth();
  const [messages, setMessages] = useState([]);
  const [input, setInput] = useState("");
  const clientRef = useRef(null);
  const listRef = useRef(null);

  const seenKeysRef = useRef(new Set());

  useEffect(() => {
    seenKeysRef.current = new Set();
  }, [roomId]);

  useEffect(() => {
    if (!accessToken) return;
    joinRoom(roomId, accessToken).catch(console.error);
    return () => leaveRoom(roomId, accessToken).catch(console.error);
  }, [roomId, accessToken]);

  // 히스토리: 역순 → 정규화 → 디듀프 → 세팅
  useEffect(() => {
    if (!accessToken) return;
    fetchHistory(roomId, 0, 50, accessToken)
      .then((slice) => {
        const arr = (slice?.content || []).reverse().map((r) => normalizeMsg(r, roomId));

        // 디듀프
        const uniq = [];
        const localSeen = new Set();
        for (const m of arr) {
          const k = msgKey(m);
          if (localSeen.has(k)) continue;
          localSeen.add(k);
          uniq.push(m);
        }

        // 이후 실시간 중복 방지용으로 seenKeysRef에도 반영
        seenKeysRef.current = new Set(localSeen);

        setMessages(uniq);
        scrollToBottom();
      })
      .catch(console.error);
  }, [roomId, accessToken]);

  // 실시간
  useEffect(() => {
    if (loading) return;
    const wsToken = accessToken || sessionStorage.getItem("access_token");
    if (!wsToken) return;

    const handleMessage = (raw) => {
      const m = normalizeMsg(raw, roomId);
      const k = msgKey(m);

      if (seenKeysRef.current.has(k)) return;
      seenKeysRef.current.add(k);

      setMessages((prev) => {
        // 혹시 몰라 prev에서도 한 번 더 체크
        if (prev.some((x) => msgKey(x) === k)) return prev;
        return [...prev, m];
      });

      // 스크롤
      setTimeout(scrollToBottom, 0);
    };

    const client = connectChat(roomId, wsToken, handleMessage);
    clientRef.current = client;

    return () => {
      try { client.deactivate(); } catch {}
    };
  }, [roomId, accessToken, loading]);

  function scrollToBottom() {
    if (!listRef.current) return;
    listRef.current.scrollTop = listRef.current.scrollHeight;
  }

  function handleSend() {
    const text = input.trim();
    if (!text) return;
    const ok = sendChat(clientRef.current, roomId, text);
    if (!ok) {
      alert("메시지 전송 실패(연결 끊김)");
      return;
    }
    setInput("");
  }

  function handleKeyDown(e) {
    if (e.key === "Enter" && !e.shiftKey) {
      e.preventDefault();
      handleSend();
    }
  }

	useEffect(() => {
		// 메시지가 바뀔 때마다 자동 스크롤
		if (messages.length > 0) {
			scrollToBottom();
		}
	}, [messages]);

  return (
    <div style={{ padding: 16 }}>
      <h2>Room #{roomId}</h2>
      <div
        ref={listRef}
        style={{
          height: 420, overflowY: "auto", border: "1px solid #ddd",
          borderRadius: 8, padding: 12, marginBottom: 12, background: "#fafafa",
        }}
      >
        {messages.map((m) => {
          const k = msgKey(m);
          const mine = user?.nickname === m.sender || user?.loginId === m.sender;
          return (
            <div key={k} style={{ display: "flex", justifyContent: mine ? "flex-end" : "flex-start", margin: "6px 0" }}>
              <div
                style={{
                  maxWidth: "70%",
                  padding: "8px 10px",
                  borderRadius: 10,
                  background: mine ? "#004488" : "white",
                  color: mine ? "white" : "#222",
                  border: mine ? "none" : "1px solid #e8e8e8",
                  boxShadow: "0 1px 2px rgba(0,0,0,0.04)",
                }}
              >
                {!mine && (
                  <div style={{ fontSize: 12, color: "#666", marginBottom: 2 }}>
                    {m.sender ?? "unknown"}
                  </div>
                )}
                <div>{m.text}</div>
              </div>
            </div>
          );
        })}
      </div>

      <div style={{ display: "flex", gap: 8 }}>
        <textarea
          rows={1}
          style={{ flex: 1, padding: 10, borderRadius: 8, border: "1px solid #ddd" }}
          placeholder="메시지 입력… (Enter 전송, Shift+Enter 줄바꿈)"
          value={input}
          onChange={(e) => setInput(e.target.value)}
          onKeyDown={handleKeyDown}
        />
        <button
          onClick={handleSend}
          style={{
            padding: "10px 14px", borderRadius: 8,
            border: "1px solid #004488", background: "#004488",
            color: "white", cursor: "pointer",
          }}
        >
          보내기
        </button>
      </div>
    </div>
  );
}