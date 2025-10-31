import React, {
  createContext, useCallback, useContext, useEffect, useMemo, useRef, useState
} from "react";
import api, { setAccessTokenGetter, setOnTokenRefreshed, API_BASE } from "../api/axios";

const AuthCtx = createContext(null);

export function AuthProvider({ children }){
  const [accessToken, setAccessToken] = useState(() => sessionStorage.getItem("access_token") || "");
  const tokenRef = useRef(accessToken);
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);
  const [attemptedInitialRefresh, setAttemptedInitialRefresh] = useState(false);
  const [suppressAutoRefresh, setSuppressAutoRefresh] = useState(false);

  useEffect(() => { tokenRef.current = accessToken; }, [accessToken]);

  useEffect(() => {
    setAccessTokenGetter(() => tokenRef.current);
    setOnTokenRefreshed((t) => setAccessToken(t));
  }, []);

  useEffect(() => {
    if (accessToken) sessionStorage.setItem("access_token", accessToken);
    else sessionStorage.removeItem("access_token");
  }, [accessToken]);

  const fetchMe = useCallback(async () => {
    const t = tokenRef.current || sessionStorage.getItem("access_token");
    if (!t) {
      setUser(null);
      return null;
    }
    try {
      const { data } = await api.get("/me", {
        headers: { Authorization: `Bearer ${t}` },
        withCredentials: true,
      });
      setUser(data);
      return data;
    } catch {
      setUser(null);
      return null;
    }
  }, []);

	useEffect(() => {
		(async () => {
			try {
				const t = accessToken;
	
				if (t) {
					if (isExpired(t)) {
						sessionStorage.removeItem("access_token");
						setAccessToken("");
					} else {
						await fetchMe();
						return;
					}
				}
	
				if (suppressAutoRefresh) return;
				if (attemptedInitialRefresh) return;
	
				const everLoggedIn = localStorage.getItem("everLoggedIn") === "1";
				if (!everLoggedIn) {
					setUser(null);
					return;
				}
	
				setAttemptedInitialRefresh(true);
				try {
					const { data } = await api.post("/token/refresh", {}, { withCredentials: true });
					const token = data?.access_token || data?.accessToken || data?.token;
					if (token) {
						setAccessToken(token);
						await fetchMe();
					} else {
						sessionStorage.removeItem("access_token");
						localStorage.removeItem("everLoggedIn");
						setAccessToken("");
						setUser(null);
					}
				} catch (e) {
					sessionStorage.removeItem("access_token");
					localStorage.removeItem("everLoggedIn");
					setAccessToken("");
					setUser(null);
				}
			} finally {
				setLoading(false);
			}
		})();
	}, [accessToken, fetchMe, attemptedInitialRefresh, suppressAutoRefresh]);

  const login = useCallback(async (loginId, password) => {
    const { data } = await api.post("/login", { loginId, password }, { withCredentials: true });
    const token = data?.access_token || data?.accessToken || data?.token;
    if (!token) throw new Error("No access token returned");

    setAccessToken(token);
    localStorage.setItem("everLoggedIn", "1");

    const { data: me } = await api.get("/me", {
      headers: { Authorization: `Bearer ${token}` },
      withCredentials: true,
    });
    setUser(me);
    return me;
  }, []);

  const signup = useCallback(async (payload) => {
    await api.post("/signup", payload, { withCredentials: true });
    return true;
  }, []);

  const logout = useCallback(async (options = {}) => {
    try {
      await api.post("/logout", {}, { ...options, withCredentials: true, skipAuthRefresh: true });
    } catch {}
    setAccessToken("");
    setUser(null);
    localStorage.removeItem("everLoggedIn");
    setSuppressAutoRefresh(true);
    setAttemptedInitialRefresh(true);
    setTimeout(() => {
      setSuppressAutoRefresh(false);
      setAttemptedInitialRefresh(false);
    }, 1500);
  }, []);

	const isExpired = (token) => {
		try {
			const payload = JSON.parse(atob(token.split(".")[1] || ""));
			const nowSec = Math.floor(Date.now() / 1000);
			return payload.exp && payload.exp < nowSec;
		} catch {
			return true;
		}
	}
  const value = useMemo(
    () => ({ user, accessToken, login, signup, logout, loading, API_BASE }),
    [user, accessToken, login, signup, logout, loading]
  );

  return <AuthCtx.Provider value={value}>{children}</AuthCtx.Provider>;
}

export const useAuth = () => useContext(AuthCtx);