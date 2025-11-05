import { useEffect, useState } from "react";
import {
  fetchNotifications,
  fetchUnreadCount,
  markNotificationRead,
} from "../../services/notification";

export default function NotificationList() {
  const [list, setList] = useState([]);
  const [unreadCount, setUnreadCount] = useState(0);

  const loadData = async () => {
    const [notis, count] = await Promise.all([
      fetchNotifications(),
      fetchUnreadCount(),
    ]);
    setList(notis);
    setUnreadCount(count);
  };

  useEffect(() => {
    loadData();
  }, []);

  const handleRead = async (id) => {
    await markNotificationRead(id);
    await loadData();
  };

  return (
    <div className="notification-panel">
      <h2>알림 ({unreadCount} 미확인)</h2>
      <ul>
        {list.map((n) => (
          <li
            key={n.id}
            style={{
              backgroundColor: n.isRead ? "#fafafa" : "#e8f0fe",
              cursor: "pointer",
              marginBottom: 8,
              padding: 8,
              borderRadius: 6,
            }}
            onClick={() => handleRead(n.id)}
          >
            <div style={{ fontWeight: "bold" }}>{n.type}</div>
            <div>{n.message}</div>
            <div style={{ fontSize: 12, color: "#666" }}>
              {new Date(n.createdAt).toLocaleString()}
            </div>
          </li>
        ))}
      </ul>
    </div>
  );
}