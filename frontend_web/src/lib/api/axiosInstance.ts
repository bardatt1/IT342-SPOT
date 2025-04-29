import axios from 'axios';

// Create axios instance with default config
const apiUrl = import.meta.env.VITE_API_URL || 'http://localhost:8080/api';
console.log('API URL:', apiUrl); // Debug API URL

const axiosInstance = axios.create({
  baseURL: apiUrl,
  headers: {
    'Content-Type': 'application/json',
  },
  // Add timeout to prevent hanging requests
  timeout: 15000,
});

// Request interceptor
axiosInstance.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
      console.log('Request with token to:', config.url);
    } else {
      console.log('Request without token to:', config.url);
    }
    return config;
  },
  (error) => {
    console.error('Request interceptor error:', error);
    return Promise.reject(error);
  }
);

// Response interceptor
axiosInstance.interceptors.response.use(
  (response) => {
    console.log('Response from:', response.config.url, 'Status:', response.status);
    return response;
  },
  (error) => {
    if (error.response) {
      // The request was made and the server responded with a status code
      // that falls out of the range of 2xx
      console.error(
        'API Error:',
        error.response.config.url,
        'Status:', error.response.status,
        'Data:', error.response.data
      );
      
      if (error.response.status === 401) {
        // Only handle 401 errors from certain endpoints
        const url = error.response.config.url;
        
        // Don't immediately logout for all 401 errors
        // We can be more selective about which 401s should force logout
        console.log('Unauthorized access on:', url);
        
        // Check if this is a critical endpoint that requires session validity
        const isCriticalEndpoint = 
          url?.includes('/auth/') || // Auth endpoints
          url === '/user/me' ||     // User profile endpoint
          url === '/user/profile';  // Another user endpoint
        
        if (isCriticalEndpoint) {
          console.log('Critical endpoint unauthorized, logging out');
          localStorage.removeItem('token');
          localStorage.removeItem('user');
          
          // Use a more user-friendly approach - set flag and let UI handle redirect
          // This prevents abrupt redirects in the middle of user actions
          if (!window.location.pathname.includes('/login')) {
            console.log('Redirecting to login page');
            // Add a small delay to allow current code to complete
            setTimeout(() => {
              window.location.href = '/login';
            }, 100);
          }
        } else {
          console.log('Non-critical endpoint unauthorized, continuing session');
          // For non-critical endpoints, we can just let the error propagate
          // The individual components can handle these errors appropriately
        }
      }
    } else if (error.request) {
      // The request was made but no response was received
      console.error('No response received:', error.request);
    } else {
      // Something happened in setting up the request that triggered an Error
      console.error('Error setting up request:', error.message);
    }
    return Promise.reject(error);
  }
);

export default axiosInstance;
