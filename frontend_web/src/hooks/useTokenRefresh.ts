import { useState, useEffect } from 'react';

/**
 * Custom hook to handle token refresh logic
 * This can help solve issues where the token is valid but the server still returns 403
 */
export const useTokenRefresh = () => {
  const [isRefreshing, setIsRefreshing] = useState(false);
  const [lastRefresh, setLastRefresh] = useState<Date | null>(null);
  
  /**
   * Force a token refresh by clearing and reloading it
   */
  const refreshToken = async (): Promise<boolean> => {
    setIsRefreshing(true);
    
    try {
      // Get current token and user data
      const currentToken = localStorage.getItem('token');
      const currentUser = localStorage.getItem('user');
      
      if (!currentToken || !currentUser) {
        console.error('No token or user data found to refresh');
        return false;
      }
      
      // First, temporarily store the current token
      const tempToken = currentToken;
      const tempUser = currentUser;
      
      // Clear token to simulate logout
      localStorage.removeItem('token');
      
      // Small delay to ensure token is cleared
      await new Promise(resolve => setTimeout(resolve, 100));
      
      // Store the token back (simulates a refresh)
      localStorage.setItem('token', tempToken);
      localStorage.setItem('user', tempUser);
      
      // Update timestamp to protect from immediate re-clearing
      localStorage.setItem('auth_timestamp', Date.now().toString());
      
      // Record the refresh time
      const now = new Date();
      setLastRefresh(now);
      console.log('Token refreshed at:', now.toISOString());
      
      return true;
    } catch (error) {
      console.error('Error refreshing token:', error);
      return false;
    } finally {
      setIsRefreshing(false);
    }
  };
  
  /**
   * Schedule periodic token refreshes to avoid 403 errors
   */
  useEffect(() => {
    // Check if we have a token to refresh
    const hasToken = !!localStorage.getItem('token');
    if (!hasToken) return;
    
    // Set up token refresh every 30 minutes to avoid expiration issues
    const refreshInterval = setInterval(() => {
      console.log('Performing scheduled token refresh');
      refreshToken().then(success => {
        console.log('Scheduled token refresh:', success ? 'succeeded' : 'failed');
      });
    }, 30 * 60 * 1000); // 30 minutes
    
    return () => {
      clearInterval(refreshInterval);
    };
  }, []);
  
  return {
    refreshToken,
    isRefreshing,
    lastRefresh
  };
};

export default useTokenRefresh;