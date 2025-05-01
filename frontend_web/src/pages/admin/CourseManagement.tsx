import { useState, useEffect } from 'react';
import DashboardLayout from '../../components/ui/layout/DashboardLayout';
import { courseApi, type Course, type CourseCreateDto, type CourseUpdateDto } from '../../lib/api/course';
import { Button } from '../../components/ui/button';
import { Input } from '../../components/ui/input';
import { Label } from '../../components/ui/label';
import { Textarea } from '../../components/ui/textarea';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '../../components/ui/card';
import { Alert, AlertDescription, AlertTitle } from '../../components/ui/alert';
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '../../components/ui/table';
import { Badge } from '../../components/ui/badge';
import { Plus, Pencil, Trash2, BookOpen, AlertTriangle, X, FileText, Code, Bookmark } from 'lucide-react';

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
        <div className="flex h-64 items-center justify-center">
          <div className="flex flex-col items-center space-y-4 text-center">
            <div className="animate-spin text-[#215f47]">
              <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                <path d="M21 12a9 9 0 1 1-6.219-8.56"/>
              </svg>
            </div>
            <p className="text-lg font-medium text-gray-700">Loading courses...</p>
          </div>
        </div>
      </DashboardLayout>
    );
  }

  return (
    <DashboardLayout>
      <div className="space-y-6 p-6">
        <div className="flex flex-col justify-between sm:flex-row sm:items-center border-b border-[#215f47]/10 pb-4">
          <div>
            <h2 className="text-2xl font-bold text-[#215f47] flex items-center gap-2">
              <BookOpen className="h-6 w-6" />
              Course Management
            </h2>
            <p className="text-gray-500 mt-1">Create and manage course offerings</p>
          </div>
          
          <div className="mt-4 sm:mt-0">
            <Button onClick={handleAddNew} className="bg-[#215f47] hover:bg-[#215f47]/90 flex items-center">
              <Plus className="mr-2 h-4 w-4" />
              Add New Course
            </Button>
          </div>
        </div>
        
        {error && (
          <Alert variant="destructive" className="border-red-500/20 mb-6">
            <AlertTriangle className="h-4 w-4" />
            <AlertTitle>Error</AlertTitle>
            <AlertDescription>{error}</AlertDescription>
          </Alert>
        )}
        
        {showForm && (
          <Card className="border-[#215f47]/20 shadow-sm mb-6">
            <CardHeader className="pb-3">
              <CardTitle className="text-[#215f47] flex items-center gap-2">
                {formType === 'add' ? <Plus className="h-5 w-5" /> : <Pencil className="h-5 w-5" />}
                {formType === 'add' ? 'Add New Course' : 'Edit Course'}
              </CardTitle>
              <CardDescription>
                {formType === 'add' ? 'Create a new course in the system' : 'Modify existing course details'}
              </CardDescription>
            </CardHeader>
            
            <CardContent>
              <form onSubmit={handleSubmit} className="space-y-5">
                <div className="space-y-1.5">
                  <Label htmlFor="courseCode" className="text-[#215f47]">
                    <div className="flex items-center gap-1.5">
                      <Code className="h-3.5 w-3.5" />
                      Course Code
                    </div>
                  </Label>
                  <Input
                    id="courseCode"
                    name="courseCode"
                    value={formData.courseCode}
                    onChange={handleInputChange}
                    required
                    className="border-[#215f47]/20 focus-visible:ring-[#215f47]/20 focus-visible:ring-offset-1 focus-visible:ring-offset-[#215f47]/90"
                    placeholder="e.g., CS101"
                  />
                </div>
                
                <div className="space-y-1.5">
                  <Label htmlFor="courseName" className="text-[#215f47]">
                    <div className="flex items-center gap-1.5">
                      <Bookmark className="h-3.5 w-3.5" />
                      Course Name
                    </div>
                  </Label>
                  <Input
                    id="courseName"
                    name="courseName"
                    value={formData.courseName}
                    onChange={handleInputChange}
                    required
                    className="border-[#215f47]/20 focus-visible:ring-[#215f47]/20 focus-visible:ring-offset-1 focus-visible:ring-offset-[#215f47]/90"
                    placeholder="e.g., Introduction to Computer Science"
                  />
                </div>
                
                <div className="space-y-1.5">
                  <Label htmlFor="courseDescription" className="text-[#215f47]">
                    <div className="flex items-center gap-1.5">
                      <FileText className="h-3.5 w-3.5" />
                      Course Description
                    </div>
                  </Label>
                  <Textarea
                    id="courseDescription"
                    name="courseDescription"
                    value={formData.courseDescription}
                    onChange={handleInputChange}
                    rows={4}
                    className="border-[#215f47]/20 focus-visible:ring-[#215f47]/20 focus-visible:ring-offset-1 focus-visible:ring-offset-[#215f47]/90 resize-none"
                    placeholder="Provide a description of the course"
                  />
                </div>
              
                <div className="flex justify-end space-x-3 pt-2">
                  <Button 
                    variant="outline" 
                    type="button" 
                    onClick={resetForm}
                    className="border-[#215f47]/20 text-[#215f47] hover:bg-[#215f47]/5 hover:text-[#215f47] flex items-center"
                  >
                    <X className="mr-2 h-4 w-4" />
                    Cancel
                  </Button>
                  <Button 
                    type="submit"
                    className="bg-[#215f47] hover:bg-[#215f47]/90 flex items-center"
                  >
                    {formType === 'add' ? (
                      <>
                        <Plus className="mr-2 h-4 w-4" />
                        Create Course
                      </>
                    ) : (
                      <>
                        <Pencil className="mr-2 h-4 w-4" />
                        Update Course
                      </>
                    )}
                  </Button>
                </div>
              </form>
            </CardContent>
          </Card>
        )}
        
        <Card className="border-[#215f47]/20 shadow-sm">
          <CardHeader className="pb-3">
            <CardTitle className="text-base text-[#215f47]">Course List</CardTitle>
            <CardDescription>All available courses in the system</CardDescription>
          </CardHeader>

          <CardContent>
            {courses.length === 0 ? (
              <div className="flex flex-col items-center justify-center py-12 text-center">
                <AlertTriangle className="h-12 w-12 text-amber-500/70 mb-4" />
                <h3 className="text-lg font-medium text-gray-700 mb-1">No Courses Found</h3>
                <p className="text-gray-500 max-w-sm mb-6">
                  There are no courses in the system yet. Click the "Add New Course" button to create one.
                </p>
                <Button 
                  onClick={handleAddNew} 
                  className="bg-[#215f47] hover:bg-[#215f47]/90 flex items-center"
                >
                  <Plus className="mr-2 h-4 w-4" />
                  Add New Course
                </Button>
              </div>
            ) : (
              <div className="overflow-x-auto">
                <Table>
                  <TableHeader className="bg-[#215f47]/5">
                    <TableRow>
                      <TableHead className="text-[#215f47] font-medium w-[15%]">
                        Course Code
                      </TableHead>
                      <TableHead className="text-[#215f47] font-medium w-[25%]">
                        Name
                      </TableHead>
                      <TableHead className="text-[#215f47] font-medium">
                        Description
                      </TableHead>
                      <TableHead className="text-[#215f47] font-medium text-right w-[15%]">
                        Actions
                      </TableHead>
                    </TableRow>
                  </TableHeader>
                  <TableBody>
                    {courses.map((course) => (
                      <TableRow key={course.id} className="hover:bg-[#215f47]/5">
                        <TableCell className="font-medium">
                          <Badge variant="outline" className="bg-[#215f47]/5 text-[#215f47] border-[#215f47]/20 font-mono">
                            {course.courseCode}
                          </Badge>
                        </TableCell>
                        <TableCell className="font-medium">
                          {course.courseName}
                        </TableCell>
                        <TableCell className="text-gray-600">
                          {course.courseDescription.length > 100
                            ? `${course.courseDescription.substring(0, 100)}...`
                            : course.courseDescription || 'No description provided'}
                        </TableCell>
                        <TableCell className="text-right space-x-1">
                          <Button
                            variant="ghost"
                            size="icon"
                            onClick={() => handleEdit(course.id)}
                            className="h-8 w-8 text-[#215f47] hover:bg-[#215f47]/10 hover:text-[#215f47]"
                          >
                            <Pencil className="h-4 w-4" />
                          </Button>
                          <Button
                            variant="ghost"
                            size="icon"
                            onClick={() => handleDelete(course.id)}
                            className="h-8 w-8 text-red-500 hover:bg-red-50 hover:text-red-600"
                          >
                            <Trash2 className="h-4 w-4" />
                          </Button>
                        </TableCell>
                      </TableRow>
                    ))}
                  </TableBody>
                </Table>
              </div>
            )}
          </CardContent>
        </Card>
      </div>
    </DashboardLayout>
  );
};

export default CourseManagement;
