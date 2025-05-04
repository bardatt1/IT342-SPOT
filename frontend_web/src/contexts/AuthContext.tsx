import { createContext, useState, useContext, useEffect, ReactNode } from 'react';
import axios from 'axios';
import { jwtDecode } from 'jwt-decode';
import axiosInstance from '../lib/api/axiosInstance';
import { setCookie, getCookie, removeCookie, clearAuthCookies } from '../lib/cookies';

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
    const storedToken = getCookie('auth_token');
    
    if (!storedToken || isTokenExpired(storedToken)) {
      console.error('Cannot refresh user data: No valid token');
      setIsAuthenticated(false);
      setUser(null);
      clearAuthCookies();
      return;
    }

    try {
      // Set auth header for the request
      axios.defaults.headers.common['Authorization'] = `Bearer ${storedToken}`;
      
      // For future implementation: Add an endpoint to fetch current user data
      // For now, we'll still use the stored user data since there's no endpoint
      // that just returns current user info without requiring a new login
      const storedUserData = getCookie('user');
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
      const token = getCookie('auth_token');
      
      if (token && !isTokenExpired(token)) {
        try {
          // Set auth header for all axios instances
          axios.defaults.headers.common['Authorization'] = `Bearer ${token}`;
          
          console.log('Valid token found, initializing authentication');
          
          // Fetch the user's current profile from the server to get accurate role information
          try {
            // Call API to get current user profile
            // This ensures the role comes from the server, not cookies
            const response = await axiosInstance.get('/auth/me');
            
            if (response && response.data && response.data.data) {
              console.log('User profile retrieved from server');
              const apiUserData = response.data.data;
              
              // Create user object with verified role from server
              const verifiedUser: User = {
                id: apiUserData.id,
                role: apiUserData.userType || 'GUEST', // Use role from server, default to GUEST instead of ADMIN
                email: apiUserData.email,
                name: `${apiUserData.firstName || ''} ${apiUserData.lastName || ''}`.trim(),
                googleLinked: apiUserData.googleLinked || false,
                hasTemporaryPassword: apiUserData.usesTemporaryPassword || false,
                studentPhysicalId: apiUserData.studentPhysicalId || ''
              };
              
              setUser(verifiedUser);
              setIsAuthenticated(true);
            } else {
              // If server doesn't return user data, extract info from token
              try {
                console.log('No API response, decoding from token');
                const decoded = jwtDecode<JwtPayload>(token);
                
                // Create minimal user object with safe defaults
                const safeUser: User = {
                  id: parseInt(getCookie('user_id') || '0'),
                  role: 'GUEST', // Always set a minimal role by default, not ADMIN
                  email: decoded.sub,
                  name: decoded.sub.split('@')[0] || 'User',
                  googleLinked: false,
                  hasTemporaryPassword: false
                };
                
                setUser(safeUser);
                setIsAuthenticated(true);
              } catch (tokenError) {
                console.error('Failed to decode token:', tokenError);
                logout();
              }
            }
          } catch (apiError) {
            console.error('Error fetching user profile:', apiError);
            logout();
          }
        } catch (error) {
          console.error('Auth initialization error:', error);
          logout();
        }
      } else if (token) {
        // Token is expired, clean up
        console.warn('Token expired, cleaning up');
        logout();
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
        setCookie('auth_token', responseData.accessToken);
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
        role: responseData.userType || 'GUEST', // Default to lowest privilege level instead of ADMIN
        email: responseData.email,
        name: `${responseData.firstName || ''} ${responseData.lastName || ''}`.trim(),
        googleLinked: responseData.googleLinked || false,
        hasTemporaryPassword: usesTemporaryPassword,
        studentPhysicalId: studentPhysicalId
      };
      
      // Store minimal user data in cookies, excluding security-critical fields
      setCookie('user', JSON.stringify({
        id: userData.id,
        email: userData.email,
        name: userData.name,
        googleLinked: userData.googleLinked,
        hasTemporaryPassword: userData.hasTemporaryPassword,
        studentPhysicalId: userData.studentPhysicalId
        // Note: role is intentionally NOT stored in cookies
      }));
      setCookie('user_id', userData.id.toString());
      
      setUser(userData);
      setIsAuthenticated(true);
      console.log('Login successful, user data set');
      
      return userData;
    } catch (error) {
      console.error('Login error:', error);
      throw error;
    }
  };

  const logout = () => {
    clearAuthCookies();
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
        setCookie('user', JSON.stringify(updatedUser));
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
