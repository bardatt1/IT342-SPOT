import { ReactNode } from 'react';
import { Navigate } from 'react-router-dom';
import { useAuth } from '../../contexts/AuthContext';

type ChildrenType = ReactNode | ((props: { user: any }) => ReactNode);

interface ProtectedRouteProps {
  children: ChildrenType;
  allowedRoles?: string[];
}

const ProtectedRoute = ({ children, allowedRoles }: ProtectedRouteProps) => {
  const { isAuthenticated, isLoading, user } = useAuth();

  // Show loading state if still checking authentication
  if (isLoading) {
    return <div className="flex h-screen items-center justify-center">Loading...</div>;
  }

  // Redirect to login if not authenticated
  if (!isAuthenticated) {
    return <Navigate to="/login" replace />;
  }

  // Check role permissions if roles are specified
  if (allowedRoles && user && !allowedRoles.includes(user.role)) {
    // Redirect based on role
    if (user.role === 'ADMIN') {
      return <Navigate to="/admin/dashboard" replace />;
    } else if (user.role === 'TEACHER') {
      return <Navigate to="/teacher/dashboard" replace />;
    } else if (user.role === 'STUDENT') {
      // Students might use mobile but handle it anyway
      return <Navigate to="/student/dashboard" replace />;
    } else {
      // Fallback for any other role
      return <Navigate to="/unauthorized" replace />;
    }
  }

  // Render children as a function if it is one
  if (typeof children === 'function') {
    return <>{children({ user })}</>;
  }

  // Regular ReactNode children
  return <>{children}</>;
};

export default ProtectedRoute;
