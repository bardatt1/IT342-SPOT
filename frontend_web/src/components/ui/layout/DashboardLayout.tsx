import { ReactNode, useState } from 'react';
import { Link, useNavigate, useLocation } from 'react-router-dom';
import { useAuth } from '../../../contexts/AuthContext';
import { LogOut, Menu, X, User, BookOpen, Users, Calendar, BarChart } from 'lucide-react';
import { Button } from '../button';

interface DashboardLayoutProps {
  children: ReactNode;
}

const DashboardLayout = ({ children }: DashboardLayoutProps) => {
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const [sidebarOpen, setSidebarOpen] = useState(false);

  const isAdmin = user?.role === 'ADMIN';

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  const adminNavItems = [
    { name: 'Dashboard', href: '/admin/dashboard', icon: <BarChart className="w-5 h-5" /> },
    { name: 'Users', href: '/admin/users', icon: <Users className="w-5 h-5" /> },
    { name: 'Courses', href: '/admin/courses', icon: <BookOpen className="w-5 h-5" /> },
    { name: 'Sections', href: '/admin/sections', icon: <Calendar className="w-5 h-5" /> },
    { name: 'Schedules', href: '/admin/schedules', icon: <Calendar className="w-5 h-5" /> },
  ];

  const teacherNavItems = [
    { name: 'Dashboard', href: '/teacher/dashboard', icon: <BarChart className="w-5 h-5" /> },
    { name: 'My Sections', href: '/teacher/sections', icon: <Calendar className="w-5 h-5" /> },
    { name: 'Attendance', href: '/teacher/attendance', icon: <Users className="w-5 h-5" /> },
    { name: 'Analytics', href: '/teacher/analytics', icon: <BarChart className="w-5 h-5" /> },
    { name: 'Seat Management', href: '/teacher/seats', icon: <User className="w-5 h-5" /> },
  ];

  const navItems = isAdmin ? adminNavItems : teacherNavItems;

  return (
    <div className="flex h-screen bg-gray-100">
      {/* Mobile sidebar */}
      <div className="lg:hidden">
        <Button
          variant="ghost"
          size="icon"
          className="fixed right-4 top-4 z-40"
          onClick={() => setSidebarOpen(!sidebarOpen)}
        >
          {sidebarOpen ? <X className="h-6 w-6" /> : <Menu className="h-6 w-6" />}
        </Button>
      </div>

      {/* Sidebar for mobile */}
      {sidebarOpen && (
        <div className="fixed inset-0 z-30 bg-black bg-opacity-30 lg:hidden" onClick={() => setSidebarOpen(false)} />
      )}

      {/* Sidebar */}
      <div
        className={`fixed inset-y-0 left-0 z-30 w-64 transform bg-white shadow-lg transition-transform duration-300 ease-in-out lg:translate-x-0 lg:static lg:inset-auto lg:z-auto ${
          sidebarOpen ? 'translate-x-0' : '-translate-x-full'
        }`}
      >
        <div className="flex h-full flex-col">
          <div className="flex h-16 items-center justify-center border-b">
            <h1 className="text-xl font-semibold">SPOT</h1>
          </div>

          <div className="flex-1 overflow-y-auto py-4">
            <nav className="space-y-1 px-2">
              {navItems.map((item) => (
                <Link
                  key={item.name}
                  to={item.href}
                  className={`group flex items-center rounded-md px-2 py-2 text-sm font-medium ${
                    location.pathname === item.href
                      ? 'bg-gray-100 text-gray-900'
                      : 'text-gray-600 hover:bg-gray-50 hover:text-gray-900'
                  }`}
                >
                  {item.icon}
                  <span className="ml-3">{item.name}</span>
                </Link>
              ))}
            </nav>
          </div>

          <div className="border-t border-gray-200 p-4">
            <div className="flex items-center">
              <div className="flex-shrink-0">
                <User className="h-8 w-8 rounded-full" />
              </div>
              <div className="ml-3">
                <p className="text-sm font-medium text-gray-700">{user?.name}</p>
                <p className="text-xs font-medium text-gray-500">{user?.role}</p>
              </div>
            </div>
            <div className="mt-3">
              <Button
                variant="outline"
                className="w-full justify-start"
                onClick={handleLogout}
              >
                <LogOut className="mr-2 h-4 w-4" />
                Sign out
              </Button>
            </div>
          </div>
        </div>
      </div>

      {/* Main content */}
      <div className="flex flex-1 flex-col overflow-hidden">
        <header className="bg-white shadow-sm">
          <div className="px-4 py-4 sm:px-6 lg:px-8">
            <h1 className="text-lg font-semibold leading-6 text-gray-900">
              {/* Dynamic title based on path */}
              {location.pathname.includes('/dashboard')
                ? 'Dashboard'
                : location.pathname.includes('/users')
                ? 'User Management'
                : location.pathname.includes('/courses')
                ? 'Courses'
                : location.pathname.includes('/sections')
                ? 'Sections'
                : location.pathname.includes('/attendance')
                ? 'Attendance'
                : location.pathname.includes('/analytics')
                ? 'Analytics'
                : ''}
            </h1>
          </div>
        </header>
        <main className="flex-1 overflow-auto bg-gray-50 p-4 sm:p-6 lg:p-8">{children}</main>
      </div>
    </div>
  );
};

export default DashboardLayout;
