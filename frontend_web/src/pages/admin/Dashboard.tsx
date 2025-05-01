import { useState, useEffect } from 'react';
import DashboardLayout from '../../components/ui/layout/DashboardLayout';
import { courseApi, type Course } from '../../lib/api/course';
import { sectionApi, type Section } from '../../lib/api/section';
import { teacherApi, type Teacher } from '../../lib/api/teacher';
import { studentApi, type Student } from '../../lib/api/student';
import { Users, BookOpen, Calendar, AlertTriangle, UserCog } from 'lucide-react';
import { Card, CardContent, CardHeader, CardTitle } from '../../components/ui/card';
import { Badge } from '../../components/ui/badge';
import { Alert, AlertDescription } from '../../components/ui/alert';
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '../../components/ui/table';

const AdminDashboard = () => {
  const [courses, setCourses] = useState<Course[]>([]);
  const [sections, setSections] = useState<Section[]>([]);
  const [teachers, setTeachers] = useState<Teacher[]>([]);
  const [students, setStudents] = useState<Student[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

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
          // Still continue with other data
        }
        
        try {
          const sectionsData = await sectionApi.getAllSections();
          setSections(sectionsData || []);
        } catch (sectionError) {
          console.error('Error fetching sections:', sectionError);
          // Still continue with other data
        }
        
        try {
          const teachersData = await teacherApi.getAll();
          setTeachers(teachersData || []);
        } catch (teacherError) {
          console.error('Error fetching teachers:', teacherError);
          // Still continue with other data
        }
        
        try {
          const studentsData = await studentApi.getAll();
          setStudents(studentsData || []);
        } catch (studentError) {
          console.error('Error fetching students:', studentError);
          // Still continue with other data
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
              <UserCog className="h-6 w-6" />
              Admin Dashboard
            </h2>
            <p className="text-sm text-gray-500 mt-1">Overview of system data and metrics</p>
          </div>
          <Badge variant="outline" className="bg-[#215f47]/5 text-[#215f47] px-3 py-1">
            Admin View
          </Badge>
        </div>
    
        <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-4">
          {/* Summary Cards */}
          <Card className="border-[#215f47]/20 shadow-sm hover:shadow transition-shadow duration-200">
            <CardContent className="p-6">
              <div className="flex items-center">
                <div className="flex h-12 w-12 items-center justify-center rounded-full bg-[#215f47]/10">
                  <BookOpen className="h-6 w-6 text-[#215f47]" />
                </div>
                <div className="ml-4">
                  <p className="text-sm font-medium text-gray-500">Total Courses</p>
                  <h3 className="text-2xl font-bold text-[#215f47]">{courses.length}</h3>
                </div>
              </div>
            </CardContent>
          </Card>
          
          <Card className="border-[#215f47]/20 shadow-sm hover:shadow transition-shadow duration-200">
            <CardContent className="p-6">
              <div className="flex items-center">
                <div className="flex h-12 w-12 items-center justify-center rounded-full bg-[#215f47]/10">
                  <Calendar className="h-6 w-6 text-[#215f47]" />
                </div>
                <div className="ml-4">
                  <p className="text-sm font-medium text-gray-500">Total Sections</p>
                  <h3 className="text-2xl font-bold text-[#215f47]">{sections.length}</h3>
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
                  <Users className="h-6 w-6 text-[#215f47]" />
                </div>
                <div className="ml-4">
                  <p className="text-sm font-medium text-gray-500">Students</p>
                  <h3 className="text-2xl font-bold text-[#215f47]">{students.length}</h3>
                </div>
              </div>
            </CardContent>
          </Card>
        </div>
        
        {/* Recent Courses */}
        <Card className="border-[#215f47]/20 shadow-sm">
          <CardHeader className="px-6 pb-2 pt-6">
            <CardTitle className="text-lg font-medium text-[#215f47]">Recent Courses</CardTitle>
          </CardHeader>
          <CardContent className="px-6 pb-6">
            <div className="rounded-md overflow-hidden border border-[#215f47]/20">
              <Table>
                <TableHeader className="bg-[#215f47]/5">
                  <TableRow>
                    <TableHead className="text-[#215f47] font-medium">Course Code</TableHead>
                    <TableHead className="text-[#215f47] font-medium">Name</TableHead>
                    <TableHead className="text-[#215f47] font-medium">Description</TableHead>
                  </TableRow>
                </TableHeader>
                <TableBody>
                  {courses.slice(0, 5).map((course) => (
                    <TableRow key={course.id} className="hover:bg-[#215f47]/5 transition-colors">
                      <TableCell className="font-medium">{course.courseCode}</TableCell>
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
                      <TableCell colSpan={3} className="h-24 text-center text-muted-foreground">
                        No courses found
                      </TableCell>
                    </TableRow>
                  )}
                </TableBody>
              </Table>
            </div>
          </CardContent>
        </Card>
      </div>
    </DashboardLayout>
  );
};

export default AdminDashboard;
