// ChatRoom.jsx
import { useEffect, useRef, useState } from "react";
import { useParams } from "react-router-dom";
import { useAuth } from "../auth/useAuth";
import { joinRoom, leaveRoom, fetchHistory } from "../services/chat";
import { connectChat, sendChat } from "../services/stomp";

// --- 정규화 & 키 유틸 ---
function normalizeMsg(raw, fallbackRoomId) {
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

  const text =
    raw.text ??
    raw.content ??
    raw.message ??
    (typeof raw.body === "string" ? raw.body : "");

  const clientTmpId =
    raw.clientTmpId ??
    (typeof crypto !== "undefined" && crypto.randomUUID
      ? crypto.randomUUID()
      : `tmp_${Math.random().toString(36).slice(2)}`);

  return { ...raw, roomId, id, ts, sender, text, clientTmpId };
}

function msgKey(m) {
  if (m.id) return `${m.roomId}-${m.id}`;
  return `${m.roomId}-${m.ts ?? "t"}-${m.sender ?? "s"}-${m.clientTmpId}`;
}

export default function ChatRoom() {
  const { roomId: roomIdParam } = useParams();
  const roomId = Number(roomIdParam) || 1;

  const { accessToken, user, loading } = useAuth();

  const [messages, setMessages] = useState([]);
  const [input, setInput] = useState("");

  // ✅ 추가: 방 세팅(참가 등록+히스토리 로드)이 끝났는지 여부
  const [ready, setReady] = useState(false);

  const clientRef = useRef(null);
  const listRef = useRef(null);
  const seenKeysRef = useRef(new Set());

  // 방 바뀌면 seenKeys 초기화
  useEffect(() => {
    seenKeysRef.current = new Set();
  }, [roomId]);

  // ✅ 1) 방 참여 + 히스토리 로드 + 정렬 + ready=true
  useEffect(() => {
    if (!accessToken) return;
    let canceled = false;

    (async () => {
      try {
        // 1. 방 참여 (DB에 chat_participant 넣는 로직이라고 가정)
        await joinRoom(roomId, accessToken);
        if (canceled) return;

        // 2. 히스토리 로드
        const slice = await fetchHistory(roomId, 0, 300, accessToken);

        const rawList = Array.isArray(slice?.content)
          ? slice.content
          : Array.isArray(slice)
          ? slice
          : [];

        // 3. normalize
        const arr = rawList.map((r) => normalizeMsg(r, roomId));

        // 4. 중복 제거
        const uniq = [];
        const localSeen = new Set();
        for (const m of arr) {
          const k = msgKey(m);
          if (localSeen.has(k)) continue;
          localSeen.add(k);
          uniq.push(m);
        }

        // 5. 오래된 -> 최신 순으로 정렬
        uniq.sort((a, b) => {
          const aHasTs = !!a.ts;
          const bHasTs = !!b.ts;

          if (aHasTs && bHasTs) {
            const at = new Date(a.ts).getTime();
            const bt = new Date(b.ts).getTime();
            if (at !== bt) return at - bt; // 더 옛날이 앞으로
          }

          const aId = a.id ?? 0;
          const bId = b.id ?? 0;
          return aId - bId;
        });

        // 6. 중복관리 세트 업데이트
        seenKeysRef.current = new Set(uniq.map((m) => msgKey(m)));

        // 7. 메시지 상태 저장
        if (!canceled) {
          setMessages(uniq);
        }

        // 8. 스크롤 맨 아래로
        setTimeout(() => {
          if (!listRef.current) return;
          listRef.current.scrollTop = listRef.current.scrollHeight;
        }, 0);

        // ✅ 9. 이제 채팅 가능하다고 표시
        if (!canceled) {
          setReady(true);
        }
      } catch (e) {
        console.error("[history load fail]", e);
      }
    })();

    // cleanup: 방 나갈 때 leaveRoom
    return () => {
      canceled = true;
      setReady(false); // 방 나가면 다시 false
      if (!accessToken || !roomId) return;
      leaveRoom(roomId, accessToken).catch(() => {});
    };
  }, [roomId, accessToken]);

  // ✅ 2) 실시간(STOMP) 연결은 ready === true일 때만
  useEffect(() => {
    if (!ready) return;          // 아직 방에 join 등록 안 끝났으면 연결 안 함
    if (loading) return;
    const wsToken = accessToken || sessionStorage.getItem("access_token");
    if (!wsToken) return;

    const handleMessage = (raw) => {
      const m = normalizeMsg(raw, roomId);
      const k = msgKey(m);

      // 중복 방지
      if (seenKeysRef.current.has(k)) return;
      seenKeysRef.current.add(k);

      setMessages((prev) => {
        if (prev.some((x) => msgKey(x) === k)) return prev;
        return [...prev, m]; // 최신은 배열 뒤에 붙음 -> 화면 하단
      });

      // 새 메시지 받을 때마다 스크롤 맨 아래
      requestAnimationFrame(() => {
        requestAnimationFrame(() => {
          if (!listRef.current) return;
          listRef.current.scrollTop = listRef.current.scrollHeight;
        });
      });
    };

    // 소켓 연결
    const client = connectChat(roomId, wsToken, handleMessage);
    clientRef.current = client;

    // 언마운트/방 전환 시 소켓 종료
    return () => {
      try {
        client.deactivate();
      } catch {}
    };
  }, [roomId, accessToken, loading, ready]);

	function handleSend() {
		const text = input.trim();
		if (!text) return;
	
		console.log("[DEBUG] ready =", ready);
	
		if (!ready) {
			alert("방 준비 중입니다. 잠시 후 다시 시도해주세요.");
			return;
		}
	
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

  return (
    <div style={{ padding: 16 }}>
      <h2>Room #{roomId}</h2>

      <div
        ref={listRef}
        style={{
          height: 420,
          overflowY: "auto",
          border: "1px solid #ddd",
          borderRadius: 8,
          padding: 12,
          marginBottom: 12,
          background: "#fafafa",
        }}
      >
        {messages.map((m) => {
          const k = msgKey(m);
          const mine =
            user?.nickname === m.sender || user?.loginId === m.sender;
          return (
            <div
              key={k}
              style={{
                display: "flex",
                justifyContent: mine ? "flex-end" : "flex-start",
                margin: "6px 0",
              }}
            >
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
                  <div
                    style={{
                      fontSize: 12,
                      color: "#666",
                      marginBottom: 2,
                    }}
                  >
                    {m.sender ?? "unknown"}
                  </div>
                )}
                <div
                  style={{
                    whiteSpace: "pre-wrap",
                    wordBreak: "break-word",
                    fontSize: 14,
                    lineHeight: 1.4,
                  }}
                >
                  {m.text}
                </div>
              </div>
            </div>
          );
        })}
      </div>

      <div style={{ display: "flex", gap: 8 }}>
        <textarea
          rows={1}
          style={{
            flex: 1,
            padding: 10,
            borderRadius: 8,
            border: "1px solid #ddd",
          }}
          placeholder="메시지 입력… (Enter 전송, Shift+Enter 줄바꿈)"
          value={input}
          onChange={(e) => setInput(e.target.value)}
          onKeyDown={handleKeyDown}
        />
        <button
          onClick={handleSend}
          style={{
            padding: "10px 14px",
            borderRadius: 8,
            border: "1px solid #004488",
            background: "#004488",
            color: "white",
            cursor: "pointer",
          }}
        >
          보내기
        </button>
      </div>
    </div>
  );
}