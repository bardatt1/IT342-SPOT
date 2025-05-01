import { useState, useEffect } from 'react';
import DashboardLayout from '../../components/ui/layout/DashboardLayout';
import { courseApi, type Course, type CourseCreateDto, type CourseUpdateDto } from '../../lib/api/course';
import { Button } from '../../components/ui/button';
import { Plus, Pencil, Trash2 } from 'lucide-react';

const CourseManagement = () => {
  const [courses, setCourses] = useState<Course[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  
  // Form state
  const [showForm, setShowForm] = useState(false);
  const [formType, setFormType] = useState<'add' | 'edit'>('add');
  const [selectedCourseId, setSelectedCourseId] = useState<number | null>(null);
  
  const [formData, setFormData] = useState({
    courseName: '',
    courseDescription: '',
    courseCode: ''
  });

  useEffect(() => {
    fetchCourses();
  }, []);

  const fetchCourses = async () => {
    try {
      setIsLoading(true);
      const data = await courseApi.getAll();
      setCourses(data);
    } catch (error) {
      console.error('Error fetching courses:', error);
      setError('Failed to load courses. Please try again later.');
    } finally {
      setIsLoading(false);
    }
  };

  const resetForm = () => {
    setShowForm(false);
    setFormType('add');
    setSelectedCourseId(null);
    setFormData({
      courseName: '',
      courseDescription: '',
      courseCode: ''
    });
  };

  const handleAddNew = () => {
    setShowForm(true);
    setFormType('add');
  };

  const handleEdit = (id: number) => {
    setShowForm(true);
    setFormType('edit');
    setSelectedCourseId(id);
    
    // Populate form data based on selected course
    const course = courses.find(c => c.id === id);
    if (course) {
      setFormData({
        courseName: course.courseName,
        courseDescription: course.courseDescription,
        courseCode: course.courseCode
      });
    }
  };

  const handleDelete = async (id: number) => {
    if (!window.confirm('Are you sure you want to delete this course?')) {
      return;
    }
    
    try {
      await courseApi.delete(id);
      setCourses(courses.filter(c => c.id !== id));
    } catch (error) {
      console.error('Error deleting course:', error);
      setError('Failed to delete course. Please try again later.');
    }
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    
    try {
      if (formType === 'edit' && selectedCourseId) {
        const courseData: CourseUpdateDto = {
          id: selectedCourseId,
          courseName: formData.courseName,
          courseDescription: formData.courseDescription,
          courseCode: formData.courseCode
        };
        
        const updatedCourse = await courseApi.update(courseData);
        setCourses(courses.map(c => c.id === selectedCourseId ? updatedCourse : c));
      } else {
        const courseData: CourseCreateDto = {
          courseName: formData.courseName,
          courseDescription: formData.courseDescription,
          courseCode: formData.courseCode
        };
        
        const newCourse = await courseApi.create(courseData);
        setCourses([...courses, newCourse]);
      }
      
      resetForm();
    } catch (error) {
      console.error(`Error ${formType === 'add' ? 'creating' : 'updating'} course:`, error);
      setError(`Failed to ${formType === 'add' ? 'create' : 'update'} course. Please try again later.`);
    }
  };

  const handleInputChange = (
    e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>
  ) => {
    const { name, value } = e.target;
    setFormData({ ...formData, [name]: value });
  };

  if (isLoading) {
    return (
      <DashboardLayout>
        <div className="flex h-full items-center justify-center">
          <p className="text-lg">Loading courses...</p>
        </div>
      </DashboardLayout>
    );
  }

  return (
    <DashboardLayout>
      <div className="space-y-6">
        <div className="flex flex-col justify-between sm:flex-row sm:items-center">
          <h2 className="text-xl font-semibold">Course Management</h2>
          
          <div className="mt-4 sm:mt-0">
            <Button onClick={handleAddNew} className="flex items-center">
              <Plus className="mr-1 h-4 w-4" />
              Add New Course
            </Button>
          </div>
        </div>
        
        {error && (
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
        )}
        
        {showForm && (
          <div className="rounded-lg bg-white p-6 shadow">
            <h3 className="mb-4 text-lg font-medium">
              {formType === 'add' ? 'Add New' : 'Edit'} Course
            </h3>
            
            <form onSubmit={handleSubmit} className="space-y-4">
              <div>
                <label htmlFor="courseCode" className="block text-sm font-medium text-gray-700">
                  Course Code
                </label>
                <input
                  type="text"
                  id="courseCode"
                  name="courseCode"
                  value={formData.courseCode}
                  onChange={handleInputChange}
                  required
                  className="mt-1 block w-full rounded-md border border-gray-300 px-3 py-2 shadow-sm focus:border-blue-500 focus:outline-none focus:ring-blue-500 sm:text-sm"
                  placeholder="e.g., CS101"
                />
              </div>
              
              <div>
                <label htmlFor="courseName" className="block text-sm font-medium text-gray-700">
                  Course Name
                </label>
                <input
                  type="text"
                  id="courseName"
                  name="courseName"
                  value={formData.courseName}
                  onChange={handleInputChange}
                  required
                  className="mt-1 block w-full rounded-md border border-gray-300 px-3 py-2 shadow-sm focus:border-blue-500 focus:outline-none focus:ring-blue-500 sm:text-sm"
                  placeholder="e.g., Introduction to Computer Science"
                />
              </div>
              
              <div>
                <label htmlFor="courseDescription" className="block text-sm font-medium text-gray-700">
                  Course Description
                </label>
                <textarea
                  id="courseDescription"
                  name="courseDescription"
                  value={formData.courseDescription}
                  onChange={handleInputChange}
                  rows={4}
                  className="mt-1 block w-full rounded-md border border-gray-300 px-3 py-2 shadow-sm focus:border-blue-500 focus:outline-none focus:ring-blue-500 sm:text-sm"
                  placeholder="Provide a description of the course"
                />
              </div>
              
              <div className="flex justify-end space-x-3 pt-4">
                <Button variant="outline" type="button" onClick={resetForm}>
                  Cancel
                </Button>
                <Button type="submit">
                  {formType === 'add' ? 'Create' : 'Update'}
                </Button>
              </div>
            </form>
          </div>
        )}
        
        <div className="overflow-hidden rounded-lg bg-white shadow">
          {courses.length === 0 ? (
            <div className="p-6 text-center text-gray-500">
              No courses found. Click "Add New Course" to create one.
            </div>
          ) : (
            <div className="overflow-x-auto">
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
                    <th className="px-6 py-3 text-right text-xs font-medium uppercase tracking-wider text-gray-500">
                      Actions
                    </th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-gray-200 bg-white">
                  {courses.map((course) => (
                    <tr key={course.id}>
                      <td className="whitespace-nowrap px-6 py-4 text-sm font-medium text-gray-900">
                        {course.courseCode}
                      </td>
                      <td className="whitespace-nowrap px-6 py-4 text-sm text-gray-500">
                        {course.courseName}
                      </td>
                      <td className="px-6 py-4 text-sm text-gray-500">
                        {course.courseDescription.length > 100
                          ? `${course.courseDescription.substring(0, 100)}...`
                          : course.courseDescription}
                      </td>
                      <td className="whitespace-nowrap px-6 py-4 text-right text-sm font-medium">
                        <Button
                          variant="ghost"
                          size="icon"
                          onClick={() => handleEdit(course.id)}
                          className="text-blue-600 hover:text-blue-900"
                        >
                          <Pencil className="h-4 w-4" />
                        </Button>
                        <Button
                          variant="ghost"
                          size="icon"
                          onClick={() => handleDelete(course.id)}
                          className="text-red-600 hover:text-red-900"
                        >
                          <Trash2 className="h-4 w-4" />
                        </Button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </div>
      </div>
    </DashboardLayout>
  );
};

export default CourseManagement;
