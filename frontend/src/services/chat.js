import { apiGet, apiPost } from "./api";

export const joinRoom = (roomId, token) =>
  apiPost(`/api/chat/rooms/${roomId}/join`, token);

export const leaveRoom = (roomId, token) =>
  apiPost(`/api/chat/rooms/${roomId}/leave`, token);

export const fetchHistory = (roomId, page = 0, size = 50, token) =>
  apiGet(`/api/chat/rooms/${roomId}/messages?page=${page}&size=${size}`, token);
