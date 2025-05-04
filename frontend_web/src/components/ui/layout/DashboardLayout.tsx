import { ReactNode, useState, useEffect } from 'react';
import { Link, useNavigate, useLocation } from 'react-router-dom';
import { useAuth } from '../../../contexts/AuthContext';
import { LogOut, User, BookOpen, Users, Calendar, BarChart } from 'lucide-react';
import { teacherApi } from '../../../lib/api/teacher';
import { Button } from '../button';
import { Avatar, AvatarFallback } from '../avatar';
import {
  Sidebar,
  SidebarContent,
  SidebarFooter,
  SidebarHeader,
  SidebarMenu,
  SidebarMenuItem,
  SidebarMenuButton,
  SidebarProvider,
  SidebarTrigger,
  SidebarInset,
} from '../sidebar';

interface DashboardLayoutProps {
  children: ReactNode;
}

const DashboardLayout = ({ children }: DashboardLayoutProps) => {
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  // Initialize teacherName from localStorage if available
  const [teacherName, setTeacherName] = useState<{firstName: string, lastName: string} | null>(() => {
    const cachedData = localStorage.getItem('teacherName');
    return cachedData ? JSON.parse(cachedData) : null;
  });

  const isAdmin = user?.role === 'ADMIN';
  
  // Fetch teacher details if user is not admin - only once when component mounts or user changes
  useEffect(() => {
    const fetchTeacherDetails = async () => {
      // Only fetch if we don't already have the teacher name and user is a teacher
      if (!isAdmin && user?.id && !teacherName) {
        try {
          const teacherData = await teacherApi.getCurrentTeacher();
          const nameData = {
            firstName: teacherData.firstName || '',
            lastName: teacherData.lastName || ''
          };
          
          // Save to state and localStorage
          setTeacherName(nameData);
          localStorage.setItem('teacherName', JSON.stringify(nameData));
        } catch (error) {
          console.error('Error fetching teacher details for sidebar:', error);
        }
      }
    };
    
    fetchTeacherDetails();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [isAdmin, user?.id]);

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  const adminNavItems = [
    { name: 'Dashboard', href: '/admin/dashboard', icon: <BarChart className="w-5 h-5" /> },
    { name: 'Users', href: '/admin/users', icon: <Users className="w-5 h-5" /> },
    { name: 'Courses', href: '/admin/courses', icon: <BookOpen className="w-5 h-5" /> },
    { name: 'Sections & Schedules', href: '/admin/sections', icon: <Calendar className="w-5 h-5" /> },
    //{ name: 'Schedules', href: '/admin/schedules', icon: <Calendar className="w-5 h-5" /> },
  ];

  const teacherNavItems = [
    { name: 'Dashboard', href: '/teacher/dashboard', icon: <BarChart className="w-5 h-5" /> },
    { name: 'My Sections', href: '/teacher/sections', icon: <Calendar className="w-5 h-5" /> },
    { name: 'Attendance & Analytics', href: '/teacher/attendance', icon: <Users className="w-5 h-5" /> },
    // { name: 'Analytics', href: '/teacher/analytics', icon: <BarChart className="w-5 h-5" /> },
    { name: 'Seat Management', href: '/teacher/seats', icon: <User className="w-5 h-5" /> },
  ];

  const navItems = isAdmin ? adminNavItems : teacherNavItems;

  // Get initials for avatar
  const getInitials = () => {
    if (teacherName) {
      return `${teacherName.firstName.charAt(0)}${teacherName.lastName.charAt(0)}`;
    }
    return user?.name?.split(' ').map(n => n[0]).join('') || user?.email?.charAt(0).toUpperCase() || 'U';
  };

  // Get current page title
  const getPageTitle = () => {
    const path = location.pathname;
    if (path.includes('/dashboard')) return 'Dashboard';
    if (path.includes('/users')) return 'User Management';
    if (path.includes('/courses')) return 'Courses';
    if (path.includes('/sections')) return 'Sections';
    if (path.includes('/attendance')) return 'Attendance';
    if (path.includes('/analytics')) return 'Analytics';
    if (path.includes('/seats')) return 'Seat Management';
    if (path.includes('/schedules')) return 'Schedules';
    return '';
  };

  return (
    <SidebarProvider>
      <div className="flex h-screen bg-background">
        <Sidebar>
          <SidebarHeader>
            <div className="flex h-14 items-center px-4">
              <div className="flex items-center gap-2">
                <img src="/spot-logo.png" alt="SPOT Logo" className="h-6 w-6" />
                <h1 className="text-xl font-semibold">SPOT</h1>
              </div>
            </div>
          </SidebarHeader>
          
          <SidebarContent>
            <SidebarMenu>
              {navItems.map((item) => (
                <SidebarMenuItem key={item.name}>
                  <SidebarMenuButton 
                    asChild 
                    isActive={location.pathname === item.href}
                    tooltip={item.name}
                  >
                    <Link to={item.href}>
                      {item.icon}
                      <span>{item.name}</span>
                    </Link>
                  </SidebarMenuButton>
                </SidebarMenuItem>
              ))}
            </SidebarMenu>
          </SidebarContent>
          
          <SidebarFooter>
            <div className="p-4 border-t border-border">
              <div className="flex items-center gap-4 mb-4">
                <Avatar>
                  <AvatarFallback className="bg-primary/10 text-primary">
                    {getInitials()}
                  </AvatarFallback>
                </Avatar>
                <div className="flex flex-col w-full">
                  <Link to="/teacher/profile" className="hover:underline cursor-pointer">
                    <p className="text-sm font-medium">
                      {teacherName 
                        ? `${teacherName.firstName} ${teacherName.lastName}` 
                        : user?.name || user?.email?.split('@')[0] || 'User'}
                    </p>
                    <p className="text-xs text-muted-foreground">{user?.email}</p>
                  </Link>
                </div>
              </div>
              
              <Button
                variant="outline"
                className="w-full justify-start"
                onClick={handleLogout}
              >
                <LogOut className="mr-2 h-4 w-4" />
                Sign out
              </Button>
            </div>
          </SidebarFooter>
        </Sidebar>
        
        <SidebarInset>
          <header className="sticky top-0 z-10 flex h-14 items-center gap-4 border-b bg-background px-4 sm:px-6">
            <SidebarTrigger />
            <div className="flex-1">
              <h1 className="text-lg font-semibold">{getPageTitle()}</h1>
            </div>
          </header>
          
          <main className="flex-1 overflow-auto p-4 sm:p-6 lg:p-8">
            {children}
          </main>
        </SidebarInset>
      </div>
    </SidebarProvider>
  );
};

export default DashboardLayout;
