import { ReactNode, useState, useEffect } from 'react';
import { Navigate } from 'react-router-dom';
import { useAuth } from '../../contexts/AuthContext';
import { jwtDecode } from 'jwt-decode';

interface AdminRouteProps {
  children: ReactNode;
}

// Define a custom JWT payload type to include our specific fields
interface SpotJwtPayload {
  sub: string;
  exp: number;
  // Custom fields that might be in our JWT
  userType?: string;
  role?: string;
  authorities?: string[];
  // Any other possible fields
  [key: string]: any;
}

/**
 * AdminRoute - A special route component that protects admin routes
 * 
 * Instead of relying on localStorage for role information, this component
 * verifies admin status from the JWT token itself, which is more secure
 * than checking client-side state that can be manipulated.
 */
const AdminRoute = ({ children }: AdminRouteProps) => {
  const { isAuthenticated, isLoading } = useAuth(); // Remove unused 'user' variable
  const [isVerifying, setIsVerifying] = useState(true);
  const [isVerifiedAdmin, setIsVerifiedAdmin] = useState(false);

  useEffect(() => {
    const verifyAdminFromToken = () => {
      try {
        if (!isAuthenticated) {
          setIsVerifiedAdmin(false);
          setIsVerifying(false);
          return;
        }

        // Get token from localStorage
        const token = localStorage.getItem('token');
        if (!token) {
          setIsVerifiedAdmin(false);
          setIsVerifying(false);
          return;
        }

        // Decode the JWT token with our custom payload type
        const decodedToken = jwtDecode<SpotJwtPayload>(token);
        
        // Check if the token contains role/user type information
        // Since we can't change the backend, we need to use whatever fields exist in the token
        const isAdmin = Boolean(
          (decodedToken.userType === 'ADMIN') || 
          (decodedToken.role === 'ADMIN') || 
          (decodedToken.authorities && 
           Array.isArray(decodedToken.authorities) && 
           decodedToken.authorities.includes('ADMIN'))
        );
        
        if (isAdmin) {
          setIsVerifiedAdmin(true);
        } else {
          // Token doesn't contain admin role
          console.warn('User attempted to access admin route without proper JWT permissions');
          setIsVerifiedAdmin(false);
        }
      } catch (error) {
        console.error('Error verifying admin status from token:', error);
        setIsVerifiedAdmin(false);
      } finally {
        setIsVerifying(false);
      }
    };

    verifyAdminFromToken();
  }, [isAuthenticated]);

  // Show loading while verifying admin status
  if (isLoading || isVerifying) {
    return (
      <div className="flex h-screen items-center justify-center">
        <div className="text-center">
          <div className="inline-block h-8 w-8 animate-spin rounded-full border-4 border-solid border-current border-r-transparent align-[-0.125em] motion-reduce:animate-[spin_1.5s_linear_infinite]" role="status">
            <span className="!absolute !-m-px !h-px !w-px !overflow-hidden !whitespace-nowrap !border-0 !p-0 ![clip:rect(0,0,0,0)]">
              Loading...
            </span>
          </div>
          <p className="mt-2 text-gray-700">Verifying admin access...</p>
        </div>
      </div>
    );
  }

  // Redirect to login if not authenticated
  if (!isAuthenticated) {
    return <Navigate to="/login" replace />;
  }

  // If admin status is not verified, redirect to unauthorized page
  if (!isVerifiedAdmin) {
    return <Navigate to="/unauthorized" replace />;
  }

  // Render children only if admin status is verified from token
  return <>{children}</>;
};

export default AdminRoute;
