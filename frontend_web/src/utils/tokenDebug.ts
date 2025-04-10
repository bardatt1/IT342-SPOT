/**
 * Utility to diagnose token-related issues
 */
export const tokenDebugUtils = {
  /**
   * Test if the current token is valid by making a request to the server
   */
  testToken: async () => {
    const token = localStorage.getItem('token');
    if (!token) {
      console.error('No token found in localStorage');
      return {
        valid: false,
        error: 'No token found'
      };
    }

    try {
      // Skip validation endpoint since it's giving 403s
      // Instead, try to fetch a protected resource directly
      console.log('Testing protected endpoint access with token');
      const response = await fetch('http://localhost:8080/api/courses', {
        method: 'GET',
        headers: {
          'Authorization': `Bearer ${token.trim()}`,
          'Accept': 'application/json',
          'Content-Type': 'application/json'
        },
        credentials: 'include' // Include cookies if any
      });
      
      if (response.ok) {
        let data;
        try {
          data = await response.json();
        } catch (e) {
          data = { text: await response.text() };
        }
        
        return {
          valid: true,
          response: {
            status: response.status,
            statusText: response.statusText,
            data
          }
        };
      } else {
        return {
          valid: false,
          error: {
            status: response.status,
            statusText: response.statusText,
            message: 'Protected endpoint access failed'
          }
        };
      }
    } catch (error: any) {
      console.error('Token test failed:', error);
      return {
        valid: false,
        error: error.response || error.message,
        originalError: error
      };
    }
  },

  /**
   * Print detailed token information
   */
  inspectToken: () => {
    const token = localStorage.getItem('token');
    if (!token) {
      console.log('No token found in localStorage');
      return null;
    }
    
    console.log('Token found:', token.substring(0, 15) + '...');
    
    try {
      // Try to decode the token (assuming JWT format)
      const parts = token.split('.');
      if (parts.length === 3) {
        // Decode the middle part (payload)
        const payload = JSON.parse(atob(parts[1]));
        console.log('Token payload:', payload);
        
        // Check token expiration
        if (payload.exp) {
          const expDate = new Date(payload.exp * 1000);
          const now = new Date();
          console.log('Token expires:', expDate);
          console.log('Token expired:', expDate < now);
        }
        
        return payload;
      } else {
        console.log('Token is not in standard JWT format');
        return null;
      }
    } catch (e) {
      console.error('Error decoding token:', e);
      return null;
    }
  },
  
  /**
   * Reset the authentication state
   */
  resetAuth: () => {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    localStorage.removeItem('auth_timestamp');
    sessionStorage.clear();
    console.log('Auth state reset complete');
  }
};

export default tokenDebugUtils;
