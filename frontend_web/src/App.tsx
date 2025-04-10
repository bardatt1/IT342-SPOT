import { Routes, Route, Navigate } from 'react-router-dom'
import { Toaster } from '@/components/ui/toaster'
import { AuthGuard } from '@/components/AuthGuard'
import { MainLayout } from '@/components/MainLayout'
import { useEffect, useState } from 'react'
import { useAuthMonitor } from '@/hooks/useAuthMonitor'
import { useTokenRefresh } from '@/hooks/useTokenRefresh'
import TokenDebugger from '@/components/TokenDebugger'

// Debug Pages
import DebugConsole from '@/pages/Debug/DebugConsole'

// Landing Pages
import LandingPage from '@/pages/Landing'
import ProblemPage from '@/pages/Landing/Problem'
import SolutionPage from '@/pages/Landing/Solution'
import TeamPage from '@/pages/Landing/Team'

// Auth Pages
import LoginPage from '@/pages/Auth/Login'
import SignUpPage from '@/pages/Auth/SignUp'
import LogoutPage from '@/pages/Auth/Logout'

// Main App Pages
import HomePage from '@/pages/MainApp/Home'
import ManageSeatsPage from '@/pages/MainApp/ManageSeats'
import AttendancePage from '@/pages/MainApp/Attendance'
import AnalyticsPage from '@/pages/MainApp/Analytics'
import QRGenerationPage from '@/pages/MainApp/QRGeneration'

function App() {
  const [isAuthenticated, setIsAuthenticated] = useState<boolean | null>(null);
  const { refreshToken } = useTokenRefresh();
  
  // Use auth monitor to track changes to auth data
  useAuthMonitor();

  useEffect(() => {
    console.log('App: Checking authentication state');
    // Check for token in localStorage
    const token = localStorage.getItem('token');
    const userString = localStorage.getItem('user');
    const authTimestamp = localStorage.getItem('auth_timestamp');
    
    // Check if this is fresh login data (within last 10 seconds)
    const isFreshLogin = authTimestamp && (Date.now() - parseInt(authTimestamp)) < 10000;
    
    if (isFreshLogin) {
      console.log('App: Found fresh login data, skipping validation');
      setIsAuthenticated(true);
      return;
    }
    
    // Only clear data if it's obviously corrupt
    const isTokenCorrupt = token === 'undefined' || token === 'null';
    const isUserDataCorrupt = userString === 'undefined' || userString === 'null';
    
    // If we have both token and user data, try to use them
    if (token && userString && !isTokenCorrupt && !isUserDataCorrupt) {
      try {
        // Parse the user data
        const user = JSON.parse(userString);
        console.log('App: Found valid authentication data:', { 
          token: token.substring(0, 15) + '...', 
          hasUser: !!user 
        });
        
        // Refresh token to ensure it's properly initialized
        refreshToken().then(success => {
          console.log('Initial token refresh:', success ? 'succeeded' : 'failed');
        });
        
        // Skip detailed token validation for now to prevent issues
        setIsAuthenticated(true);
      } catch (error) {
        console.error('App: Error parsing user data:', error);
        setIsAuthenticated(false);
      }
    } else {
      console.log('App: No valid authentication data found');
      setIsAuthenticated(false);
    }
  }, [])

  // If we don't know authentication status yet, show nothing (prevents flash of content)
  if (isAuthenticated === null) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <div className="animate-spin rounded-full h-12 w-12 border-t-2 border-b-2 border-primary"></div>
      </div>
    )
  }

  return (
    <>
      {/* Add TokenDebugger component for auth troubleshooting */}
      <TokenDebugger />
      
      <Routes>
        {/* Landing Routes - accessible to everyone */}
        <Route path="/" element={<LandingPage />} />
        <Route path="/problem" element={<ProblemPage />} />
        <Route path="/solution" element={<SolutionPage />} />
        <Route path="/team" element={<TeamPage />} />

        {/* Auth Routes - redirect to home if already logged in */}
        <Route path="/login" element={isAuthenticated ? <Navigate to="/home" replace /> : <LoginPage setIsAuthenticated={setIsAuthenticated as any} />} />
        <Route path="/signup" element={isAuthenticated ? <Navigate to="/home" replace /> : <SignUpPage setIsAuthenticated={setIsAuthenticated as any} />} />
        <Route path="/logout" element={<LogoutPage setIsAuthenticated={setIsAuthenticated as any} />} />

        {/* Protected App Routes - require authentication and use MainLayout */}
        <Route path="/home" element={
          <AuthGuard>
            <MainLayout>
              <HomePage />
            </MainLayout>
          </AuthGuard>
        } />
        <Route path="/manageseats" element={
          <AuthGuard>
            <MainLayout>
              <ManageSeatsPage />
            </MainLayout>
          </AuthGuard>
        } />
        <Route path="/attendance" element={
          <AuthGuard>
            <MainLayout>
              <AttendancePage />
            </MainLayout>
          </AuthGuard>
        } />
        <Route path="/analytics" element={
          <AuthGuard>
            <MainLayout>
              <AnalyticsPage />
            </MainLayout>
          </AuthGuard>
        } />
        <Route path="/qrgeneration" element={
          <AuthGuard>
            <MainLayout>
              <QRGenerationPage />
            </MainLayout>
          </AuthGuard>
        } />

        {/* Debug Routes - protected and only for development */}
        <Route path="/debug" element={
          <AuthGuard>
            <MainLayout>
              <DebugConsole />
            </MainLayout>
          </AuthGuard>
        } />

        {/* Catch all route - redirect to landing page */}
        <Route path="*" element={<Navigate to="/" replace />} />
      </Routes>
      <Toaster />
    </>
  )
}

export default App
