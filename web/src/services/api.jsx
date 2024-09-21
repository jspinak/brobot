import axios from 'axios';

const api = axios.create({
  baseURL: process.env.REACT_APP_BROBOT_API_URL,
});

// You can add interceptors or other configurations here if needed
api.interceptors.request.use((config) => {
  // For example, you could add an auth token to all requests
  // config.headers.Authorization = `Bearer ${localStorage.getItem('token')}`;
  return config;
});

export default api;