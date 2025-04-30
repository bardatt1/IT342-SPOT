import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { GoogleOAuthProvider } from '@react-oauth/google';
import { AuthProvider } from './contexts/AuthContext';
import ProtectedRoute from './components/auth/ProtectedRoute';

// Authentication Pages
import Login from './pages/auth/Login';

// Admin Pages
import AdminDashboard from './pages/admin/Dashboard';
import UserManagement from './pages/admin/UserManagement';
import CourseManagement from './pages/admin/CourseManagement';
import SectionManagement from './pages/admin/SectionManagement';
import ScheduleManagement from './pages/admin/ScheduleManagement';

// Teacher Pages
import TeacherDashboard from './pages/teacher/Dashboard';
import TeacherSections from './pages/teacher/Sections';
import AttendanceTracking from './pages/teacher/AttendanceTracking';
// import Analytics from './pages/teacher/Analytics';
import SeatManagement from './pages/teacher/SeatManagement';
import TeacherProfile from './pages/teacher/TeacherProfile';

// Import our error handling components
import ErrorBoundary from './components/error/ErrorBoundary';
import ErrorFallback from './components/error/ErrorFallback';

function App() {
  // Google OAuth Client ID from environment variable
  const googleClientId = import.meta.env.VITE_GOOGLE_CLIENT_ID || '';

  return (
    <ErrorBoundary
      fallback={
        <ErrorFallback 
          message="Something went wrong in the application"
          showDetails={import.meta.env.DEV}
        />
      }
    >
      <GoogleOAuthProvider clientId={googleClientId}>
        <AuthProvider>
          <Router>
            <Routes>
              {/* Public Routes */}
              <Route path="/login" element={<Login />} />
              
              {/* Redirect root to login */}
              <Route path="/" element={<Navigate to="/login" replace />} />
              
              {/* Admin Routes */}
              <Route 
                path="/admin/dashboard" 
                element={
                  <ProtectedRoute allowedRoles={['ADMIN']}>
                    <AdminDashboard />
                  </ProtectedRoute>
                } 
              />
              <Route 
                path="/admin/users" 
                element={
                  <ProtectedRoute allowedRoles={['ADMIN']}>
                    <UserManagement />
                  </ProtectedRoute>
                } 
              />
              <Route 
                path="/admin/courses" 
                element={
                  <ProtectedRoute allowedRoles={['ADMIN']}>
                    <CourseManagement />
                  </ProtectedRoute>
                } 
              />
              <Route 
                path="/admin/sections" 
                element={
                  <ProtectedRoute allowedRoles={['ADMIN']}>
                    <SectionManagement />
                  </ProtectedRoute>
                } 
              />
              <Route 
                path="/admin/schedules" 
                element={
                  <ProtectedRoute allowedRoles={['ADMIN']}>
                    <ScheduleManagement />
                  </ProtectedRoute>
                } 
              />
              
              {/* Teacher Routes */}
              <Route 
                path="/teacher/dashboard" 
                element={
                  <ProtectedRoute allowedRoles={['TEACHER']}>
                    <TeacherDashboard />
                  </ProtectedRoute>
                } 
              />
              <Route 
                path="/teacher/sections" 
                element={
                  <ProtectedRoute allowedRoles={['TEACHER']}>
                    <TeacherSections />
                  </ProtectedRoute>
                } 
              />
              <Route 
                path="/teacher/attendance" 
                element={
                  <ProtectedRoute allowedRoles={['TEACHER']}>
                    <AttendanceTracking />
                  </ProtectedRoute>
                } 
              />
              {/* <Route 
                path="/teacher/analytics" 
                element={
                  <ProtectedRoute allowedRoles={['TEACHER']}>
                    <Analytics />
                  </ProtectedRoute>
                } 
              /> */}
              <Route 
                path="/teacher/seats" 
                element={
                  <ProtectedRoute allowedRoles={['TEACHER']}>
                    <SeatManagement />
                  </ProtectedRoute>
                } 
              />
              <Route 
                path="/teacher/profile" 
                element={
                  <ProtectedRoute allowedRoles={['TEACHER']}>
                    <TeacherProfile />
                  </ProtectedRoute>
                } 
              />
              
              {/* Dashboard route that redirects based on role */}
              <Route 
                path="/dashboard" 
                element={
                  <ProtectedRoute>
                    {({ user }: any) => {
                      if (user?.role === 'ADMIN') {
                        return <Navigate to="/admin/dashboard" replace />;
                      } else if (user?.role === 'TEACHER') {
                        return <Navigate to="/teacher/dashboard" replace />;
                      } else {
                        // Fallback - shouldn't happen for web app
                        return <Navigate to="/login" replace />;
                      }
                    }}
                  </ProtectedRoute>
                } 
              />
              
              {/* Fallback route */}
              <Route path="*" element={<Navigate to="/login" replace />} />
            </Routes>
          </Router>
        </AuthProvider>
      </GoogleOAuthProvider>
    </ErrorBoundary>
  );
}

export default App;
