import { defineConfig } from "vite";
import react from "@vitejs/plugin-react";

// The API only accepts http://localhost:5173 (FRONTEND_ORIGIN), so the port is fixed.
const server = { host: "localhost", port: 5173, strictPort: true };

export default defineConfig({
  plugins: [react()],
  server,
  preview: server
});
