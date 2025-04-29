import { useState, useEffect } from 'react';
import DashboardLayout from '../../components/ui/layout/DashboardLayout';
import { courseApi, type Course } from '../../lib/api/course';
import { sectionApi, type Section } from '../../lib/api/section';
import { teacherApi, type Teacher } from '../../lib/api/teacher';
import { studentApi, type Student } from '../../lib/api/student';
import { Users, BookOpen, Calendar } from 'lucide-react';

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
          <p className="text-lg">Loading dashboard data...</p>
        </div>
      </DashboardLayout>
    );
  }

  if (error) {
    return (
      <DashboardLayout>
        <div className="flex h-full items-center justify-center">
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
        </div>
      </DashboardLayout>
    );
  }

  return (
    <DashboardLayout>
      <div className="space-y-6">
        <h2 className="text-xl font-semibold">Admin Dashboard</h2>
        
        <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-4">
          {/* Summary Cards */}
          <div className="rounded-lg bg-white p-6 shadow">
            <div className="flex items-center">
              <div className="flex h-12 w-12 items-center justify-center rounded-lg bg-blue-100">
                <BookOpen className="h-6 w-6 text-blue-600" />
              </div>
              <div className="ml-4">
                <h3 className="text-sm font-medium text-gray-500">Total Courses</h3>
                <p className="text-2xl font-semibold text-gray-900">{courses.length}</p>
              </div>
            </div>
          </div>
          
          <div className="rounded-lg bg-white p-6 shadow">
            <div className="flex items-center">
              <div className="flex h-12 w-12 items-center justify-center rounded-lg bg-green-100">
                <Calendar className="h-6 w-6 text-green-600" />
              </div>
              <div className="ml-4">
                <h3 className="text-sm font-medium text-gray-500">Total Sections</h3>
                <p className="text-2xl font-semibold text-gray-900">{sections.length}</p>
              </div>
            </div>
          </div>
          
          <div className="rounded-lg bg-white p-6 shadow">
            <div className="flex items-center">
              <div className="flex h-12 w-12 items-center justify-center rounded-lg bg-purple-100">
                <Users className="h-6 w-6 text-purple-600" />
              </div>
              <div className="ml-4">
                <h3 className="text-sm font-medium text-gray-500">Teachers</h3>
                <p className="text-2xl font-semibold text-gray-900">{teachers.length}</p>
              </div>
            </div>
          </div>
          
          <div className="rounded-lg bg-white p-6 shadow">
            <div className="flex items-center">
              <div className="flex h-12 w-12 items-center justify-center rounded-lg bg-yellow-100">
                <Users className="h-6 w-6 text-yellow-600" />
              </div>
              <div className="ml-4">
                <h3 className="text-sm font-medium text-gray-500">Students</h3>
                <p className="text-2xl font-semibold text-gray-900">{students.length}</p>
              </div>
            </div>
          </div>
        </div>
        
        {/* Recent Courses */}
        <div className="rounded-lg bg-white p-6 shadow">
          <h3 className="font-medium text-gray-900">Recent Courses</h3>
          <div className="mt-4 overflow-x-auto">
            <table className="min-w-full divide-y divide-gray-200">
              <thead className="bg-gray-50">
                <tr>
                  <th className="px-6 py-3 text-left text-xs font-medium uppercase tracking-wider text-gray-500">
                    Course Code
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium uppercase tracking-wider text-gray-500">
                    Name
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium uppercase tracking-wider text-gray-500">
                    Description
                  </th>
                </tr>
              </thead>
              <tbody className="divide-y divide-gray-200 bg-white">
                {courses.slice(0, 5).map((course) => (
                  <tr key={course.id}>
                    <td className="whitespace-nowrap px-6 py-4 text-sm font-medium text-gray-900">
                      {course.courseCode}
                    </td>
                    <td className="whitespace-nowrap px-6 py-4 text-sm text-gray-500">
                      {course.courseName}
                    </td>
                    <td className="px-6 py-4 text-sm text-gray-500">
                      {course.courseDescription.length > 50
                        ? `${course.courseDescription.substring(0, 50)}...`
                        : course.courseDescription}
                    </td>
                  </tr>
                ))}
                {courses.length === 0 && (
                  <tr>
                    <td colSpan={3} className="px-6 py-4 text-center text-sm text-gray-500">
                      No courses found
                    </td>
                  </tr>
                )}
              </tbody>
            </table>
          </div>
        </div>
      </div>
    </DashboardLayout>
  );
};

export default AdminDashboard;
