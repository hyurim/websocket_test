import React, { useEffect } from "react";
import { useNavigate } from "react-router-dom";
import { useAuth } from "../../auth/useAuth";

export default function Logout(){
  const { logout } = useAuth();
  const nav = useNavigate();
  useEffect(() => {
    (async () => {
		await logout({ skipAuthRefresh: true });
      nav("/", { replace: true });
    })();
  }, []);
  return <div style={{padding:24}}>Logging out...</div>;
}
