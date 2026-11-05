
import axios from 'axios';

const api = axios.create({
  baseURL: import.meta.env.VITE_API_URL || '/api'
});

const raw = {
  transformResponse: (r) => r
};

api.interceptors.request.use((config) => {
  const token = localStorage.getItem('token');

  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }

  return config;
});

api.interceptors.response.use(
  (response) => response,
  (error) => {
    const status = error.response?.status;
    const url = error.config?.url || '';

    if (
      localStorage.getItem('token') &&
      (status === 401 ||
        (status === 403 && !url.startsWith('/admin')))
    ) {
      window.dispatchEvent(new Event('auth:logout'));
    }

    return Promise.reject(error);
  }
);

const list = (path) =>
  api.get(path)
    .then((response) => response.data)
    .catch((error) => {
      if (error.response?.status === 404) return [];
      throw error;
    });

const id = encodeURIComponent;

export const authApi = {
  login: (username, password) =>
    api.post('/public/login', { username, password }, raw)
      .then((response) => response.data),

  signup: (payload) => api.post('/public/signup', payload)
};

export const journalApi = {
  list: () => list('/journal'),

  get: (entryId) =>
    api.get(`/journal/id/${id(entryId)}`).then((r) => r.data),

  create: (data) =>
    api.post('/journal', data).then((r) => r.data),

  update: (entryId, data) =>
    api.put(`/journal/id/${id(entryId)}`, data).then((r) => r.data),

  remove: (entryId) =>
    api.delete(`/journal/id/${id(entryId)}`)
};

export const userApi = {
  greeting: () => api.get('/user', raw).then((r) => r.data),

  update: (data) => api.put('/user', data),

  remove: () => api.delete('/user')
};

export const adminApi = {
  users: () => list('/admin/all-users'),

  createAdmin: (data) =>
    api.post('/admin/create-admin-users', data),

  clearCache: () => api.get('/admin/clear-app-cache')
};

export default api;