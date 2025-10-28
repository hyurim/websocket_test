import { useEffect, useRef, useState } from "react";
import { useParams } from "react-router-dom";
import { useAuth } from "../auth/useAuth";
import { joinRoom, leaveRoom, fetchHistory } from "../services/chat";
import { connectChat, sendChat } from "../services/stomp";

export default function ChatRoom() {
  const { roomId: roomIdParam } = useParams();
  const roomId = Number(roomIdParam) || 1;

  const { token, user } = useAuth();
  const [messages, setMessages] = useState([]);
  const [input, setInput] = useState("");
  const clientRef = useRef(null);
  const listRef = useRef(null);

  useEffect(() => {
    if (!token) return;
    joinRoom(roomId, token).catch(console.error);
    return () => {
      leaveRoom(roomId, token).catch(console.error);
    };
  }, [roomId, token]);

  useEffect(() => {
    if (!token) return;
    fetchHistory(roomId, 0, 50, token)
      .then((slice) => {
        const arr = (slice?.content || []).reverse();
        setMessages(arr);
        scrollToBottom();
      })
      .catch(console.error);
  }, [roomId, token]);

  useEffect(() => {
    if (!token) return;
    const client = connectChat(roomId, token, (raw) => {
			// 서버 응답 어떤 키로 와도 안전하게 매핑
			const msg = {
				sender: raw?.sender || raw?.user || raw?.nickname || raw?.loginId || "unknown",
				text:   raw?.text   || raw?.message || raw?.content || raw?.body || "",
				ts:     raw?.createdAt || raw?.timestamp || Date.now(),
			};
			setMessages((prev) => {
				const next = [...prev, msg];
        setTimeout(scrollToBottom, 0);
        return next;
      });
    });
    clientRef.current = client;
    return () => client?.deactivate();
  }, [roomId, token]);

  function scrollToBottom() {
    if (!listRef.current) return;
    listRef.current.scrollTop = listRef.current.scrollHeight;
  }

  function handleSend() {
    const text = input.trim();
    if (!text) return;
    const mine = {
			sender: user?.nickname || user?.loginId || "me",
			text,
			_local: true,
			ts: Date.now(),
			};
			setMessages((prev) => [...prev, mine]);
			setTimeout(scrollToBottom, 0);
			
			sendChat(clientRef.current, roomId, text).catch((e) => {
			console.error(e);
			setMessages((prev) => prev.filter((m) => m !== mine));
			alert("메시지 전송 실패");
			});
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
        {messages.map((m, i) => {
          const mine =
            user?.nickname === m.sender || user?.loginId === m.sender;
          return (
            <div
              key={i}
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
                  <div style={{ fontSize: 12, color: "#666", marginBottom: 2 }}>
                    {m.sender}
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
