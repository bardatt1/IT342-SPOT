import axios from 'axios';

const API_URL = 'http://localhost:8080';

const api = axios.create({
  baseURL: API_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Add token to requests if it exists and not an auth endpoint
api.interceptors.request.use((config) => {
  const token = localStorage.getItem('token');
  const isAuthEndpoint = config.url?.includes('/auth/');
  
  if (token && !isAuthEndpoint) {
    config.headers.Authorization = `Bearer ${token}`;
  }

  // Debug log
  console.log('Request config:', {
    url: config.url,
    method: config.method,
    headers: config.headers,
    data: config.data
  });

  return config;
});

export interface LoginRequest {
  email: string;
  password: string;
}

export interface SignupRequest {
  email: string;
  password: string;
  firstName: string;
  lastName: string;
  role: string;
  platformType: string;
}

export interface AuthResponse {
  token: string;
  user: {
    id: number;
    email: string;
    firstName: string;
    lastName: string;
    role: string;
  };
}

export const authApi = {
  login: (data: LoginRequest) => 
    api.post<AuthResponse>('/api/auth/login', data),
  
  signup: async (data: SignupRequest) => {
    console.log('Sending signup request:', JSON.stringify(data, null, 2));
    try {
      console.log('Making request to:', `${API_URL}/api/auth/signup`);
      const response = await api.post<AuthResponse>('/api/auth/signup', data);
      console.log('Signup response:', response);
      return response;
    } catch (error: any) {
      console.error('Signup error:', {
        message: error.message,
        status: error.response?.status,
        data: error.response?.data,
        headers: error.response?.headers
      });
      throw error;
    }
  },
  
  googleLogin: (token: string, role: string = 'STUDENT') => 
    api.post<AuthResponse>('/api/auth/google', { token, role }),
  
  validateToken: (token: string) => 
    api.get<boolean>('/auth/validate', {
      headers: { Authorization: `Bearer ${token}` }
    }),
};

export default api;
