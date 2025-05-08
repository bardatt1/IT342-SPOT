import { ReactNode, useState, useEffect } from 'react';
import { Link, useNavigate, useLocation } from 'react-router-dom';
import { useAuth } from '../../../contexts/AuthContext';
import { LogOut, User, BookOpen, Users, Calendar, Clock, BarChart, Shield, UserCog, ChevronDown, ChevronRight } from 'lucide-react';
import { teacherApi } from '../../../lib/api/teacher';
import { Button } from '../button';
import { Avatar, AvatarFallback } from '../avatar';
import { Separator } from '../separator';
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

// Define interfaces for navigation item types
interface NavItemBase {
  type: string;
}

interface NavItem extends NavItemBase {
  type: 'item';
  name: string;
  href: string;
  icon: React.ReactNode;
}

interface NavSeparator extends NavItemBase {
  type: 'separator';
}

interface NavDropdownChild {
  name: string;
  href: string;
  icon: React.ReactNode;
}

interface NavDropdown extends NavItemBase {
  type: 'dropdown';
  name: string;
  href: string;
  icon: React.ReactNode;
  children: NavDropdownChild[];
}

type NavItemType = NavItem | NavSeparator | NavDropdown;

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
  const isSystemAdmin = user?.role === 'SYSTEMADMIN';
  
  // Fetch teacher details if user is a teacher (not admin or system admin) - only once when component mounts or user changes
  useEffect(() => {
    const fetchTeacherDetails = async () => {
      // Only fetch if we don't already have the teacher name and user is a teacher (not admin or system admin)
      if (!isAdmin && !isSystemAdmin && user?.id && !teacherName) {
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

  // System Admin navigation items with separators and nested items
  const systemAdminNavItems: NavItemType[] = [
    { 
      type: 'dropdown', 
      name: 'Dashboard', 
      icon: <Shield className="w-5 h-5" />,
      href: '/system-admin/dashboard',
      children: []
    },
    { type: 'separator' },
    { 
      type: 'dropdown', 
      name: 'Admins', 
      icon: <UserCog className="w-5 h-5" />,
      href: '/system-admin/admins',
      children: []
    },
    { 
      type: 'dropdown', 
      name: 'Users', 
      icon: <Users className="w-5 h-5" />,
      href: '/admin/users',
      children: []
    },
    { type: 'separator' },
    { 
      type: 'dropdown', 
      name: 'Courses', 
      icon: <BookOpen className="w-5 h-5" />,
      href: '/admin/courses',
      children: [
        { name: 'Sections', href: '/admin/sections', icon: <Calendar className="w-5 h-5" /> },
        { name: 'Schedules', href: '/admin/schedules', icon: <Clock className="w-5 h-5" /> }
      ]
    },
  ];
  
  // Admin navigation items with separators and nested items
  const adminNavItems: NavItemType[] = [
    { 
      type: 'dropdown', 
      name: 'Dashboard', 
      icon: <BarChart className="w-5 h-5" />,
      href: '/admin/dashboard',
      children: []
    },
    { type: 'separator' },
    { 
      type: 'dropdown', 
      name: 'User Management', 
      icon: <Users className="w-5 h-5" />,
      href: '/admin/users',
      children: []
    },
    { type: 'separator' },
    { 
      type: 'dropdown', 
      name: 'Courses', 
      icon: <BookOpen className="w-5 h-5" />,
      href: '/admin/courses',
      children: [
        { name: 'Sections', href: '/admin/sections', icon: <Calendar className="w-5 h-5" /> },
        { name: 'Schedules', href: '/admin/schedules', icon: <Clock className="w-5 h-5" /> }
      ]
    },
  ];

  // Teacher navigation items
  const teacherNavItems: NavItemType[] = [
    { 
      type: 'dropdown', 
      name: 'Dashboard', 
      icon: <BarChart className="w-5 h-5" />,
      href: '/teacher/dashboard',
      children: []
    },
    { type: 'separator' },
    { 
      type: 'dropdown', 
      name: 'My Sections', 
      icon: <Calendar className="w-5 h-5" />,
      href: '/teacher/sections',
      children: []
    },
    { 
      type: 'dropdown', 
      name: 'Attendance & Analytics', 
      icon: <Users className="w-5 h-5" />,
      href: '/teacher/attendance',
      children: []
    },
    { 
      type: 'dropdown', 
      name: 'Seat Management', 
      icon: <User className="w-5 h-5" />,
      href: '/teacher/seats',
      children: []
    },
  ];

  let navItems;
  if (isSystemAdmin) {
    navItems = systemAdminNavItems;
  } else if (isAdmin) {
    navItems = adminNavItems;
  } else {
    navItems = teacherNavItems;
  }

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
    if (path.includes('/courses')) return 'Course Management';
    if (path.includes('/sections')) return 'Section Management';
    if (path.includes('/attendance')) return 'Attendance Management';
    if (path.includes('/analytics')) return 'Analytics';
    if (path.includes('/seats')) return 'Seat Management';
    if (path.includes('/schedules')) return 'Schedule Management';
    if (path.includes('/admins')) return 'Admin Management';
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
              {navItems.map((item, index) => {
                // Render a separator
                if (item.type === 'separator') {
                  return <Separator key={`separator-${index}`} className="my-2" />;
                }
                
                // Render a dropdown menu item
                if (item.type === 'dropdown') {
                  const dropdownItem = item as NavDropdown;
                  const [isOpen, setIsOpen] = useState(() => {
                    // Auto-open the dropdown if any of its children is active
                    return dropdownItem.children.some(child => location.pathname === child.href);
                  });
                  
                  const isActive = location.pathname === dropdownItem.href || 
                                  dropdownItem.children.some(child => location.pathname === child.href);
                  
                  // Handle clicking on any menu item to navigate
                  // Only toggle dropdown if it has children
                  const handleItemClick = () => {
                    navigate(dropdownItem.href);
                    if (dropdownItem.children.length > 0) {
                      setIsOpen(!isOpen);
                    }
                  };
                  
                  return (
                    <div key={dropdownItem.name} className="space-y-1">
                      <SidebarMenuItem>
                        <div 
                          className={`flex items-center w-full cursor-pointer rounded-md px-3 py-2 text-sm font-medium transition-colors ${isActive ? 'bg-accent text-accent-foreground' : 'text-muted-foreground hover:bg-accent hover:text-accent-foreground'}`}
                          onClick={handleItemClick}
                        >
                          <div className="flex items-center w-full">
                            <div className="flex-shrink-0 w-5 flex justify-center">
                              {dropdownItem.icon}
                            </div>
                            <span className="ml-3">{dropdownItem.name}</span>
                            {/* Only show chevron if this item has children */}
                            {dropdownItem.children.length > 0 && (
                              <div className="ml-auto">
                                {isOpen ? 
                                  <ChevronDown className="h-4 w-4 transition-transform duration-200" /> : 
                                  <ChevronRight className="h-4 w-4 transition-transform duration-200" />}
                              </div>
                            )}
                          </div>
                        </div>
                      </SidebarMenuItem>
                      
                      {/* Dropdown children with transition - only render if there are children */}
                      {dropdownItem.children.length > 0 && (
                        <div 
                          className={`pl-6 overflow-hidden transition-all duration-300 ease-in-out ${isOpen ? 'max-h-40 opacity-100' : 'max-h-0 opacity-0'}`}
                        >
                          {dropdownItem.children.map((child: NavDropdownChild) => (
                            <SidebarMenuItem key={child.name}>
                              <SidebarMenuButton 
                                asChild 
                                isActive={location.pathname === child.href}
                                tooltip={child.name}
                              >
                                <Link to={child.href} className="flex items-center w-full">
                                  <div className="flex-shrink-0 w-5 flex justify-center">
                                    {child.icon}
                                  </div>
                                  <span className="ml-3">{child.name}</span>
                                </Link>
                              </SidebarMenuButton>
                            </SidebarMenuItem>
                          ))}
                        </div>
                      )}
                    </div>
                  );
                }
                
                // This is a fallback case that should not be reached
                // since all items are now dropdown items
                const regularItem = item as NavItem;
                return (
                  <SidebarMenuItem key={regularItem.name}>
                    <SidebarMenuButton 
                      asChild 
                      isActive={location.pathname === regularItem.href}
                      tooltip={regularItem.name}
                    >
                      <Link to={regularItem.href}>
                        {regularItem.icon}
                        <span>{regularItem.name}</span>
                      </Link>
                    </SidebarMenuButton>
                  </SidebarMenuItem>
                );
              })}
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
