import { defineConfig } from "vite";
import react from "@vitejs/plugin-react-swc";

export default defineConfig({
  plugins: [react()],
  define: {
    global: "window",
    "process.env": {},
  },
  resolve: {
    dedupe: ["react", "react-dom"],
  },
  base: "/kanji-study/",
  server: {
    proxy: {
      "/api": {
        target: "http://localhost:8081",
        changeOrigin: true,
      },
    },
  },
});
