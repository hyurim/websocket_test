import axios from "axios";

const RAW_BASE = import.meta.env.DEV
  ? "/api"
  : import.meta.env.VITE_API_BASE ||
    "https://kanji-proxy.yimjh2309.workers.dev";

const STRIPPED = RAW_BASE.replace(/\/+$/, "");
export const API_BASE = STRIPPED.endsWith("/api")
  ? STRIPPED
  : `${STRIPPED}/api`;

const api = axios.create({
  baseURL: API_BASE,
  withCredentials: true,
});

let getAccessToken = () => null;
export const setAccessTokenGetter = (fn) => {
  getAccessToken = fn;
};

let onTokenRefreshed = () => {};
export const setOnTokenRefreshed = (fn) => {
  onTokenRefreshed = fn;
};

api.interceptors.request.use((config) => {
  const token = getAccessToken?.();
  if (token) {
    config.headers = config.headers || {};
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

let isRefreshing = false;
let queue = [];

const flushQueue = (error, token = null) => {
  queue.forEach(({ resolve, reject }) =>
    error ? reject(error) : resolve(token)
  );
  queue = [];
};

api.interceptors.response.use(
  (res) => res,
  async (error) => {
    const original = error.config;
    const status = error?.response?.status;
    const is401 = status === 401;
    const isRefreshCall = original?.url?.includes("/token/refresh");

    if (original?.skipAuthRefresh) {
      return Promise.reject(error);
    }
    if (!is401) return Promise.reject(error);

    if (isRefreshCall) return Promise.reject(error);

    if (original?._retry) return Promise.reject(error);
    original._retry = true;

    if (isRefreshing) {
      return new Promise((resolve, reject) => {
        queue.push({ resolve, reject });
      }).then((token) => {
        original.headers = original.headers || {};
        original.headers.Authorization = `Bearer ${token}`;
        return api(original);
      });
    }

    isRefreshing = true;
    try {
      const { data } = await api.post("/token/refresh", {});
      const newToken = data?.access_token || data?.accessToken || data?.token;
      if (!newToken) throw new Error("No access token in refresh response");

      onTokenRefreshed?.(newToken);

      flushQueue(null, newToken);
      isRefreshing = false;

      original.headers = original.headers || {};
      original.headers.Authorization = `Bearer ${newToken}`;
      return api(original);
    } catch (e) {
      flushQueue(e, null);
      isRefreshing = false;
      return Promise.reject(e);
    }
  }
);

export default api;
