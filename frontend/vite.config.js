import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173, // 或者你想固定的任何端口
    proxy: {
      // 拦截所有以 /api 开头的请求
      '/api': {
        target: 'http://localhost:8080', // 你的 Spring Boot 后端地址
        changeOrigin: true, // 允许跨域
        // 【关键一步】：把前端请求里的 '/api' 抹掉。
        // 因为前端发的是 /api/xiaozhi/chat，而你后端 Controller 是 @RequestMapping("/xiaozhi")
        // 重写后，到达后端的真实路径就是 http://localhost:8080/xiaozhi/chat，完美对齐！
        rewrite: (path) => path.replace(/^\/api/, '')
      }
    }
  }
})