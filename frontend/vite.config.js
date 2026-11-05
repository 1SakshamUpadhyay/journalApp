import { defineConfig, loadEnv } from 'vite';
import react from '@vitejs/plugin-react';
// Backend has no CORS config, so dev traffic goes through this proxy: /api -> backend (incl. /journal context path)
export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd(), '');
  return {
    plugins: [react()],
    server: { proxy: { '/api': { target: env.VITE_PROXY_TARGET || 'http://localhost:8080/journal', changeOrigin: true, rewrite: p => p.replace(/^\/api/, '') } } }
  };
});
