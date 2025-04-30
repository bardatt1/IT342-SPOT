import { createContext, useState, useContext, useEffect, ReactNode } from 'react';
import axios from 'axios';
import { jwtDecode } from 'jwt-decode';
import axiosInstance from '../lib/api/axiosInstance';

interface User {
  id: number;
  role: string;
  email: string;
  name: string;
  googleLinked: boolean;
  hasTemporaryPassword?: boolean;
  studentPhysicalId?: string;
}

interface AuthContextType {
  user: User | null;
  isAuthenticated: boolean;
  isLoading: boolean;
  login: (email: string, password: string) => Promise<User>;
  logout: () => void;
  bindGoogleAccount: (email: string, googleToken: string) => Promise<boolean>;
  refreshUserData: () => Promise<void>;
}

interface AuthProviderProps {
  children: ReactNode;
}

interface JwtPayload {
  sub: string;
  exp: number;
}

// API response structure (for documentation purposes only)
// This helps document the backend API response format but isn't directly used
// @ts-ignore - This interface is for documentation only
interface ApiResponse {
  result: string;
  message: string;
  data: {
    id: number;
    email: string;
    firstName: string;
    lastName: string;
    userType: string;
    accessToken: string;
    googleLinked?: boolean;
  };
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

const isTokenExpired = (token: string): boolean => {
  try {
    const decoded = jwtDecode<JwtPayload>(token);
    // Add a buffer of 60 seconds to avoid edge cases
    return decoded.exp < (Date.now() / 1000) - 60;
  } catch (error) {
    console.error('Error decoding token:', error);
    return true;
  }
};

export const AuthProvider: React.FC<AuthProviderProps> = ({ children }) => {
  const [user, setUser] = useState<User | null>(null);
  const [isAuthenticated, setIsAuthenticated] = useState<boolean>(false);
  const [isLoading, setIsLoading] = useState<boolean>(true);

  // Function to refresh user data with a direct API call
  const refreshUserData = async (): Promise<void> => {
    const storedToken = localStorage.getItem('token');
    
    if (!storedToken || isTokenExpired(storedToken)) {
      console.error('Cannot refresh user data: No valid token');
      setIsAuthenticated(false);
      setUser(null);
      localStorage.removeItem('token');
      localStorage.removeItem('user');
      return;
    }

    try {
      // Set auth header for the request
      axios.defaults.headers.common['Authorization'] = `Bearer ${storedToken}`;
      
      // For future implementation: Add an endpoint to fetch current user data
      // For now, we'll still use the stored user data since there's no endpoint
      // that just returns current user info without requiring a new login
      const storedUserData = localStorage.getItem('user');
      if (storedUserData) {
        console.log('Using stored user data for refresh');
        const userData = JSON.parse(storedUserData);
        setUser(userData);
        setIsAuthenticated(true);
      } else {
        console.warn('No stored user data to refresh from');
        setIsAuthenticated(false);
        setUser(null);
      }
    } catch (error) {
      console.error('Error refreshing user data:', error);
      // If refresh fails, logout the user
      logout();
    }
  };

  useEffect(() => {
    const initAuth = async () => {
      const storedToken = localStorage.getItem('token');
      const storedUserData = localStorage.getItem('user');
      
      if (storedToken && !isTokenExpired(storedToken)) {
        try {
          // Set auth header for all axios instances
          axios.defaults.headers.common['Authorization'] = `Bearer ${storedToken}`;
          
          console.log('Valid token found, initializing authentication');
          
          if (storedUserData) {
            console.log('Using stored user data for initialization');
            const userData = JSON.parse(storedUserData);
            setUser(userData);
            setIsAuthenticated(true);
          } else {
            // If we have a token but no user data, try to get user info from token
            try {
              console.log('No stored user data found, extracting from token');
              const decoded = jwtDecode<JwtPayload>(storedToken);
              
              // Try to fetch user data from API based on token's subject (email)
              console.log('Attempting to fetch user data for:', decoded.sub);
              
              // For future implementation: Add an endpoint to fetch user data by email
              // For now, we'll use the token data as a minimal solution
              const partialUser = {
                id: 0, 
                role: 'GUEST', // Set a safe minimal role until we know better
                email: decoded.sub,
                name: decoded.sub.split('@')[0] || 'User',
                googleLinked: false
              };
              
              setUser(partialUser);
              setIsAuthenticated(true);
              localStorage.setItem('user', JSON.stringify(partialUser));
              console.log('Created partial user from token');
            } catch (tokenError) {
              console.error('Failed to extract user info from token:', tokenError);
              // Token is invalid or malformed
              localStorage.removeItem('token');
              setIsAuthenticated(false);
              setUser(null);
            }
          }
        } catch (error) {
          console.error('Error during auth initialization:', error);
          // Clear credentials on initialization error
          localStorage.removeItem('token');
          localStorage.removeItem('user');
          setIsAuthenticated(false);
          setUser(null);
        }
      } else if (storedToken) {
        // Token is expired, clean up
        console.warn('Token expired, cleaning up');
        localStorage.removeItem('token');
        localStorage.removeItem('user');
        setIsAuthenticated(false);
        setUser(null);
      } else {
        console.log('No authentication token found');
        setIsAuthenticated(false);
        setUser(null);
      }
      
      setIsLoading(false);
    };
    
    initAuth();
  }, []);

  const login = async (email: string, password: string): Promise<User> => {
    try {
      // No more token bypass - we'll always use direct database authentication
      
      console.log('Authenticating user:', email);
      const response = await axiosInstance.post('/auth/login', { email, password });
      
      console.log('Response structure:', response.data);
      
      // Extract the response data from the nested structure
      // The API returns {result, message, data} where data contains the actual JWT response
      const responseData = response.data.data || response.data;
      console.log('Extracted response data:', responseData);
      
      // Check if the response contains usesTemporaryPassword flag
      const usesTemporaryPassword = responseData.usesTemporaryPassword || false;
      const studentPhysicalId = responseData.studentPhysicalId || '';
      
      // Always save the token for authenticated sessions
      if (responseData.accessToken) {
        localStorage.setItem('token', responseData.accessToken);
        // Set auth header
        axios.defaults.headers.common['Authorization'] = `Bearer ${responseData.accessToken}`;
        console.log('Token saved and header set');
      } else {
        console.error('No access token received in login response');
        throw new Error('Authentication failed: No token received');
      }
      
      // Map the API response fields to our User interface
      const userData: User = {
        id: responseData.id,
        role: responseData.userType || 'ADMIN', // API uses userType for role
        email: responseData.email,
        name: `${responseData.firstName || ''} ${responseData.lastName || ''}`.trim(),
        googleLinked: responseData.googleLinked || false,
        hasTemporaryPassword: usesTemporaryPassword,
        studentPhysicalId: studentPhysicalId
      };
      
      setUser(userData);
      setIsAuthenticated(true);
      localStorage.setItem('user', JSON.stringify(userData));
      console.log('Login successful, user data:', userData);
      
      return userData;
    } catch (error) {
      console.error('Login error:', error);
      throw error;
    }
  };

  const logout = () => {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    localStorage.removeItem('teacherName'); // Also clear cached teacher name
    setUser(null);
    setIsAuthenticated(false);
    // Clear auth header
    delete axios.defaults.headers.common['Authorization'];
    console.log('User logged out, all session data cleared');
  };

  const bindGoogleAccount = async (email: string, googleToken: string): Promise<boolean> => {
    try {
      // Call API to bind Google account
      await axiosInstance.post('/auth/bind-oauth', { email, googleToken });
      
      // Update user state
      if (user) {
        const updatedUser = { ...user, googleLinked: true };
        setUser(updatedUser);
        localStorage.setItem('user', JSON.stringify(updatedUser));
      }
      
      return true;
    } catch (error) {
      console.error('Error binding Google account:', error);
      return false;
    }
  };

  return (
    <AuthContext.Provider
      value={{
        user,
        isAuthenticated,
        isLoading,
        login,
        logout,
        bindGoogleAccount,
        refreshUserData
      }}
    >
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = (): AuthContextType => {
  const context = useContext(AuthContext);
  if (context === undefined) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};
