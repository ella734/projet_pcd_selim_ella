import axios from 'axios';

const api = axios.create({
  baseURL: import.meta.env.VITE_API_URL || '/api',
  withCredentials: true,
});

api.interceptors.response.use(
  (response) => response,
  (error) => {
    // Only redirect on 401 if we're not already on the login page
    if (error.response?.status === 401) {
      const currentPath = window.location.pathname;
      if (currentPath !== '/login') {
        localStorage.removeItem('user');
        window.location.href = '/login?reason=expired';
      }
    }
    return Promise.reject(error);
  }
);

export default api;
