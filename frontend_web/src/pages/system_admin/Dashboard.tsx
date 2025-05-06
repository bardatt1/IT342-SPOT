import { useState, useEffect } from 'react';
import DashboardLayout from '../../components/ui/layout/DashboardLayout';
import { courseApi, type Course } from '../../lib/api/course';
import { sectionApi } from '../../lib/api/section';
import { teacherApi, type Teacher } from '../../lib/api/teacher';
import { studentApi, type Student } from '../../lib/api/student';
import { adminApi, type Admin } from '../../lib/api/admin';
import { Shield, Users, BookOpen, AlertTriangle, UserCog, School, UserPlus } from 'lucide-react';
import { Card, CardContent, CardHeader, CardTitle } from '../../components/ui/card';
import { Badge } from '../../components/ui/badge';
import { Alert, AlertDescription } from '../../components/ui/alert';
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '../../components/ui/table';
import { Button } from '../../components/ui/button';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '../../components/ui/tabs';
import { useNavigate } from 'react-router-dom';

const SystemAdminDashboard = () => {
  const [courses, setCourses] = useState<Course[]>([]);
  const [teachers, setTeachers] = useState<Teacher[]>([]);
  const [students, setStudents] = useState<Student[]>([]);
  const [admins, setAdmins] = useState<Admin[]>([]);
  const [systemAdmins, setSystemAdmins] = useState<Admin[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [activeTab, setActiveTab] = useState('overview');
  const navigate = useNavigate();

  useEffect(() => {
    const fetchDashboardData = async () => {
      try {
        setIsLoading(true);
        setError(null);
        
        // Fetch each data source independently to avoid one failure affecting everything
        try {
          const coursesData = await courseApi.getAll();
          setCourses(coursesData || []);
        } catch (courseError) {
          console.error('Error fetching courses:', courseError);
        }
        
        try {
          await sectionApi.getAllSections();
          // We don't need sections data for the system admin dashboard
        } catch (sectionError) {
          console.error('Error fetching sections:', sectionError);
        }
        
        try {
          const teachersData = await teacherApi.getAll();
          setTeachers(teachersData || []);
        } catch (teacherError) {
          console.error('Error fetching teachers:', teacherError);
        }
        
        try {
          const studentsData = await studentApi.getAll();
          setStudents(studentsData || []);
        } catch (studentError) {
          console.error('Error fetching students:', studentError);
        }
        
        try {
          // Assume these endpoints will be available
          const adminsData = await adminApi.getAll();
          setAdmins(adminsData || []);
          
          const systemAdminsData = await adminApi.getAllSystemAdmins();
          setSystemAdmins(systemAdminsData || []);
        } catch (adminError) {
          console.error('Error fetching admins:', adminError);
        }
      } catch (error) {
        console.error('Error in dashboard data fetching:', error);
        setError('Some data could not be loaded. Please check backend connectivity.');
      } finally {
        setIsLoading(false);
      }
    };

    fetchDashboardData();
  }, []);

  if (isLoading) {
    return (
      <DashboardLayout>
        <div className="flex h-full items-center justify-center">
          <div className="flex items-center space-x-2">
            <div className="h-4 w-4 animate-spin rounded-full border-2 border-[#215f47] border-t-transparent"></div>
            <p className="text-lg font-medium text-[#215f47]">Loading dashboard data...</p>
          </div>
        </div>
      </DashboardLayout>
    );
  }

  if (error) {
    return (
      <DashboardLayout>
        <div className="flex h-full items-center justify-center p-6">
          <Alert variant="destructive" className="border-red-300 bg-red-50">
            <AlertTriangle className="h-5 w-5 text-red-600" />
            <AlertDescription className="text-red-700">{error}</AlertDescription>
          </Alert>
        </div>
      </DashboardLayout>
    );
  }

  return (
    <DashboardLayout>
      <div className="space-y-6 p-6">
        <div className="flex items-center justify-between">
          <div>
            <h2 className="text-2xl font-bold text-[#215f47] flex items-center gap-2">
              <Shield className="h-6 w-6" />
              System Admin Dashboard
            </h2>
            <p className="text-sm text-gray-500 mt-1">Comprehensive system overview and management</p>
          </div>
          <div className="flex items-center gap-2">
            <Button 
              onClick={() => navigate('/system-admin/admin-management')} 
              className="bg-[#215f47] hover:bg-[#215f47]/90 text-white gap-2 py-2"
            >
              <UserPlus className="h-4 w-4" />
              Manage Admins
            </Button>
            <Badge variant="outline" className="bg-red-50 text-red-700 border-red-200 px-3 py-1">
              System Admin
            </Badge>
          </div>
        </div>
        
        <Tabs value={activeTab} onValueChange={setActiveTab} className="w-full">
          <TabsList className="grid grid-cols-5 mb-4">
            <TabsTrigger value="overview">Overview</TabsTrigger>
            <TabsTrigger value="admins">Admins</TabsTrigger>
            <TabsTrigger value="teachers">Teachers</TabsTrigger>
            <TabsTrigger value="students">Students</TabsTrigger>
            <TabsTrigger value="courses">Courses</TabsTrigger>
          </TabsList>
          
          <TabsContent value="overview" className="mt-4">
            <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-5">
              {/* Summary Cards */}
              <Card className="border-[#215f47]/20 shadow-sm hover:shadow transition-shadow duration-200">
                <CardContent className="p-6">
                  <div className="flex items-center">
                    <div className="flex h-12 w-12 items-center justify-center rounded-full bg-red-100">
                      <Shield className="h-6 w-6 text-red-700" />
                    </div>
                    <div className="ml-4">
                      <p className="text-sm font-medium text-gray-500">System Admins</p>
                      <h3 className="text-2xl font-bold text-[#215f47]">{systemAdmins.length}</h3>
                    </div>
                  </div>
                </CardContent>
              </Card>
              
              <Card className="border-[#215f47]/20 shadow-sm hover:shadow transition-shadow duration-200">
                <CardContent className="p-6">
                  <div className="flex items-center">
                    <div className="flex h-12 w-12 items-center justify-center rounded-full bg-[#215f47]/10">
                      <UserCog className="h-6 w-6 text-[#215f47]" />
                    </div>
                    <div className="ml-4">
                      <p className="text-sm font-medium text-gray-500">Admins</p>
                      <h3 className="text-2xl font-bold text-[#215f47]">{admins.length}</h3>
                    </div>
                  </div>
                </CardContent>
              </Card>
              
              <Card className="border-[#215f47]/20 shadow-sm hover:shadow transition-shadow duration-200">
                <CardContent className="p-6">
                  <div className="flex items-center">
                    <div className="flex h-12 w-12 items-center justify-center rounded-full bg-[#215f47]/10">
                      <Users className="h-6 w-6 text-[#215f47]" />
                    </div>
                    <div className="ml-4">
                      <p className="text-sm font-medium text-gray-500">Teachers</p>
                      <h3 className="text-2xl font-bold text-[#215f47]">{teachers.length}</h3>
                    </div>
                  </div>
                </CardContent>
              </Card>
              
              <Card className="border-[#215f47]/20 shadow-sm hover:shadow transition-shadow duration-200">
                <CardContent className="p-6">
                  <div className="flex items-center">
                    <div className="flex h-12 w-12 items-center justify-center rounded-full bg-[#215f47]/10">
                      <School className="h-6 w-6 text-[#215f47]" />
                    </div>
                    <div className="ml-4">
                      <p className="text-sm font-medium text-gray-500">Students</p>
                      <h3 className="text-2xl font-bold text-[#215f47]">{students.length}</h3>
                    </div>
                  </div>
                </CardContent>
              </Card>
              
              <Card className="border-[#215f47]/20 shadow-sm hover:shadow transition-shadow duration-200">
                <CardContent className="p-6">
                  <div className="flex items-center">
                    <div className="flex h-12 w-12 items-center justify-center rounded-full bg-[#215f47]/10">
                      <BookOpen className="h-6 w-6 text-[#215f47]" />
                    </div>
                    <div className="ml-4">
                      <p className="text-sm font-medium text-gray-500">Courses</p>
                      <h3 className="text-2xl font-bold text-[#215f47]">{courses.length}</h3>
                    </div>
                  </div>
                </CardContent>
              </Card>
            </div>
          </TabsContent>
          
          <TabsContent value="admins">
            <Card className="border-[#215f47]/20 shadow-sm">
              <CardHeader className="px-6 pb-2 pt-6">
                <CardTitle className="text-lg font-medium text-[#215f47]">All Administrators</CardTitle>
              </CardHeader>
              <CardContent className="px-6 pb-6">
                <div className="rounded-md overflow-hidden border border-[#215f47]/20">
                  <Table>
                    <TableHeader className="bg-[#215f47]/5">
                      <TableRow>
                        <TableHead className="text-[#215f47] font-medium">ID</TableHead>
                        <TableHead className="text-[#215f47] font-medium">Name</TableHead>
                        <TableHead className="text-[#215f47] font-medium">Email</TableHead>
                        <TableHead className="text-[#215f47] font-medium">Role</TableHead>
                      </TableRow>
                    </TableHeader>
                    <TableBody>
                      {admins.map((admin) => (
                        <TableRow key={admin.id} className="hover:bg-[#215f47]/5 transition-colors">
                          <TableCell className="font-medium">{admin.id}</TableCell>
                          <TableCell>{`${admin.firstName} ${admin.lastName}`}</TableCell>
                          <TableCell>{admin.email}</TableCell>
                          <TableCell>
                            {admin.systemAdmin ? (
                              <Badge className="bg-red-50 text-red-700 border-red-200">
                                System Admin
                              </Badge>
                            ) : (
                              <Badge className="bg-[#215f47]/10 text-[#215f47]">
                                Admin
                              </Badge>
                            )}
                          </TableCell>
                        </TableRow>
                      ))}
                      {admins.length === 0 && (
                        <TableRow>
                          <TableCell colSpan={4} className="h-24 text-center text-muted-foreground">
                            No administrators found
                          </TableCell>
                        </TableRow>
                      )}
                    </TableBody>
                  </Table>
                </div>
              </CardContent>
            </Card>
          </TabsContent>
          
          <TabsContent value="teachers">
            <Card className="border-[#215f47]/20 shadow-sm">
              <CardHeader className="px-6 pb-2 pt-6">
                <CardTitle className="text-lg font-medium text-[#215f47]">All Teachers</CardTitle>
              </CardHeader>
              <CardContent className="px-6 pb-6">
                <div className="rounded-md overflow-hidden border border-[#215f47]/20">
                  <Table>
                    <TableHeader className="bg-[#215f47]/5">
                      <TableRow>
                        <TableHead className="text-[#215f47] font-medium">ID</TableHead>
                        <TableHead className="text-[#215f47] font-medium">Name</TableHead>
                        <TableHead className="text-[#215f47] font-medium">Email</TableHead>
                        <TableHead className="text-[#215f47] font-medium">Teacher ID</TableHead>
                      </TableRow>
                    </TableHeader>
                    <TableBody>
                      {teachers.map((teacher) => (
                        <TableRow key={teacher.id} className="hover:bg-[#215f47]/5 transition-colors">
                          <TableCell className="font-medium">{teacher.id}</TableCell>
                          <TableCell>{`${teacher.firstName} ${teacher.lastName}`}</TableCell>
                          <TableCell>{teacher.email}</TableCell>
                          <TableCell>{teacher.teacherPhysicalId || 'Not assigned'}</TableCell>
                        </TableRow>
                      ))}
                      {teachers.length === 0 && (
                        <TableRow>
                          <TableCell colSpan={4} className="h-24 text-center text-muted-foreground">
                            No teachers found
                          </TableCell>
                        </TableRow>
                      )}
                    </TableBody>
                  </Table>
                </div>
              </CardContent>
            </Card>
          </TabsContent>
          
          <TabsContent value="students">
            <Card className="border-[#215f47]/20 shadow-sm">
              <CardHeader className="px-6 pb-2 pt-6">
                <CardTitle className="text-lg font-medium text-[#215f47]">All Students</CardTitle>
              </CardHeader>
              <CardContent className="px-6 pb-6">
                <div className="rounded-md overflow-hidden border border-[#215f47]/20">
                  <Table>
                    <TableHeader className="bg-[#215f47]/5">
                      <TableRow>
                        <TableHead className="text-[#215f47] font-medium">ID</TableHead>
                        <TableHead className="text-[#215f47] font-medium">Name</TableHead>
                        <TableHead className="text-[#215f47] font-medium">Email</TableHead>
                        <TableHead className="text-[#215f47] font-medium">Program</TableHead>
                        <TableHead className="text-[#215f47] font-medium">Student ID</TableHead>
                      </TableRow>
                    </TableHeader>
                    <TableBody>
                      {students.map((student) => (
                        <TableRow key={student.id} className="hover:bg-[#215f47]/5 transition-colors">
                          <TableCell className="font-medium">{student.id}</TableCell>
                          <TableCell>{`${student.firstName} ${student.lastName}`}</TableCell>
                          <TableCell>{student.email}</TableCell>
                          <TableCell>{student.program}</TableCell>
                          <TableCell>{student.studentPhysicalId || 'Not assigned'}</TableCell>
                        </TableRow>
                      ))}
                      {students.length === 0 && (
                        <TableRow>
                          <TableCell colSpan={5} className="h-24 text-center text-muted-foreground">
                            No students found
                          </TableCell>
                        </TableRow>
                      )}
                    </TableBody>
                  </Table>
                </div>
              </CardContent>
            </Card>
          </TabsContent>
          
          <TabsContent value="courses">
            <Card className="border-[#215f47]/20 shadow-sm">
              <CardHeader className="px-6 pb-2 pt-6">
                <CardTitle className="text-lg font-medium text-[#215f47]">All Courses</CardTitle>
              </CardHeader>
              <CardContent className="px-6 pb-6">
                <div className="rounded-md overflow-hidden border border-[#215f47]/20">
                  <Table>
                    <TableHeader className="bg-[#215f47]/5">
                      <TableRow>
                        <TableHead className="text-[#215f47] font-medium">ID</TableHead>
                        <TableHead className="text-[#215f47] font-medium">Course Code</TableHead>
                        <TableHead className="text-[#215f47] font-medium">Name</TableHead>
                        <TableHead className="text-[#215f47] font-medium">Description</TableHead>
                      </TableRow>
                    </TableHeader>
                    <TableBody>
                      {courses.map((course) => (
                        <TableRow key={course.id} className="hover:bg-[#215f47]/5 transition-colors">
                          <TableCell className="font-medium">{course.id}</TableCell>
                          <TableCell>{course.courseCode}</TableCell>
                          <TableCell>{course.courseName}</TableCell>
                          <TableCell className="max-w-md">
                            {course.courseDescription.length > 50
                              ? `${course.courseDescription.substring(0, 50)}...`
                              : course.courseDescription}
                          </TableCell>
                        </TableRow>
                      ))}
                      {courses.length === 0 && (
                        <TableRow>
                          <TableCell colSpan={4} className="h-24 text-center text-muted-foreground">
                            No courses found
                          </TableCell>
                        </TableRow>
                      )}
                    </TableBody>
                  </Table>
                </div>
              </CardContent>
            </Card>
          </TabsContent>
        </Tabs>
      </div>
    </DashboardLayout>
  );
};

export default SystemAdminDashboard;