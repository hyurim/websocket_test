import SockJS from "sockjs-client";
import { Client } from "@stomp/stompjs";

export const API_BASE = import.meta.env.VITE_API_BASE || "";

export function connectChat(roomId, token, onMessage) {
  const socket = new SockJS(`${API_BASE}`.replace(/\/$/, "") + "/ws-chat");

  const client = new Client({
    webSocketFactory: () => socket,
    connectHeaders: token ? { Authorization: `Bearer ${token}` } : {},
    reconnectDelay: 2000,
    debug: () => {},
    heartbeatIncoming: 10000,
    heartbeatOutgoing: 10000,
  });

  client.onConnect = () => {
    client.subscribe(`/topic/rooms/${roomId}`, (frame) => {
      const msg = JSON.parse(frame.body);
      onMessage(msg);
    });
  };

  client.activate();
  return client;
}

export function sendChat(client, roomId, text, token) {
  if (!client || !client.connected) return;
  client.publish({
    destination: `/app/chat.send`,
    body: JSON.stringify({ roomId, content: text }),
  });
}
