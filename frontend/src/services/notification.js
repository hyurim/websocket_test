import api from "../api/axios";

// 알림 목록 조회
export const fetchNotifications = async () => {
  const res = await api.get("/notifications");
  return res.data;
};

// 안읽은 알림 수
export const fetchUnreadCount = async () => {
  const res = await api.get("/notifications/unread-count");
  return res.data;
};

// 알림 읽음 처리
export const markNotificationRead = async (id) => {
  await api.post(`/notifications/${id}/read`);
};
