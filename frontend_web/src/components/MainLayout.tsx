import { useState } from 'react';
import { Link, useNavigate, useLocation } from 'react-router-dom';
import { 
  Home, 
  Grid, 
  BarChart4, 
  QrCode, 
  Users, 
  LogOut,
  Menu,
  X
} from 'lucide-react';

interface NavItemProps {
  to: string;
  icon: React.ReactNode;
  label: string;
  isActive: boolean;
  onClick?: () => void;
}

const NavItem = ({ to, icon, label, isActive, onClick }: NavItemProps) => (
  <Link
    to={to}
    onClick={onClick}
    className={`flex items-center space-x-3 px-4 py-3 rounded-lg transition-colors ${
      isActive
        ? 'bg-primary text-white'
        : 'text-gray-600 hover:bg-gray-100'
    }`}
  >
    <div>{icon}</div>
    <span className="font-medium">{label}</span>
  </Link>
);

export const MainLayout = ({ children }: { children: React.ReactNode }) => {
  const navigate = useNavigate();
  const location = useLocation();
  const [isMobileMenuOpen, setIsMobileMenuOpen] = useState(false);

  const handleLogout = () => {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    navigate('/login');
  };

  // Get the current user from localStorage
  const userString = localStorage.getItem('user');
  let user = null;
  try {
    user = userString && userString !== 'undefined' ? JSON.parse(userString) : null;
  } catch (error) {
    console.error('Error parsing user data:', error);
    // If there's an error parsing, remove the invalid data
    localStorage.removeItem('user');
  }

  const navItems = [
    {
      to: '/home',
      icon: <Home size={20} />,
      label: 'Dashboard',
    },
    {
      to: '/manageseats',
      icon: <Grid size={20} />,
      label: 'Manage Seats',
    },
    {
      to: '/attendance',
      icon: <Users size={20} />,
      label: 'Attendance',
    },
    {
      to: '/analytics',
      icon: <BarChart4 size={20} />,
      label: 'Analytics',
    },
    {
      to: '/qrgeneration',
      icon: <QrCode size={20} />,
      label: 'QR Codes',
    },
  ];

  const toggleMobileMenu = () => {
    setIsMobileMenuOpen(!isMobileMenuOpen);
  };

  const closeMobileMenu = () => {
    setIsMobileMenuOpen(false);
  };

  return (
    <div className="flex flex-col md:flex-row min-h-screen bg-gray-50">
      {/* Mobile menu button */}
      <div className="md:hidden fixed top-0 left-0 right-0 bg-white z-30 flex justify-between items-center p-4 border-b shadow-sm">
        <div className="flex items-center">
          <span className="text-xl font-bold text-primary">SPOT</span>
        </div>
        <button
          onClick={toggleMobileMenu}
          className="p-2 rounded-md text-gray-700 hover:bg-gray-100"
        >
          {isMobileMenuOpen ? <X size={24} /> : <Menu size={24} />}
        </button>
      </div>

      {/* Mobile sidebar overlay */}
      {isMobileMenuOpen && (
        <div
          className="fixed inset-0 bg-black bg-opacity-50 z-30 md:hidden"
          onClick={closeMobileMenu}
        ></div>
      )}

      {/* Sidebar */}
      <div
        className={`fixed z-40 md:relative md:z-auto w-[280px] transition-all duration-300 transform ${
          isMobileMenuOpen ? 'translate-x-0' : '-translate-x-full'
        } md:translate-x-0 bg-white h-full overflow-y-auto flex flex-col shadow-lg`}
      >
        <div className="p-6 border-b">
          <div className="text-2xl font-bold text-primary">SPOT</div>
          <div className="text-sm text-gray-500">Student Presence Observation Tool</div>
        </div>

        {user && (
          <div className="p-4 border-b">
            <div className="flex items-center space-x-3">
              <div className="flex items-center justify-center w-10 h-10 rounded-full bg-primary text-white">
                {user.firstName?.charAt(0) || ''}
                {user.lastName?.charAt(0) || ''}
              </div>
              <div>
                <div className="font-medium">
                  {user.firstName} {user.lastName}
                </div>
                <div className="text-sm text-gray-500">{user.email}</div>
              </div>
            </div>
          </div>
        )}

        <div className="p-4 flex-1">
          <nav className="flex flex-col space-y-1">
            {navItems.map((item) => (
              <NavItem
                key={item.to}
                to={item.to}
                icon={item.icon}
                label={item.label}
                isActive={location.pathname === item.to}
                onClick={closeMobileMenu}
              />
            ))}
          </nav>
        </div>

        <div className="p-4 border-t">
          <button
            onClick={handleLogout}
            className="flex w-full items-center space-x-3 px-4 py-3 text-red-600 hover:bg-red-50 rounded-lg transition-colors"
          >
            <LogOut size={20} />
            <span className="font-medium">Logout</span>
          </button>
        </div>
      </div>

      {/* Main content */}
      <div className="flex-1 md:ml-0 pt-16 md:pt-0">
        <main className="p-6">{children}</main>
      </div>
    </div>
  );
};
