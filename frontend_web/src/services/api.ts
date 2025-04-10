import axios, { AxiosRequestHeaders } from 'axios';

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
  // More specific check for auth endpoints to avoid unnecessary token inclusion
  const isAuthEndpoint = 
    config.url?.includes('/auth/login') || 
    config.url?.includes('/auth/signup') || 
    config.url?.includes('/auth/google');
  
  // Ensure we have properly initialized headers
  if (!config.headers) {
    config.headers = {} as AxiosRequestHeaders;
  }
  
  // Log full request for detailed debugging
  const logInfo = {
    method: config.method,
    baseURL: config.baseURL,
    fullUrl: `${API_URL}${config.url}`,
    isAuthEndpoint
  };
  console.log(`API Request to: ${config.url}`, logInfo);
  
  if (token && !isAuthEndpoint) {
    // Make sure token is properly formatted and not 'undefined'
    if (token !== 'undefined' && token !== 'null') {
      // Remove any existing token headers to prevent conflicts
      delete config.headers['Authorization'];
      
      // WORKAROUND: Server appears to be sensitive to token format
      // Format token exactly as expected by backend
      config.headers['Authorization'] = `Bearer ${token.trim()}`;
      
      // Set content type for all requests to ensure consistent behavior
      config.headers['Content-Type'] = 'application/json';
      
      // Set Accept header for all requests
      config.headers['Accept'] = 'application/json';
      
      // Enable CORS credentials
      config.withCredentials = true;
      
      // Debug the token being sent
      console.log('Using token for request:', token.substring(0, 15) + '...');
      console.log('Authorization header:', config.headers['Authorization']);
    } else {
      console.warn('Invalid token found in localStorage. Not adding to request.');
    }
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

// Handle response errors globally
api.interceptors.response.use(
  response => response,
  async (error) => {
    // Add retry attempt tracking to config to prevent infinite loops
    if (!error.config) {
      error.config = {};
    }
    error.config.__retryCount = error.config.__retryCount || 0;

    // Log detailed error info for debugging
    if (error.response) {
      console.error(`API Error ${error.response.status}:`, {
        url: error.config.url,
        method: error.config.method,
        status: error.response.status,
        data: error.response.data,
        retryCount: error.config.__retryCount
      });
      
      // Handle 401/403 errors (auth issues)
      if ((error.response.status === 401 || error.response.status === 403) && error.config.__retryCount < 1) {
        console.error('Authentication error:', error.response.data);
        
        // Log the token that was used
        const authHeader = error.config.headers?.Authorization;
        if (authHeader) {
          console.log('Token used:', authHeader.substring(0, 25) + '...');
        } else {
          console.log('No Authorization header was present in the request');
        }
        
        // Check if we've recently tried to refresh the token (within last 5 seconds)
        const lastRefreshTime = localStorage.getItem('last_token_refresh_time');
        const now = Date.now();
        const canRefresh = !lastRefreshTime || (now - parseInt(lastRefreshTime)) > 5000;
        
        if (canRefresh) {
          try {
            console.log('Attempting to refresh token after 403/401 error');
            
            // Get current token
            const currentToken = localStorage.getItem('token');
            if (currentToken) {
              // Mark that we're refreshing to prevent more refreshes
              localStorage.setItem('last_token_refresh_time', now.toString());
              
              // Force token 'refresh' by removing and re-adding it
              const tempToken = currentToken;
              localStorage.removeItem('token');
              await new Promise(resolve => setTimeout(resolve, 100));
              localStorage.setItem('token', tempToken);
              localStorage.setItem('auth_timestamp', now.toString());
              
              // Increment retry count to prevent infinite loops
              error.config.__retryCount += 1;
              
              // Retry the original request
              console.log('Token refreshed, retrying request');
              error.config.headers['Authorization'] = `Bearer ${tempToken}`;
              return api(error.config);
            }
          } catch (refreshError) {
            console.error('Error during token refresh:', refreshError);
          }
        } else {
          console.log(`Skipping token refresh - last refresh was too recent (${(now - parseInt(lastRefreshTime))/1000}s ago)`);
        }
      }
    } else if (error.request) {
      // The request was made but no response was received
      console.error('No response received:', error.request);
    } else {
      // Something happened in setting up the request
      console.error('Request setup error:', error.message);
    }
    return Promise.reject(error);
  }
);

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
  
  validateToken: (token: string) => {
    console.log('Validating token:', token.substring(0, 15) + '...');
    // Use default Axios instance to prevent interceptor conflicts
    return api.get<boolean>('/api/auth/validate', {
      headers: { 
        'Authorization': `Bearer ${token.trim()}`,
        'Accept': 'application/json'
      }
    });
  },
  
  // Debug method to check token on server side
  debugToken: async () => {
    const token = localStorage.getItem('token');
    if (!token) {
      console.error('No token to debug');
      return null;
    }
    
    try {
      // Make a request to a protected endpoint to test token
      const response = await api.get('/api/courses', {
        headers: {
          'Authorization': `Bearer ${token.trim()}`,
          'Accept': 'application/json'
        }
      });
      console.log('Token debug success:', response);
      return response;
    } catch (error: any) {
      console.error('Token debug failed:', {
        message: error.message,
        status: error.response?.status,
        data: error.response?.data
      });
      return error.response;
    }
  },
};

export default api;
