import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../../contexts/AuthContext';
import { Button } from '../../components/ui/button';
// Google OAuth temporarily disabled
// import { GoogleLogin } from '@react-oauth/google';
import axios from 'axios';

const Login = () => {
  const { login } = useAuth();
  const navigate = useNavigate();
  
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState<string | null>(null);
  const [isLoading, setIsLoading] = useState(false);

  // No need for currentUser here as we use the API response directly

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);
    setIsLoading(true);
    
    try {
      console.log('Attempting login with:', email);
      
      // Make direct API call to login
      const apiUrl = import.meta.env.VITE_API_URL || 'http://localhost:8080/api';
      const response = await axios.post(`${apiUrl}/auth/login`, { email, password });
      console.log('Login API response:', response.data);
      
      // Extract data directly from response
      const responseData = response.data.data || response.data;
      const userType = responseData.userType;
      
      // Update auth context - this will parse the response correctly
      await login(email, password);
      console.log('Login successful through AuthContext');
      
      // Determine redirect based on user type from API response
      let targetRoute = '/';
      
      if (userType === 'ADMIN') {
        console.log('User is ADMIN, redirecting to admin dashboard');
        targetRoute = '/admin/dashboard';
      } else if (userType === 'TEACHER') {
        console.log('User is TEACHER, redirecting to teacher dashboard');
        targetRoute = '/teacher/dashboard';
      }
      
      // Use navigate for spa navigation
      console.log(`Redirecting to ${targetRoute}`);
      navigate(targetRoute);
      // You can also use this alternative approach for a full page reload if needed
      // window.location.href = targetRoute;
    } catch (error) {
      console.error('Login error:', error);
      setError('Invalid email or password. Please try again.');
    } finally {
      setIsLoading(false);
    }
  };

  // Google OAuth temporarily disabled
  /*
  const handleGoogleSuccess = async (credentialResponse: any) => {
    if (credentialResponse.credential) {
      try {
        // Process Google OAuth token and get user data
        const apiUrl = import.meta.env.VITE_API_URL || 'http://localhost:8080/api';
        
        // Make API call to process the Google credential
        const response = await axios.post(`${apiUrl}/auth/google/callback`, {
          credential: credentialResponse.credential
        });
        
        // Extract data from response
        const responseData = response.data.data;
        const token = responseData.accessToken; 
        const userType = responseData.userType;
        
        // Save token to localStorage
        localStorage.setItem('token', token);
        
        // Set default auth header for future requests
        axios.defaults.headers.common['Authorization'] = `Bearer ${token}`;
        
        // Update auth context
        await login('', ''); // This is just a placeholder; we already have the token
        
        // Navigate based on userType
        if (userType === 'ADMIN') {
          navigate('/admin/dashboard');
        } else if (userType === 'TEACHER') {
          navigate('/teacher/dashboard');
        } else {
          navigate('/');
        }
      } catch (error) {
        console.error('Google login error:', error);
        setError('Google login failed. Please try again.');
      }
    }
  };
  */

  // Google OAuth temporarily disabled
  /*
  const handleGoogleError = () => {
    setError('Google login failed. Please try again or use email/password.');
  };
  */

  return (
    <div className="flex min-h-screen items-center justify-center bg-gray-50 py-12 px-4 sm:px-6 lg:px-8">
      <div className="w-full max-w-md space-y-8">
        <div>
          <h2 className="mt-6 text-center text-3xl font-extrabold text-gray-900">
            Sign in to SPOT
          </h2>
          <p className="mt-2 text-center text-sm text-gray-600">
            Student Presence and Oversight Tracker
          </p>
        </div>
        
        {error && (
          <div className="rounded-md bg-red-50 p-4">
            <div className="flex">
              <div className="ml-3">
                <h3 className="text-sm font-medium text-red-800">Error</h3>
                <div className="mt-2 text-sm text-red-700">
                  <p>{error}</p>
                </div>
              </div>
            </div>
          </div>
        )}
        
        <form className="mt-8 space-y-6" onSubmit={handleSubmit}>
          <div className="-space-y-px rounded-md shadow-sm">
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
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                className="relative block w-full rounded-t-md border-0 py-1.5 text-gray-900 ring-1 ring-inset ring-gray-300 placeholder:text-gray-400 focus:z-10 focus:ring-2 focus:ring-inset focus:ring-indigo-600 sm:text-sm sm:leading-6"
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
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                className="relative block w-full rounded-b-md border-0 py-1.5 text-gray-900 ring-1 ring-inset ring-gray-300 placeholder:text-gray-400 focus:z-10 focus:ring-2 focus:ring-inset focus:ring-indigo-600 sm:text-sm sm:leading-6"
                placeholder="Password"
              />
            </div>
          </div>

          <div>
            <Button
              type="submit"
              className="w-full"
              disabled={isLoading}
            >
              {isLoading ? 'Signing in...' : 'Sign in'}
            </Button>
          </div>
        </form>
        
        {/* Google OAuth login temporarily disabled to avoid origin errors */}
        {/* Uncomment this section once Google OAuth is properly configured
        <div className="mt-6">
          <div className="relative">
            <div className="absolute inset-0 flex items-center">
              <div className="w-full border-t border-gray-300" />
            </div>
            <div className="relative flex justify-center text-sm">
              <span className="bg-gray-50 px-2 text-gray-500">Or continue with</span>
            </div>
          </div>

          <div className="mt-6 flex justify-center">
            <GoogleLogin
              onSuccess={handleGoogleSuccess}
              onError={handleGoogleError}
              text="signin_with"
              shape="rectangular"
              theme="filled_blue"
            />
          </div>
        </div>
        */}
      </div>
    </div>
  );
};

export default Login;
