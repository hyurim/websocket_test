import SockJS from "sockjs-client";
import { Client } from "@stomp/stompjs";

export function connectChat(roomId, token, onMessage) {
  const socket = new SockJS(import.meta.env.VITE_API_BASE + "/ws/chat");

  const client = new Client({
    webSocketFactory: () => socket,
    connectHeaders: token ? { Authorization: `Bearer ${token}` } : {},
    reconnectDelay: 2000,
    debug: () => {},
  });

  client.onConnect = () => {
    client.subscribe(`/topic/room.${roomId}`, (frame) => {
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
    destination: `/app/chat/${roomId}`,
    body: JSON.stringify({ text }),
    headers: token ? { Authorization: `Bearer ${token}` } : {},
  });
}
