import { Routes, Route, NavLink, useNavigate } from "react-router-dom";
import { useState } from "react";
import { useAuth } from "./auth/useAuth";
import Home from "./pages/Home";
import Login from "./pages/User/Login";
import Signup from "./pages/User/Signup";
import Logout from "./pages/User/Logout";
import ChatRoom from "./pages/ChatRoom";

const App = () => {
  const { user } = useAuth();
  const navigate = useNavigate();

  // 모달 열림 상태 & 입력값 상태
  const [openModal, setOpenModal] = useState(false);
  const [roomInput, setRoomInput] = useState("");

  const handleOpenModal = () => {
    setOpenModal(true);
  };

  const handleCloseModal = () => {
    setOpenModal(false);
    setRoomInput("");
  };

  const handleConfirm = () => {
    if (roomInput && roomInput.trim() !== "") {
      const roomId = roomInput.trim();
      navigate(`/chat/${roomId}`);
    }
    setOpenModal(false);
  };

  return (
    <div
      style={{
        maxWidth: 640,
        margin: "0 auto",
        padding: 16,
        position: "relative",
      }}
    >
      {/* 헤더 */}
      <header
        style={{
          display: "flex",
          gap: 12,
          alignItems: "center",
          marginBottom: 16,
          borderBottom: "1px solid #ddd",
          paddingBottom: 12,
          flexWrap: "wrap",
        }}
      >
        <h1 style={{ fontSize: 22, marginRight: "auto" }}>Kanji Study</h1>

        <NavLink to="/" end style={linkStyle}>
          홈
        </NavLink>

        {!user ? (
          <>
            <NavLink to="/login" style={linkStyle}>
              로그인
            </NavLink>
            <NavLink to="/signup" style={linkStyle}>
              회원가입
            </NavLink>
          </>
        ) : (
          <>
            <span
              style={{
                fontSize: 12,
                color: "#666",
                whiteSpace: "nowrap",
              }}
            >
              {user?.nickname} 님
            </span>
            <NavLink to="/logout" style={linkStyle}>
              로그아웃
            </NavLink>
          </>
        )}

        {/* 채팅방 입장 버튼 */}
        <button
          onClick={handleOpenModal}
          style={{
            padding: "6px 10px",
            borderRadius: 8,
            border: "1px solid #004488",
            background: "#004488",
            color: "#fff",
            fontSize: 12,
            lineHeight: 1.2,
            cursor: "pointer",
            whiteSpace: "nowrap",
          }}
        >
          채팅방 입장
        </button>
      </header>

      {/* 라우터 영역 */}
      <Routes>
        <Route path="/" element={<Home />} />
        <Route path="/chat/:roomId" element={<ChatRoom />} />
        <Route path="/login" element={<Login />} />
        <Route path="/signup" element={<Signup />} />
        <Route path="/logout" element={<Logout />} />
        <Route path="*" element={<NotFound />} />
      </Routes>

      {/* 푸터 */}
      <footer style={{ marginTop: 24, fontSize: 12, color: "#666" }}>
        © {new Date().getFullYear()} Kanji Study
      </footer>

      {/* 모달 */}
      {openModal && (
        <>
          {/* 배경 어둡게 */}
          <div
            style={{
              position: "fixed",
              left: 0,
              top: 0,
              width: "100vw",
              height: "100vh",
              background: "rgba(0,0,0,0.4)",
              zIndex: 999,
            }}
            onClick={handleCloseModal} // 배경 눌러도 닫히게
          />

          {/* 모달 카드 */}
          <div
            style={{
              position: "fixed",
              left: "50%",
              top: "50%",
              transform: "translate(-50%, -50%)",
              background: "#fff",
              width: "90%",
              maxWidth: 320,
              borderRadius: 12,
              boxShadow: "0 12px 32px rgba(0,0,0,0.2)",
              border: "1px solid rgba(0,0,0,0.06)",
              zIndex: 1000,
              padding: "16px 16px 12px",
              boxSizing: "border-box",
            }}
          >
            <div
              style={{
                fontSize: 15,
                fontWeight: 600,
                color: "#111",
                marginBottom: 8,
              }}
            >
              방 번호 입력
            </div>

            <div
              style={{
                fontSize: 13,
                color: "#666",
                marginBottom: 12,
                lineHeight: 1.4,
              }}
            >
              입장할 채팅방 번호를 입력하세요.
            </div>

            <input
              value={roomInput}
              onChange={(e) => setRoomInput(e.target.value)}
              placeholder="예: 1"
              style={{
                width: "100%",
                fontSize: 14,
                padding: "8px 10px",
                borderRadius: 8,
                border: "1px solid #ccc",
                outline: "none",
                marginBottom: 12,
              }}
            />

            <div
              style={{
                display: "flex",
                justifyContent: "flex-end",
                gap: 8,
              }}
            >
              <button
                onClick={handleCloseModal}
                style={{
                  background: "white",
                  border: "1px solid #ccc",
                  borderRadius: 8,
                  fontSize: 13,
                  padding: "6px 10px",
                  cursor: "pointer",
                  color: "#666",
                }}
              >
                취소
              </button>

              <button
                onClick={handleConfirm}
                style={{
                  background: "#004488",
                  border: "none",
                  borderRadius: 8,
                  fontSize: 13,
                  padding: "6px 10px",
                  cursor: "pointer",
                  color: "white",
                  fontWeight: 500,
                }}
              >
                확인
              </button>
            </div>
          </div>
        </>
      )}
    </div>
  );
};

export default App;

const linkStyle = ({ isActive }) => ({
  padding: "6px 10px",
  borderRadius: 8,
  textDecoration: "none",
  border: "1px solid #ddd",
  color: isActive ? "white" : "#333",
  background: isActive ? "#004488" : "transparent",
});

const NotFound = () => <div>페이지를 찾을 수 없습니다.</div>;