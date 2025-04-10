/**
 * Auth Debug Utilities
 * Provides functions to help debug authentication issues
 */

export const logAuthState = () => {
  try {
    const token = localStorage.getItem('token');
    const userStr = localStorage.getItem('user');
    let user = null;
    
    console.group('Auth State Debug');
    console.log('Token exists:', !!token);
    if (token) {
      console.log('Token (first 15 chars):', token.substring(0, 15) + '...');
    }
    
    console.log('User data exists:', !!userStr);
    if (userStr) {
      try {
        user = JSON.parse(userStr);
        console.log('User data:', user);
      } catch (e) {
        console.error('Failed to parse user data:', e);
      }
    }
    console.groupEnd();
    
    return { token, user };
  } catch (err) {
    console.error('Error in auth debug:', err);
    return { token: null, user: null };
  }
};

/**
 * Test if the current token is valid by making a request to the server
 */
export const testToken = async () => {
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
    try {
      // Use a fetch API directly to avoid interceptors
      const response = await fetch(`http://localhost:8080/api/courses`, {
        method: 'GET',
        headers: {
          'Authorization': `Bearer ${token.trim()}`,
          'Accept': 'application/json',
          'Content-Type': 'application/json'
        },
        credentials: 'include' // Include cookies if any
      });
      
      if (response.ok) {
        const data = await response.json();
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
      console.error('Fetch API request failed:', error);
      return {
        valid: false,
        error: {
          message: error.message,
          type: 'FetchError'
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
};

export const clearAndLogAuth = () => {
  console.group('Auth Cleanup');
  console.log('Before cleanup:');
  logAuthState();
  
  // Clear auth data
  localStorage.removeItem('token');
  localStorage.removeItem('user');
  localStorage.removeItem('userRole'); // Also clear the separately stored userRole
  
  console.log('After cleanup:');
  logAuthState();
  console.groupEnd();
};
