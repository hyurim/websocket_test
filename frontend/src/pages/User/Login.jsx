import React, { useState } from "react";
import styled from "styled-components";
import { useNavigate, useLocation, Link } from "react-router-dom";
import { useAuth } from "../../auth/AuthProvider"

const Container = styled.div`
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 100vh;
`;

const LoginForm = styled.form`
  width: 400px;
  margin-bottom: 10px;
  display: flex;
  flex-direction: column;
  gap: 20px;
`;

const InputBox = styled.div`
  font-family: "Pretendard-Regular", serif;
  width: 100%;
  border: 0.3px solid #a69b9b;
  border-radius: 30px;
  padding: 0 10px;
  display: flex;
  align-items: center;
  background-color: #ffffff;
  color: #a69b9b;
  font-size: 15px;
`;

const Label = styled.span`
  padding-right: 15px;
  padding-left: 15px;
  font-size: 15px;
  color: #a69b9b;
  white-space: nowrap;
`;

const Divider = styled.span`
  color: #a69b9b;
  margin: 0 8px;
`;

const Input = styled.input`
  font-family: "Pretendard-Regular", serif;
  flex: 1;               /* 가로 너비 자동 */
  height: 50px;
  font-size: 16px;
  border: none;
  border-radius: 30px;
  background: transparent;
  outline: none;
`;

const LoginButton = styled.button`
  font-family: "Pretendard-Regular", serif;
  width: 420px;
  height: 50px;
  background-color: #ffb770;
  border: none;
  border-radius: 30px;
  color: white;
  font-size: 16px;
  font-weight: bold;
  cursor: pointer;
  margin-bottom: 20px;
`;

const Links = styled.div`
  display: flex;
  justify-content: center;
  margin-bottom: 20px;
  border-bottom: 1px solid #a69b9b;
  line-height: 3;
`;

const StyledLink = styled.a`
  font-family: "Pretendard-Regular", serif;
  text-decoration: none;
  color: #999;
  font-size: 14px;
`;

const Separator = styled.span`
  font-family: "Pretendard-Regular", serif;
  color: #a69b9b;
  margin: 0 10px;
`;

const ErrorMsg = styled.p`
  color: #d33;
  text-align: center;
  margin: 0;
`;

const Login = () => {
  const { login } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const next = new URLSearchParams(location.search).get("next") || "/";

  const [loginId, setLoginId] = useState("");
  const [password, setPassword] = useState("");
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState("");

  const onSubmit = async (e) => {
    e.preventDefault();
    if (!loginId || !password) {
      setError("아이디와 비밀번호를 입력해주세요.");
      return;
    }
    setSubmitting(true);
    setError("");

    try {
      await login(loginId, password);
      navigate(next, { replace: true });
    } catch (err) {
      console.log("[LOGIN ERROR]", {
        status: err?.response?.status,
        data: err?.response?.data,
      });
      const msg =
        err?.response?.data ||
        err?.message ||
        "로그인에 실패했습니다. 아이디/비밀번호를 확인해주세요.";
      setError(typeof msg === "string" ? msg : "로그인 실패");
    } finally {
      setSubmitting(false);
    }
  };


  return (
    <Container>
      <LoginForm onSubmit={onSubmit}>
        <InputBox>
          <Label>아이디</Label>
          <Divider>|</Divider>
          <Input
            type="text"
            name="username"
            value={loginId}
            onChange={(e) => setLoginId(e.target.value)}
            placeholder="아이디 입력"
            autoComplete="username"
            disabled={submitting}
          />
        </InputBox>

        <InputBox>
          <Label>비밀번호</Label>
          <Divider>|</Divider>
          <Input
            type="password"
            name="current-password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            placeholder="비밀번호 입력"
            autoComplete="current-password"
            disabled={submitting}
          />
        </InputBox>

        <LoginButton type="submit" disabled={submitting}>
          {submitting ? "로그인 중..." : "로그인"}
        </LoginButton>

        {error && <ErrorMsg>{error}</ErrorMsg>}
      </LoginForm>

      <Links>
        <StyledLink href="./FindID">아이디/비밀번호 찾기</StyledLink>
        <Separator> | </Separator>
        <StyledLink href="/kanji-study/signUp">회원가입</StyledLink>
      </Links>
    </Container>
  );
};

export default Login;