import { Routes, Route } from 'react-router-dom'
import { Toaster } from '@/components/ui/toaster'

// Landing Pages
import LandingPage from '@/pages/Landing'
import ProblemPage from '@/pages/Landing/Problem'
import SolutionPage from '@/pages/Landing/Solution'
import TeamPage from '@/pages/Landing/Team'

// Auth Pages
import LoginPage from '@/pages/Auth/Login'
import SignUpPage from '@/pages/Auth/SignUp'

// Main App Pages
import HomePage from '@/pages/MainApp/Home'
import ManageSeatsPage from '@/pages/MainApp/ManageSeats'
import AttendancePage from '@/pages/MainApp/Attendance'
import AnalyticsPage from '@/pages/MainApp/Analytics'
import QRGenerationPage from '@/pages/MainApp/QRGeneration'

function App() {
  return (
    <>
      <Routes>
        {/* Landing Routes */}
        <Route path="/" element={<LandingPage />} />
        <Route path="/problem" element={<ProblemPage />} />
        <Route path="/solution" element={<SolutionPage />} />
        <Route path="/team" element={<TeamPage />} />

        {/* Auth Routes */}
        <Route path="/login" element={<LoginPage />} />
        <Route path="/signup" element={<SignUpPage />} />

        {/* Main App Routes */}
        <Route path="/home" element={<HomePage />} />
        <Route path="/manageseats" element={<ManageSeatsPage />} />
        <Route path="/attendance" element={<AttendancePage />} />
        <Route path="/analytics" element={<AnalyticsPage />} />
        <Route path="/qrgeneration" element={<QRGenerationPage />} />
      </Routes>
      <Toaster />
    </>
  )
}

export default App
