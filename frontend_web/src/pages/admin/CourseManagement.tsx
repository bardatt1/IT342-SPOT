import { useState, useEffect } from 'react';
import DashboardLayout from '../../components/ui/layout/DashboardLayout';
import { courseApi, type Course, type CourseCreateDto, type CourseUpdateDto } from '../../lib/api/course';
import { sectionApi, type Section } from '../../lib/api/section';
import { scheduleApi, type Schedule } from '../../lib/api/schedule';
import { Button } from '../../components/ui/button';
import { Input } from '../../components/ui/input';
import { Label } from '../../components/ui/label';
import { Textarea } from '../../components/ui/textarea';
import { Card, CardContent, CardDescription, CardHeader, CardTitle} from '../../components/ui/card';
import { Alert, AlertDescription, AlertTitle } from '../../components/ui/alert';
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '../../components/ui/table';
import { Badge } from '../../components/ui/badge';

import AppModal from '../../components/ui/modal/AppModal';
import { Plus, Pencil, Trash2, BookOpen, AlertTriangle, X, FileText, Code, Bookmark, Calendar, Clock, ChevronRight, Layers, Building2 } from 'lucide-react';

const CourseManagement = () => {
  const [courses, setCourses] = useState<Course[]>([]);
  const [sections, setSections] = useState<Section[]>([]);
  const [schedules, setSchedules] = useState<Schedule[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  
  // Form state
  const [showForm, setShowForm] = useState(false);
  const [formType, setFormType] = useState<'add' | 'edit'>('add');
  const [selectedCourseId, setSelectedCourseId] = useState<number | null>(null);
  
  // Modal states for the section-schedule flow
  const [selectedCourse, setSelectedCourse] = useState<Course | null>(null);
  const [isSectionModalOpen, setIsSectionModalOpen] = useState(false);
  
  const [selectedSection, setSelectedSection] = useState<Section | null>(null);
  const [isScheduleModalOpen, setIsScheduleModalOpen] = useState(false);
  
  const [formData, setFormData] = useState({
    courseName: '',
    courseDescription: '',
    courseCode: ''
  });

  useEffect(() => {
    const fetchData = async () => {
      try {
        setIsLoading(true);
        setError(null);
        
        // Fetch courses
        const coursesData = await courseApi.getAll();
        setCourses(coursesData);
        
        // Fetch sections
        const sectionsData = await sectionApi.getAll();
        setSections(sectionsData);
        
        // We'll fetch schedules only when a specific section is selected
      } catch (err) {
        setError('Failed to fetch data. Please try again later.');
        console.error('Error fetching data:', err);
      } finally {
        setIsLoading(false);
      }
    };

    fetchData();
  }, []);

  // This function is no longer needed as we're using the section.enrollmentCount property directly
  
  // Function to fetch schedules for a specific section
  const fetchSchedulesForSection = async (sectionId: number) => {
    try {
      const data = await scheduleApi.getBySectionId(sectionId);
      setSchedules(data);
      return data;
    } catch (err) {
      console.error('Error fetching schedules for section:', err);
      return [];
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

  // Function to handle opening the sections modal when a course is clicked
  const handleCourseClick = (course: Course) => {
    setSelectedCourse(course);
    setIsSectionModalOpen(true);
  };

  // Function to handle opening the schedules modal when a section is clicked
  const handleSectionClick = async (section: Section) => {
    setSelectedSection(section);
    // Fetch schedules for this section
    await fetchSchedulesForSection(section.id);
    setIsScheduleModalOpen(true);
  };

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
              <div className="text-center py-8 border border-dashed border-[#215f47]/20 rounded-md bg-[#215f47]/5">
                <BookOpen className="w-12 h-12 text-[#215f47]/40 mx-auto mb-3" />
                <p className="text-sm text-gray-600">No courses found</p>
                <p className="text-xs text-gray-500 mt-1">Click "Add New Course" to create one</p>
              </div>
            ) : (
              <div className="rounded-md overflow-hidden border border-[#215f47]/20">
                <Table>
                  <TableHeader className="bg-[#215f47]/5">
                    <TableRow>
                      <TableHead className="text-[#215f47] font-medium">Code</TableHead>
                      <TableHead className="text-[#215f47] font-medium">Course Name</TableHead>
                      <TableHead className="text-[#215f47] font-medium hidden md:table-cell">Description</TableHead>
                      <TableHead className="text-[#215f47] font-medium text-right">Actions</TableHead>
                    </TableRow>
                  </TableHeader>
                  <TableBody>
                    {courses.map(course => (
                      <TableRow 
                        key={course.id} 
                        className="hover:bg-[#215f47]/5 transition-colors cursor-pointer"
                        onClick={() => handleCourseClick(course)}
                      >
                        <TableCell>
                          <Badge variant="outline" className="bg-[#215f47]/5 border-[#215f47]/20 text-[#215f47] font-medium">
                            {course.courseCode}
                          </Badge>
                        </TableCell>
                        <TableCell className="font-medium text-gray-800">{course.courseName}</TableCell>
                        <TableCell className="text-sm text-gray-500 hidden md:table-cell">
                          {course.courseDescription.length > 80
                            ? `${course.courseDescription.substring(0, 80)}...`
                            : course.courseDescription}
                        </TableCell>
                        <TableCell className="text-right">
                          <Button
                            variant="ghost"
                            size="sm"
                            onClick={(e) => {
                              e.stopPropagation(); // Prevent opening the modal
                              handleEdit(course.id);
                            }}
                            className="h-8 w-8 p-0 text-[#215f47] hover:bg-[#215f47]/5"
                          >
                            <Pencil className="h-4 w-4" />
                          </Button>
                          <Button
                            variant="ghost"
                            size="sm"
                            onClick={(e) => {
                              e.stopPropagation(); // Prevent opening the modal
                              handleDelete(course.id);
                            }}
                            className="h-8 w-8 p-0 text-red-500 hover:text-red-700 hover:bg-red-50"
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

      {/* Section Modal */}
      <AppModal
        isOpen={isSectionModalOpen}
        onClose={() => setIsSectionModalOpen(false)}
        title={`Sections for ${selectedCourse?.courseName || 'Course'}`}
        description="View and manage sections for this course"
        size="lg"
      >
        <div className="space-y-4">
          <div className="flex justify-between items-center">
            <div>
              <h3 className="text-lg font-medium text-[#215f47] flex items-center gap-2">
                <Badge variant="outline" className="bg-[#215f47]/5 border-[#215f47]/20 text-[#215f47] font-medium">
                  {selectedCourse?.courseCode}
                </Badge>
                <span>{selectedCourse?.courseName}</span>
              </h3>
              <p className="text-sm text-gray-500 mt-1">{selectedCourse?.courseDescription}</p>
            </div>
            <Button
              onClick={() => {
                // Here you could navigate to section management with course ID pre-selected
                // or implement an add section modal
                setIsSectionModalOpen(false);
              }}
              className="bg-[#215f47] hover:bg-[#215f47]/90 flex items-center"
            >
              <Plus className="mr-2 h-4 w-4" />
              Add Section
            </Button>
          </div>

          {selectedCourse && (
            <div className="border rounded-md overflow-hidden border-[#215f47]/20">
              <Table>
                <TableHeader className="bg-[#215f47]/5">
                  <TableRow>
                    <TableHead className="text-[#215f47] font-medium">Section Name</TableHead>
                    <TableHead className="text-[#215f47] font-medium">Teacher</TableHead>
                    <TableHead className="text-[#215f47] font-medium">Enrollment</TableHead>
                    <TableHead className="text-[#215f47] font-medium text-right">Actions</TableHead>
                  </TableRow>
                </TableHeader>
                <TableBody>
                  {sections.filter(section => section.courseId === selectedCourse.id).length > 0 ? (
                    sections.filter(section => section.courseId === selectedCourse.id).map(section => (
                      <TableRow 
                        key={section.id} 
                        className="hover:bg-[#215f47]/5 transition-colors cursor-pointer"
                        onClick={() => handleSectionClick(section)}
                      >
                        <TableCell className="font-medium">{section.sectionName}</TableCell>
                        <TableCell>
                          {section.teacherId ? (
                            <Badge variant="outline" className="bg-[#215f47]/5 text-[#215f47]">
                              {section.teacher?.firstName} {section.teacher?.lastName || ''}
                            </Badge>
                          ) : (
                            <Badge variant="outline" className="bg-gray-100 text-gray-500">
                              Not Assigned
                            </Badge>
                          )}
                        </TableCell>
                        <TableCell>
                          <Badge className="bg-[#215f47]/10 text-[#215f47] border-[#215f47]/10">
                            {section.enrollmentCount || 0} students
                          </Badge>
                        </TableCell>
                        <TableCell className="text-right">
                          <Button
                            variant="ghost"
                            size="sm"
                            onClick={(e) => {
                              e.stopPropagation();
                              // Implement edit section logic
                            }}
                            className="h-8 w-8 p-0 text-[#215f47] hover:bg-[#215f47]/5"
                          >
                            <Pencil className="h-4 w-4" />
                          </Button>
                          <Button
                            variant="ghost"
                            size="sm"
                            onClick={(e) => {
                              e.stopPropagation();
                              // Implement delete section logic
                            }}
                            className="h-8 w-8 p-0 text-red-500 hover:text-red-700 hover:bg-red-50"
                          >
                            <Trash2 className="h-4 w-4" />
                          </Button>
                        </TableCell>
                      </TableRow>
                    ))
                  ) : (
                    <TableRow>
                      <TableCell colSpan={4} className="h-32 text-center">
                        <div className="flex flex-col items-center justify-center text-center p-4">
                          <Layers className="h-10 w-10 text-[#215f47]/40 mb-2" />
                          <h3 className="text-sm font-medium text-gray-600 mb-1">No Sections Found</h3>
                          <p className="text-xs text-gray-500 max-w-xs">
                            This course doesn't have any sections yet. Click the "Add Section" button to create one.
                          </p>
                        </div>
                      </TableCell>
                    </TableRow>
                  )}
                </TableBody>
              </Table>
            </div>
          )}
        </div>
      </AppModal>

      {/* Schedule Modal */}
      <AppModal
        isOpen={isScheduleModalOpen}
        onClose={() => setIsScheduleModalOpen(false)}
        title={`Schedules for ${selectedSection?.sectionName || 'Section'}`}
        description="View and manage class schedules for this section"
        size="lg"
      >
        <div className="space-y-4">
          <div className="flex justify-between items-center">
            <div>
              <div className="flex items-center gap-2 mb-1">
                <Badge variant="outline" className="bg-[#215f47]/5 border-[#215f47]/20 text-[#215f47] font-medium">
                  {selectedCourse?.courseCode}
                </Badge>
                <ChevronRight className="h-4 w-4 text-gray-400" />
                <span className="font-medium text-[#215f47]">{selectedSection?.sectionName}</span>
              </div>
              <p className="text-sm text-gray-500">
                {selectedSection?.teacherId ? (
                  <span>Teacher: <Badge variant="outline" className="bg-[#215f47]/5 text-[#215f47]">{selectedSection?.teacher?.firstName} {selectedSection?.teacher?.lastName}</Badge></span>
                ) : (
                  <span>No teacher assigned</span>
                )}
              </p>
            </div>
            <Button
              onClick={() => {
                // Here you could implement add schedule logic
                setIsScheduleModalOpen(false);
              }}
              className="bg-[#215f47] hover:bg-[#215f47]/90 flex items-center"
            >
              <Plus className="mr-2 h-4 w-4" />
              Add Schedule
            </Button>
          </div>

          {selectedSection && (
            <div className="border rounded-md overflow-hidden border-[#215f47]/20">
              <Table>
                <TableHeader className="bg-[#215f47]/5">
                  <TableRow>
                    <TableHead className="text-[#215f47] font-medium">Day</TableHead>
                    <TableHead className="text-[#215f47] font-medium">Time</TableHead>
                    <TableHead className="text-[#215f47] font-medium">Room</TableHead>
                    <TableHead className="text-[#215f47] font-medium">Type</TableHead>
                    <TableHead className="text-[#215f47] font-medium text-right">Actions</TableHead>
                  </TableRow>
                </TableHeader>
                <TableBody>
                  {schedules.length > 0 ? (
                    schedules.map(schedule => (
                      <TableRow key={schedule.id} className="hover:bg-[#215f47]/5 transition-colors">
                        <TableCell>
                          {schedule.dayOfWeek === 1 && 'Monday'}
                          {schedule.dayOfWeek === 2 && 'Tuesday'}
                          {schedule.dayOfWeek === 3 && 'Wednesday'}
                          {schedule.dayOfWeek === 4 && 'Thursday'}
                          {schedule.dayOfWeek === 5 && 'Friday'}
                          {schedule.dayOfWeek === 6 && 'Saturday'}
                          {schedule.dayOfWeek === 7 && 'Sunday'}
                        </TableCell>
                        <TableCell className="font-mono">
                          <div className="flex items-center gap-1">
                            <Clock className="h-3.5 w-3.5 text-[#215f47]" />
                            {schedule.timeStart} - {schedule.timeEnd}
                          </div>
                        </TableCell>
                        <TableCell>
                          <div className="flex items-center gap-1">
                            <Building2 className="h-3.5 w-3.5 text-[#215f47]" />
                            {schedule.room}
                          </div>
                        </TableCell>
                        <TableCell>
                          <Badge 
                            variant="outline" 
                            className={`
                              ${schedule.scheduleType === 'LEC' ? 'bg-blue-50 text-blue-600 border-blue-200' : ''}
                              ${schedule.scheduleType === 'LAB' ? 'bg-purple-50 text-purple-600 border-purple-200' : ''}
                              ${schedule.scheduleType === 'REC' ? 'bg-amber-50 text-amber-600 border-amber-200' : ''}
                            `}
                          >
                            {schedule.scheduleType === 'LEC' && 'Lecture'}
                            {schedule.scheduleType === 'LAB' && 'Laboratory'}
                            {schedule.scheduleType === 'REC' && 'Recitation'}
                          </Badge>
                        </TableCell>
                        <TableCell className="text-right">
                          <Button
                            variant="ghost"
                            size="sm"
                            onClick={() => {
                              // Implement edit schedule logic
                            }}
                            className="h-8 w-8 p-0 text-[#215f47] hover:bg-[#215f47]/5"
                          >
                            <Pencil className="h-4 w-4" />
                          </Button>
                          <Button
                            variant="ghost"
                            size="sm"
                            onClick={() => {
                              // Implement delete schedule logic
                            }}
                            className="h-8 w-8 p-0 text-red-500 hover:text-red-700 hover:bg-red-50"
                          >
                            <Trash2 className="h-4 w-4" />
                          </Button>
                        </TableCell>
                      </TableRow>
                    ))
                  ) : (
                    <TableRow>
                      <TableCell colSpan={5} className="h-32 text-center">
                        <div className="flex flex-col items-center justify-center text-center p-4">
                          <Calendar className="h-10 w-10 text-[#215f47]/40 mb-2" />
                          <h3 className="text-sm font-medium text-gray-600 mb-1">No Schedules Found</h3>
                          <p className="text-xs text-gray-500 max-w-xs">
                            This section doesn't have any schedules yet. Click the "Add Schedule" button to create one.
                          </p>
                        </div>
                      </TableCell>
                    </TableRow>
                  )}
                </TableBody>
              </Table>
            </div>
          )}
        </div>
      </AppModal>
    </DashboardLayout>
  );
};

export default CourseManagement;
