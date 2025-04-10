import { useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useToast } from '@/components/ui/use-toast';
import { clearAndLogAuth } from '@/utils/authDebug';

type LogoutPageProps = {
  setIsAuthenticated?: React.Dispatch<React.SetStateAction<boolean | null>>;
};

export default function LogoutPage({ setIsAuthenticated }: LogoutPageProps) {
  const navigate = useNavigate();
  const { toast } = useToast();

  useEffect(() => {
    // Use the debug utility to clear and log auth state
    clearAndLogAuth();
    
    // Make sure sessionStorage is also cleared (in case any auth data is stored there)
    sessionStorage.clear();
    
    // Update authentication state
    if (setIsAuthenticated) {
      setIsAuthenticated(false);
    }
    
    // Show success message
    toast({
      title: 'Logged out',
      description: 'You have been successfully logged out',
    });
    
    // Add a small delay to ensure everything is cleaned up before redirecting
    setTimeout(() => {
      // Redirect to login page instead of home to prevent white screen
      navigate('/login');
    }, 100);
  }, [navigate, toast, setIsAuthenticated]);

  return (
    <div className="min-h-screen flex items-center justify-center">
      <div className="animate-spin rounded-full h-12 w-12 border-t-2 border-b-2 border-primary"></div>
    </div>
  );
}
