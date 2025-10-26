import React, { useState } from "react";
import { useNavigate } from "react-router-dom";
import styled from "styled-components";

const Container = styled.div`
  width: 600px;
  margin: 50px auto;
  padding: 30px;
  border: 2px solid #f5a623;
  border-radius: 10px;
  font-family: "Arial", sans-serif;
`;

const Title = styled.h2`
  color: #000000;
  text-align: center;
  margin-bottom: 40px;
  font-size: 26px;
  border-bottom: 2px solid #f5a623;
  line-height: 3;
`;

const Form = styled.form`
  display: flex;
  flex-direction: column;
`;

const Label = styled.label`
  display: flex;
  align-items: center;
  margin-bottom: 15px;
  font-size: 15px;
  color: #000000;
`;

const LabelText = styled.span`
  width: 100px;
  font-weight: bold;
  margin-left: 30px;
`;

const Input = styled.input`
  flex: 1;
  padding: 13px;
  border: none;
  border-radius: 45px;
  font-size: 15px;
  background-color: #fff4e9;
`;

const ButtonContainer = styled.div`
  display: flex;
  justify-content: center;
  gap: 30px;
  margin-top: 20px;
`;


const Button = styled.button`
  width: 100%;
  max-width: 100px;
  background-color: #ff8c00;
  color: white;
  padding: 12px;
  border: none;
  border-radius: 45px;
  font-size: 14px;
  cursor: pointer;
  &:hover {
    background-color: #e07c00;
  }
`;

const Signup = () => {
  const [form, setForm] = useState({
    loginId: "",
    password: "",
    confirmPassword: "",
    nickname: "",
  });

  const [error, setError] = useState("");
  const navigate = useNavigate();

  // 입력값 변경 핸들러
  const handleChange = (e) => {
    const { name, value } = e.target;
    setForm((prevForm) => ({
      ...prevForm,
      [name]: value,
    }));
  };

  // 폼 제출 핸들러
  const handleSubmit = async (e) => {
    e.preventDefault();

    if (form.password !== form.confirmPassword) {
      setError("비밀번호가 일치하지 않습니다.");
      return;
    }

    const signupData = {
      loginId: form.loginId,
      password: form.password,
      nickname: form.nickname,
    };

    console.log("폼 제출 데이터:", signupData);

    try {
		  const API_BASE = import.meta.env.VITE_API_BASE || '';
      const response = await fetch(`${API_BASE}/api/user/signup`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify(signupData),
      });

      if (response.ok) {
        console.log("회원가입 성공");
        navigate("/", { replace: true });
      } else {
        console.error("회원가입 실패");
      }
    } catch (error) {
      console.error("서버 오류", error);
    }
  };

  return (
    <Container>
      <Title>회원가입</Title>
      <Form onSubmit={handleSubmit}>
        <Label>
          <LabelText>아이디</LabelText>
          <Input
            type="text"
            name="loginId"
            value={form.loginId}
            onChange={handleChange}
            required
          />
        </Label>

        <Label>
          <LabelText>비밀번호</LabelText>
          <Input
            type="password"
            name="password"
            value={form.password}
            onChange={handleChange}
            required
          />
        </Label>

        <Label>
          <LabelText>비밀번호 확인</LabelText>
          <Input
            type="password"
            name="confirmPassword"
            value={form.confirmPassword}
            onChange={handleChange}
            required
          />
        </Label>

        <Label>
          <LabelText>이름</LabelText>
          <Input
            type="text"
            name="nickname"
            value={form.nickname}
            onChange={handleChange}
          />
        </Label>


        <ButtonContainer>
          <Button type="submit">가입</Button>
          <Button type="button">취소</Button>
        </ButtonContainer>
      </Form>
    </Container>
  );
};

export default Signup;