import axios from 'axios';

const api = axios.create({
    // 因为配置了 Vite 代理，这里直接写 '/api' 即可。
    baseURL: '/api',
    timeout: 60000, // 涉及到大模型，超时时间设长一点
});

export default api;