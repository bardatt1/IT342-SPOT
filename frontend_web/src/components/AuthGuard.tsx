import { ReactNode, useEffect, useState } from 'react';
import { Navigate, useLocation } from 'react-router-dom';
import { logAuthState } from '@/utils/authDebug';

interface AuthGuardProps {
  children: ReactNode;
}

export const AuthGuard = ({ children }: AuthGuardProps) => {
  const location = useLocation();
  const [isAuthenticated, setIsAuthenticated] = useState<boolean | null>(null);

  useEffect(() => {
    console.log('AuthGuard: Checking authentication status');
    // Log current auth state for debugging
    const { token } = logAuthState();
    
    // Simply check if token exists
    if (!token) {
      console.log('AuthGuard: No token found');
      setIsAuthenticated(false);
      return;
    }
    
    // If token exists, consider the user authenticated
    // We'll let App.tsx handle the validation details
    console.log('AuthGuard: Token exists, considering user authenticated');
    setIsAuthenticated(true);
  }, []);

  // Show loading state while checking authentication
  if (isAuthenticated === null) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <div className="animate-spin rounded-full h-12 w-12 border-t-2 border-b-2 border-primary"></div>
      </div>
    );
  }

  // Redirect to login if not authenticated
  if (!isAuthenticated) {
    return <Navigate to="/login" state={{ from: location }} replace />;
  }

  // Render children if authenticated
  return <>{children}</>;
};
