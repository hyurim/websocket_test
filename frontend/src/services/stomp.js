// services/stomp.js
import SockJS from "sockjs-client";
import { Client } from "@stomp/stompjs";

const wsEndpoint = () => {
  const base = (import.meta.env.VITE_API_BASE || "").replace(/\/$/, "");
  return import.meta.env.DEV ? "/ws-chat" : `${base}/ws-chat`;
};

export function connectChat(roomId, token, onMessage) {
  const socket = new SockJS(wsEndpoint());

  const client = new Client({
    webSocketFactory: () => socket,
    connectHeaders: { Authorization: `Bearer ${token}` },
    reconnectDelay: 2000,
    heartbeatIncoming: 10000,
    heartbeatOutgoing: 10000,
    debug: () => {},
  });

  client.onConnect = () => {
    // 구독은 여기서 1번만!
    client.subscribe(`/topic/rooms/${roomId}`, (frame) => {
      try {
        const msg = JSON.parse(frame.body);
        onMessage?.(msg);
      } catch (e) {
        console.error("[STOMP] parse error", e, frame.body);
      }
    });
  };

  client.onStompError = (f) =>
    console.error("[STOMP] ERROR", f.headers?.message, f.body);
  client.onWebSocketError = (e) => console.error("[WS] SOCKET ERROR", e);
  client.onWebSocketClose = (e) =>
    console.warn("[WS] CLOSED", e?.code, e?.reason);
  client.onDisconnect = () => console.warn("[STOMP] DISCONNECTED");

  client.activate();
  return client;
}

export function sendChat(client, roomId, text) {
  if (!client || !client.connected) return false;
  client.publish({
    destination: `/app/chat.send`,
    body: JSON.stringify({ roomId, content: text }),
  });
  return true;
}
