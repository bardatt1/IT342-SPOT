import { useEffect, useRef } from 'react';

/**
 * Custom hook to monitor authentication storage and detect when items are removed
 */
export function useAuthMonitor() {
  const tokenRef = useRef<string | null>(null);
  const userRef = useRef<string | null>(null);
  
  // Set initial values
  useEffect(() => {
    tokenRef.current = localStorage.getItem('token');
    userRef.current = localStorage.getItem('user');
    
    console.log('Auth Monitor: Initial state captured', {
      hasToken: !!tokenRef.current,
      hasUser: !!userRef.current
    });
    
    // Override localStorage methods to track auth data changes
    const originalSetItem = localStorage.setItem;
    const originalRemoveItem = localStorage.removeItem;
    
    // Override setItem to log auth-related changes
    localStorage.setItem = function(key: string, value: string) {
      console.log(`Auth Monitor: localStorage.setItem('${key}', ${key === 'token' ? 'TOKEN_VALUE' : value.substring(0, 20) + '...'}) called`);
      console.trace('Trace for localStorage.setItem:');
      
      if (key === 'token') {
        tokenRef.current = value;
      } else if (key === 'user') {
        userRef.current = value;
      }
      
      return originalSetItem.call(localStorage, key, value);
    };
    
    // Override removeItem to log auth-related changes
    localStorage.removeItem = function(key: string) {
      console.log(`Auth Monitor: localStorage.removeItem('${key}') called`);
      console.trace('Trace for localStorage.removeItem:');
      
      if (key === 'token') {
        tokenRef.current = null;
      } else if (key === 'user') {
        userRef.current = null;
      }
      
      return originalRemoveItem.call(localStorage, key);
    };
    
    // Polling check to detect external changes to localStorage
    const intervalId = setInterval(() => {
      const currentToken = localStorage.getItem('token');
      const currentUser = localStorage.getItem('user');
      
      // Check if token was removed
      if (tokenRef.current && !currentToken) {
        console.log('Auth Monitor: Token was removed outside of our tracking!');
        tokenRef.current = null;
      }
      
      // Check if user was removed
      if (userRef.current && !currentUser) {
        console.log('Auth Monitor: User was removed outside of our tracking!');
        userRef.current = null;
      }
    }, 500);
    
    // Clean up
    return () => {
      clearInterval(intervalId);
      localStorage.setItem = originalSetItem;
      localStorage.removeItem = originalRemoveItem;
    };
  }, []);
}
