import axios from "axios";

const api = axios.create({ baseURL: (import.meta.env.VITE_API_URL || "http://localhost:8080") + "/api" });

api.interceptors.request.use((c) => {
  const t = localStorage.getItem("token");
  if (t) c.headers.Authorization = `Bearer ${t}`;
  return c;
});
api.interceptors.response.use((r) => r, (e) => {
  if (e.response?.status === 401 && !location.pathname.startsWith("/login")) {
    localStorage.clear();
    location.href = "/login";
  }
  return Promise.reject(e);
});

export const errMsg = (e) => e.response?.data?.message || "Something went wrong. Please try again.";
export default api;
