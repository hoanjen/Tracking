import axios from 'axios';

const API_BASE_URL = process.env.REACT_APP_API_URL || 'http://localhost:8080/api';

const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

api.interceptors.request.use((config) => {
  const token = localStorage.getItem('accessToken');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

export const authService = {
  login: (googleToken) => api.post('/auth/login', { googleToken }),
  getCurrentUser: () => api.get('/auth/me'),
};

export const tiktokService = {
  createAccount: (username) => api.post('/tiktok-accounts', { username }),
  getUserAccounts: () => api.get('/tiktok-accounts'),
  deleteAccount: (accountId) => api.delete(`/tiktok-accounts/${accountId}`),
};

export const trackingService = {
  // Không cần body — backend tự fetch followers từ TikTok API
  createTracking: (accountId) =>
    api.post(`/tracking/${accountId}/create-tracking`),
  getTrackingHistory: (accountId) => api.get(`/tracking/${accountId}/history`),
};

export const topUnfollowerService = {
  // Lấy tổng hợp toàn bộ user — không cần accountId
  getTopUnfollowers: () => api.get('/top-unfollowers'),
};

export default api;
