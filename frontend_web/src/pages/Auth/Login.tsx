import { Link, useNavigate } from 'react-router-dom'
import { Button } from '@/components/ui/button'
import { useState, Dispatch, SetStateAction, useEffect } from 'react'
import { authApi } from '@/services/api'
import { useToast } from '@/components/ui/use-toast'
import { logAuthState, clearAndLogAuth } from '@/utils/authDebug'

type LoginPageProps = {
  setIsAuthenticated?: Dispatch<SetStateAction<boolean | null>>
}

export default function LoginPage({ setIsAuthenticated }: LoginPageProps) {
  const navigate = useNavigate();
  const { toast } = useToast();
  const [loading, setLoading] = useState(false);
  const [formData, setFormData] = useState({
    email: '',
    password: '',
  });

  // Check if we already have valid auth data on login page load
  useEffect(() => {
    // Check if there's existing auth data
    const token = localStorage.getItem('token');
    const userStr = localStorage.getItem('user');
    
    // Only clear existing data if it's corrupted (not if it's valid)
    if ((token === 'undefined' || token === 'null') || 
        (userStr === 'undefined' || userStr === 'null')) {
      console.log('Found corrupted auth data on login page, clearing it');
      clearAndLogAuth();
    } else if (token && userStr) {
      // If we already have valid auth data, redirect to home
      console.log('Found valid auth data on login page, redirecting to home');
      navigate('/home');
    }
  }, [navigate]);

  const handleInputChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target;
    setFormData(prev => ({ ...prev, [name]: value }));
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    try {
      // Simplify the login process - don't clear anything first
      console.log('Attempting login with credentials:', { email: formData.email });
      
      const response = await authApi.login(formData);
      console.log('Login response received:', {
        success: true,
        tokenReceived: !!response.data.token,
        userDataReceived: !!response.data.user
      });
      
      // The API is returning a token but sometimes no user data. Let's handle this case.
      if (!response.data.token) {
        throw new Error('Server returned success but missing token');
      }
      
      // If we have a token but no user, create a default user object from email
      const userData = response.data.user || {
        id: 0, // We'll use a placeholder ID
        email: formData.email,
        firstName: formData.email.split('@')[0], // Extract name from email
        lastName: '',
        role: 'TEACHER' // Default role
      };
      
      console.log('Using user data:', userData);
      
      // Store auth data directly
      console.log('Saving authentication data to localStorage');
      
      // Add a timestamp to indicate fresh login data - this will prevent other components
      // from clearing this data during initial validation
      localStorage.setItem('auth_timestamp', Date.now().toString());
      localStorage.setItem('token', response.data.token);
      localStorage.setItem('user', JSON.stringify(userData));
      
      // Store the role separately for more reliable role-based UI decisions
      localStorage.setItem('userRole', userData.role);
      
      // Log stored data synchronously to confirm it was saved
      const storedToken = localStorage.getItem('token');
      const storedUser = localStorage.getItem('user');
      console.log('Stored authentication data:', {
        tokenSaved: !!storedToken,
        userDataSaved: !!storedUser
      });
      
      toast({
        title: 'Success',
        description: 'Logged in successfully!',
      });
      
      // Update authentication state
      if (setIsAuthenticated) {
        setIsAuthenticated(true);
      }
      navigate('/home');
    } catch (error: any) {
      console.error('Login error:', error.response?.data || error.message);
      toast({
        title: 'Error',
        description: error.response?.data?.message || 'Invalid credentials',
        variant: 'destructive',
      });
    } finally {
      setLoading(false);
    }
  };

  const handleGoogleLogin = async () => {
    // Initialize Google Sign-In
    const auth2 = await (window as any).google.accounts.oauth2.initTokenClient({
      client_id: '717702285338-tn14846cq9li6i0n5qp2r8npl784a1h9.apps.googleusercontent.com',
      scope: 'email profile',
      callback: async (response: any) => {
        if (response.access_token) {
          try {
            // Clear any existing auth data first
            clearAndLogAuth();
            
            const authResponse = await authApi.googleLogin(response.access_token);
            console.log('Google login response:', authResponse.data);
            
            // Store auth data
            localStorage.setItem('token', authResponse.data.token);
            localStorage.setItem('user', JSON.stringify(authResponse.data.user));
            
            // Verify data was stored correctly
            logAuthState();
            
            toast({
              title: 'Success',
              description: 'Logged in with Google successfully!',
            });
            
            // Update authentication state
            if (setIsAuthenticated) {
              setIsAuthenticated(true);
            }
            navigate('/home');
          } catch (error: any) {
            console.error('Google login error:', error.response?.data || error.message);
            toast({
              title: 'Error',
              description: error.response?.data?.message || 'Failed to authenticate with Google',
              variant: 'destructive',
            });
          }
        }
      },
    });
    auth2.requestAccessToken();
  };

  return (
    <div className="min-h-screen flex items-center justify-center bg-gray-50">
      <div className="max-w-md w-full space-y-8 p-8 bg-white rounded-lg shadow-lg">
        <div>
          <h2 className="mt-6 text-center text-3xl font-extrabold text-gray-900">
            Sign in to SPOT
          </h2>
        </div>
        <form className="mt-8 space-y-6" onSubmit={handleSubmit}>
          <div className="rounded-md shadow-sm -space-y-px">
            <div>
              <label htmlFor="email-address" className="sr-only">
                Email address
              </label>
              <input
                id="email-address"
                name="email"
                type="email"
                autoComplete="email"
                required
                value={formData.email}
                onChange={handleInputChange}
                className="appearance-none rounded-none relative block w-full px-3 py-2 border border-gray-300 placeholder-gray-500 text-gray-900 rounded-t-md focus:outline-none focus:ring-primary focus:border-primary focus:z-10 sm:text-sm"
                placeholder="Email address"
              />
            </div>
            <div>
              <label htmlFor="password" className="sr-only">
                Password
              </label>
              <input
                id="password"
                name="password"
                type="password"
                autoComplete="current-password"
                required
                value={formData.password}
                onChange={handleInputChange}
                className="appearance-none rounded-none relative block w-full px-3 py-2 border border-gray-300 placeholder-gray-500 text-gray-900 rounded-b-md focus:outline-none focus:ring-primary focus:border-primary focus:z-10 sm:text-sm"
                placeholder="Password"
              />
            </div>
          </div>

          <div>
            <Button type="submit" className="w-full" disabled={loading}>
              {loading ? 'Signing in...' : 'Sign in'}
            </Button>
          </div>

          <div className="flex items-center justify-between">
            <div className="text-sm">
              <Link to="/signup" className="font-medium text-primary hover:text-primary/90">
                Don't have an account? Sign up
              </Link>
            </div>
          </div>

          <div className="mt-6">
            <div className="relative">
              <div className="absolute inset-0 flex items-center">
                <div className="w-full border-t border-gray-300" />
              </div>
              <div className="relative flex justify-center text-sm">
                <span className="px-2 bg-white text-gray-500">Or continue with</span>
              </div>
            </div>

            <div className="mt-6">
              <Button
                variant="outline"
                className="w-full"
                onClick={handleGoogleLogin}
              >
                <svg className="w-5 h-5 mr-2" viewBox="0 0 24 24">
                  <path
                    fill="currentColor"
                    d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92c-.26 1.37-1.04 2.53-2.21 3.31v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.09z"
                  />
                  <path
                    fill="currentColor"
                    d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z"
                  />
                  <path
                    fill="currentColor"
                    d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.07H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.93l2.85-2.22.81-.62z"
                  />
                  <path
                    fill="currentColor"
                    d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.07l3.66 2.84c.87-2.6 3.3-4.53 6.16-4.53z"
                  />
                </svg>
                Sign in with Google
              </Button>
            </div>
          </div>
        </form>
      </div>
    </div>
  )
}
