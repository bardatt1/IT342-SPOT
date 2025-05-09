import { useState, useEffect } from 'react';
import DashboardLayout from '../../components/ui/layout/DashboardLayout';
import { courseApi, type Course, type CourseCreateDto } from '../../lib/api/course';
import { sectionApi, type Section } from '../../lib/api/section';
import { scheduleApi, type Schedule } from '../../lib/api/schedule';
import { teacherApi, type Teacher } from '../../lib/api/teacher';
import { Button } from '../../components/ui/button';
import { Input } from '../../components/ui/input';
import { Label } from '../../components/ui/label';
import { Textarea } from '../../components/ui/textarea';
import { Card, CardContent, CardDescription, CardHeader, CardTitle} from '../../components/ui/card';
import { Alert, AlertDescription, AlertTitle } from '../../components/ui/alert';
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '../../components/ui/table';
import { Badge } from '../../components/ui/badge';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '../../components/ui/select';

import AppModal from '../../components/ui/modal/AppModal';
import DeleteConfirmationModal from '../../components/ui/modal/DeleteConfirmationModal';
import { Plus, X, Pencil, Bookmark, Users, CheckCircle2, AlertTriangle, Trash2, Calendar, Clock, Home, ListTodo, BookOpen, Code, FileText, ChevronRight, Layers, Building2 } from 'lucide-react';

const getDayName = (day: number) => {
  const days = ['Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday', 'Sunday'];
  return days[day - 1];
};

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
  
  // Additional modals for section management
  const [isAddSectionModalOpen, setIsAddSectionModalOpen] = useState(false);
  const [isEditSectionModalOpen, setIsEditSectionModalOpen] = useState(false);
  const [isAssignTeacherModalOpen, setIsAssignTeacherModalOpen] = useState(false);
  const [isDeleteSectionModalOpen, setIsDeleteSectionModalOpen] = useState(false);
  
  // Modals for schedule management
  const [isAddScheduleModalOpen, setIsAddScheduleModalOpen] = useState(false);
  const [isEditScheduleModalOpen, setIsEditScheduleModalOpen] = useState(false);
  const [isDeleteScheduleModalOpen, setIsDeleteScheduleModalOpen] = useState(false);
  // Allow null for selectedSchedule, but handle type coercion appropriately in updates
  const [selectedSchedule, setSelectedSchedule] = useState<Schedule | null>(null);
  
  // Course delete confirmation modal state
  const [isDeleteCourseModalOpen, setIsDeleteCourseModalOpen] = useState(false);
  const [courseToDelete, setCourseToDelete] = useState<{id: number; name: string} | null>(null);
  const [isDeleting, setIsDeleting] = useState(false);
  
  // Section form state
  const [sectionFormData, setSectionFormData] = useState({
    sectionName: '',
    sectionCode: '',
    courseId: 0,
    teacherId: null as number | null
  });
  
  // Schedule form state
  const [scheduleFormData, setScheduleFormData] = useState({
    dayOfWeek: 1, // Monday by default
    timeStart: '08:00',
    timeEnd: '09:30',
    room: '',
    scheduleType: 'LEC' as 'LEC' | 'LAB' | 'REC',
    sectionId: 0
  });
  
  // Teacher state
  const [teachers, setTeachers] = useState<Teacher[]>([]);
  const [selectedTeacherId, setSelectedTeacherId] = useState<number | null>(null);
  
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
        const sectionsData = await sectionApi.getAllSections();
        setSections(sectionsData);
        
        // Fetch teachers
        const teachersData = await teacherApi.getAll();
        setTeachers(teachersData);
        
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

  // Function to handle opening the sections modal when a course is clicked
  const handleCourseClick = (course: Course) => {
    setSelectedCourse(course);
    setIsSectionModalOpen(true);
  };

  // Function to handle opening the schedules modal when a section is clicked
  const handleSectionClick = async (section: Section) => {
    // We need to handle this in a type-safe way
    setSelectedSection(section);
    
    // Fetch schedules for the selected section
    await fetchSchedulesForSection(section.id);
    
    // Open the modal
    setIsScheduleModalOpen(true);
  };
  
  // Handle section form input change
  const handleSectionInputChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target;
    setSectionFormData({
      ...sectionFormData,
      [name]: value
    });
  };
  
  // Open add section modal
  const handleAddSectionClick = () => {
    // Reset form data and initialize with current course ID
    setSectionFormData({
      sectionName: '',
      sectionCode: '',
      courseId: selectedCourse?.id || 0,
      teacherId: null
    });
    setIsAddSectionModalOpen(true);
  };
  
  // Submit new section
  const handleAddSectionSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!selectedCourse) return;
    
    try {
      // Create section data
      const sectionData = {
        sectionName: sectionFormData.sectionName,
        courseId: selectedCourse.id,
        teacherId: sectionFormData.teacherId
      };
      
      // Create the section via API
      const newSection = await sectionApi.create(sectionData);
      
      // Create a complete section object with all properties needed for display
      // This is important for the section modal to display it correctly
      let completeSection = {
        ...newSection,
        courseId: selectedCourse.id, // Ensure courseId is set properly for filtering
        enrollmentCount: 0, // Initialize with 0 enrollments for display
      };
      
      // Add teacher information if a teacher is assigned
      if (newSection.teacherId) {
        const assignedTeacher = teachers.find(t => t.id === newSection.teacherId);
        if (assignedTeacher) {
          completeSection.teacher = assignedTeacher;
        }
      }
      
      // Update the sections array with the complete section
      setSections(prevSections => [...prevSections, completeSection]);
      
      // Close the modal
      setIsAddSectionModalOpen(false);
      
      // Reset the form data
      setSectionFormData({
        sectionName: '',
        sectionCode: '',
        courseId: selectedCourse.id,
        teacherId: null
      });
    } catch (err) {
      console.error('Error creating section:', err);
      setError('Failed to create section. Please try again.');
    }
  };
  
  // Open edit section modal
  const handleEditSectionClick = (section: Section, e: React.MouseEvent) => {
    e.stopPropagation(); // Prevent opening the schedule modal
    
    // Set form data with current section values
    setSectionFormData({
      sectionName: section.sectionName,
      sectionCode: '', // This property may not exist in the API model, so default to empty
      courseId: section.courseId,
      teacherId: section.teacherId || null
    });
    
    setSelectedSection(section);
    setIsEditSectionModalOpen(true);
  };
  
  // Submit section update
  const handleEditSectionSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!selectedSection) return;
    
    try {
      // Basic section data for update
      const sectionUpdateData = {
        id: selectedSection.id,
        sectionName: sectionFormData.sectionName,
        courseId: sectionFormData.courseId,
        teacherId: sectionFormData.teacherId
      };
      
      // Update via API
      const updatedSection = await sectionApi.update(sectionUpdateData);
      
      // Create a complete updated section object that preserves all properties needed for display
      const completeUpdatedSection = {
        ...selectedSection, // Keep all existing properties
        ...updatedSection,  // Override with updated properties
        courseId: sectionFormData.courseId, // Ensure courseId is correct for filtering
      };
      
      // Update teacher information if it changed
      if (completeUpdatedSection.teacherId !== selectedSection.teacherId) {
        const newTeacher = teachers.find(t => t.id === completeUpdatedSection.teacherId);
        completeUpdatedSection.teacher = newTeacher || null;
      }
      
      // Important: Update the selectedSection state first
      setSelectedSection(completeUpdatedSection);
      
      // Then update the section in the sections array using functional update
      // This ensures the section remains visible in the filtered list in the section modal
      setSections(prevSections => 
        prevSections.map(s => s.id === selectedSection.id ? completeUpdatedSection : s)
      );
      
      // Close the modal
      setIsEditSectionModalOpen(false);
    } catch (err) {
      console.error('Error updating section:', err);
      setError('Failed to update section. Please try again.');
    }
  };
  
  // Open assign teacher modal
  const handleAssignTeacherClick = (section: Section, e: React.MouseEvent) => {
    e.stopPropagation(); // Prevent opening the schedule modal
    setSelectedSection(section);
    // Ensure we have a valid teacherId or null
    setSelectedTeacherId(section.teacherId !== undefined ? section.teacherId : null);
    setIsAssignTeacherModalOpen(true);
  };
  
  // Submit teacher assignment
  const handleAssignTeacherSubmit = async () => {
    if (!selectedSection || selectedTeacherId === null) return;
    
    try {
      // Call the API to assign the teacher
      await sectionApi.assignTeacher(selectedSection.id, selectedTeacherId);
      
      // Get the selected teacher's full information
      const assignedTeacher = teachers.find(t => t.id === selectedTeacherId);
      
      // Create a complete updated section object that preserves all properties needed for display
      const completeUpdatedSection = {
        ...selectedSection,           // Keep all existing properties
        teacherId: selectedTeacherId, // Update the teacher ID
        teacher: assignedTeacher || null, // Update the teacher object
        // Explicitly keep the courseId to ensure filtering works in the section modal
        courseId: selectedSection.courseId
      };
      
      // First, update the selected section state
      setSelectedSection(completeUpdatedSection);
      
      // Then update the sections array using functional update to guarantee reactivity
      // This ensures the section with updated teacher remains visible in the section modal
      setSections(prevSections => 
        prevSections.map(s => s.id === selectedSection.id ? completeUpdatedSection : s)
      );
      
      // Close the modal
      setIsAssignTeacherModalOpen(false);
    } catch (err) {
      console.error('Error assigning teacher:', err);
      setError('Failed to assign teacher. Please try again.');
    }
  };
  
  // Open delete section modal
  const handleDeleteSectionClick = (section: Section, e: React.MouseEvent) => {
    e.stopPropagation(); // Prevent opening the schedule modal
    setSelectedSection(section);
    setIsDeleteSectionModalOpen(true);
  };
  
  // Submit section deletion
  const handleDeleteSectionConfirm = async () => {
    if (!selectedSection) return;
    
    try {
      // Delete the section via API
      await sectionApi.delete(selectedSection.id);
      
      // Remove the section from the state using functional update
      setSections(prevSections => 
        prevSections.filter(s => s.id !== selectedSection.id)
      );
      
      // Reset selectedSection to null
      setSelectedSection(null);
      
      // Close the confirmation modal
      setIsDeleteSectionModalOpen(false);
    } catch (err) {
      console.error('Error deleting section:', err);
      setError('Failed to delete section. Please try again.');
    }
  };

  // Function to fetch schedules for a specific section
  const fetchSchedulesForSection = async (sectionId: number) => {
    try {
      const scheduleData = await scheduleApi.getBySectionId(sectionId);
      // Ensure we never set null values in the schedules array
      setSchedules(scheduleData.filter((schedule): schedule is Schedule => schedule !== null));
    } catch (err) {
      console.error('Error fetching schedules:', err);
      setError('Failed to fetch schedules. Please try again.');
    }
  };

  // Schedule management handlers
  const handleScheduleInputChange = (e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>) => {
    const { name, value } = e.target;
    setScheduleFormData(prev => ({
      ...prev,
      [name]: name === 'dayOfWeek' ? parseInt(value) : value
    }));
  };

  // Open add schedule modal
  const handleAddScheduleClick = () => {
    if (!selectedSection) return;
    
    setScheduleFormData({
      dayOfWeek: 1,
      timeStart: '08:00',
      timeEnd: '09:30',
      room: '',
      scheduleType: 'LEC',
      sectionId: selectedSection.id
    });
    
    setIsAddScheduleModalOpen(true);
  };
  
  // Submit new schedule
  const handleAddScheduleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!selectedSection) return;
    
    try {
      // Prepare the schedule data
      const scheduleData = {
        dayOfWeek: scheduleFormData.dayOfWeek,
        timeStart: scheduleFormData.timeStart,
        timeEnd: scheduleFormData.timeEnd,
        room: scheduleFormData.room,
        scheduleType: scheduleFormData.scheduleType,
        sectionId: selectedSection.id
      };
      
      // Create the schedule via API
      const newSchedule = await scheduleApi.create(scheduleData);
      
      // Update the schedules array with the new schedule if it's not null
      if (newSchedule) {
        setSchedules(prevSchedules => [...prevSchedules, newSchedule]);
        
        // Close the modal only if successful
        setIsAddScheduleModalOpen(false);
        
        // Reset the form data
        setScheduleFormData({
          dayOfWeek: 1,
          timeStart: '08:00',
          timeEnd: '09:30',
          room: '',
          scheduleType: 'LEC',
          sectionId: selectedSection.id
        });
      }
    } catch (err) {
      console.error('Error creating schedule:', err);
      setError('Failed to create schedule. Please try again.');
    }
  };
  
  // Open edit schedule modal
  const handleEditScheduleClick = (schedule: Schedule) => {
    setSelectedSchedule(schedule);
    
    // Extract the section ID from the schedule object - adapt based on actual Schedule type
    const sectionId = selectedSection?.id || 0; 
    
    setScheduleFormData({
      dayOfWeek: schedule.dayOfWeek,
      timeStart: schedule.timeStart || '08:00',
      timeEnd: schedule.timeEnd || '09:30',
      room: schedule.room || '',
      scheduleType: (schedule.scheduleType as 'LEC' | 'LAB' | 'REC') || 'LEC',
      sectionId: sectionId
    });
    
    setIsEditScheduleModalOpen(true);
  };
  
  // Submit schedule update
  const handleEditScheduleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!selectedSchedule) return;
    
    try {
      // Prepare update data
      const scheduleUpdateData = {
        id: selectedSchedule.id,
        dayOfWeek: scheduleFormData.dayOfWeek,
        timeStart: scheduleFormData.timeStart,
        timeEnd: scheduleFormData.timeEnd,
        room: scheduleFormData.room,
        scheduleType: scheduleFormData.scheduleType,
        sectionId: scheduleFormData.sectionId
      };
      
      // Update via API - ensure this matches scheduleApi.update requirements
      // Pass the required arguments to match the scheduleApi.update function signature
      const updatedSchedule = await scheduleApi.update(selectedSchedule.id, scheduleUpdateData);
      
      // Update the selected schedule and the schedules array - use a type-safe approach
      if (updatedSchedule) {
        setSelectedSchedule(updatedSchedule);
        
        // Use a type-safe approach for the map operation
        setSchedules(prevSchedules => 
          prevSchedules.map(s => (s.id === selectedSchedule.id) ? updatedSchedule : s)
        );
      }
      
      // Close the modal
      setIsEditScheduleModalOpen(false);
    } catch (err) {
      console.error('Error updating schedule:', err);
      setError('Failed to update schedule. Please try again.');
    }
  };
  
  // Open delete schedule modal
  const handleDeleteScheduleClick = (schedule: Schedule) => {
    setSelectedSchedule(schedule);
    setIsDeleteScheduleModalOpen(true);
  };
  
  // Submit schedule deletion
  const handleDeleteScheduleConfirm = async () => {
    if (!selectedSchedule) return;
    
    try {
      // Delete the schedule via API
      await scheduleApi.delete(selectedSchedule.id);
      
      // Store the ID before clearing the selected schedule
      const deletedScheduleId = selectedSchedule.id;
      
      // Remove the schedule from the schedules array using a type-safe filter
      setSchedules(prevSchedules => 
        prevSchedules.filter(s => s.id !== deletedScheduleId)
      );
      
      // Reset selected schedule using a type assertion to handle the null assignment
      // This is safe because we're explicitly setting it to null and handling the UI accordingly
      setSelectedSchedule(null as unknown as Schedule);
      setIsDeleteScheduleModalOpen(false);
    } catch (err) {
      console.error('Error deleting schedule:', err);
      setError('Failed to delete schedule. Please try again.');
    }
  };
  
  // Functions for handling course-related actions
  const handleAddNew = () => {
    setFormData({
      courseName: '',
      courseDescription: '',
      courseCode: ''
    });
    setFormType('add');
    setSelectedCourseId(null);
    setShowForm(true);
  };
  
  const handleEdit = (id: number) => {
    const courseToEdit = courses.find(c => c.id === id);
    if (courseToEdit) {
      setFormData({
        courseName: courseToEdit.courseName,
        courseDescription: courseToEdit.courseDescription,
        courseCode: courseToEdit.courseCode || ''
      });
      setFormType('edit');
      setSelectedCourseId(id);
      setShowForm(true);
    }
  };
  
  // Show delete confirmation modal
  const handleDeleteClick = (id: number) => {
    const courseToDeleteData = courses.find(c => c.id === id);
    if (courseToDeleteData) {
      setCourseToDelete({ id, name: courseToDeleteData.courseName });
      setIsDeleteCourseModalOpen(true);
    }
  };
  
  // Perform the actual deletion
  const handleDelete = async () => {
    if (!courseToDelete) return;
    
    try {
      setIsDeleting(true);
      
      await courseApi.delete(courseToDelete.id);
      setCourses(courses.filter(c => c.id !== courseToDelete.id));
      
      // Close the modal and reset state
      setIsDeleteCourseModalOpen(false);
      setCourseToDelete(null);
      setIsDeleting(false);
    } catch (error) {
      console.error('Error deleting course:', error);
      setError('Failed to delete course. Please try again.');
      setIsDeleting(false);
    }
  };
  
  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    
    try {
      if (formType === 'add') {
        // Add new course
        const newCourse = await courseApi.create(formData as CourseCreateDto);
        setCourses([...courses, newCourse]);
      } else if (selectedCourseId) {
        // Edit existing course
        // Making sure we're passing the data in the format the API expects
        const courseUpdateData = {
          id: selectedCourseId,
          ...formData
        };
        const updatedCourse = await courseApi.update(courseUpdateData);
        setCourses(courses.map(c => c.id === selectedCourseId ? updatedCourse : c));
      }
      
      // Reset form after submission
      resetForm();
    } catch (error) {
      console.error('Error submitting course:', error);
      setError(`Failed to ${formType === 'add' ? 'create' : 'update'} course. Please try again.`);
    }
  };
  
  const handleInputChange = (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) => {
    const { name, value } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: value
    }));
  };

  // Reset form for adding/editing a course
  const resetForm = () => {
    setShowForm(false);
    setFormData({
      courseName: '',
      courseDescription: '',
      courseCode: ''
    });
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
                              handleDeleteClick(course.id);
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
              onClick={handleAddSectionClick}
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
                          <div className="flex items-center justify-end space-x-1">
                            <Button
                              variant="ghost"
                              size="sm"
                              onClick={(e) => handleAssignTeacherClick(section, e)}
                              className="h-8 w-8 p-0 flex items-center justify-center text-blue-600 hover:bg-blue-50 rounded-full"
                              title="Assign Teacher"
                            >
                              <Users className="h-3.5 w-3.5" />
                            </Button>
                            <Button
                              variant="ghost"
                              size="sm"
                              onClick={(e) => handleEditSectionClick(section, e)}
                              className="h-8 w-8 p-0 flex items-center justify-center text-[#215f47] hover:bg-[#215f47]/5 rounded-full"
                              title="Edit Section"
                            >
                              <Pencil className="h-3.5 w-3.5" />
                            </Button>
                            <Button
                              variant="ghost"
                              size="sm"
                              onClick={(e) => handleDeleteSectionClick(section, e)}
                              className="h-8 w-8 p-0 flex items-center justify-center text-red-600 hover:bg-red-50 rounded-full"
                              title="Delete Section"
                            >
                              <Trash2 className="h-3.5 w-3.5" />
                            </Button>
                          </div>
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
              onClick={handleAddScheduleClick}
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
                            onClick={() => handleEditScheduleClick(schedule)}
                            className="h-8 w-8 p-0 text-[#215f47] hover:bg-[#215f47]/5"
                          >
                            <Pencil className="h-4 w-4" />
                          </Button>
                          <Button
                            variant="ghost"
                            size="sm"
                            onClick={() => handleDeleteScheduleClick(schedule)}
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

      {/* Add Section Modal */}
      <AppModal
        isOpen={isAddSectionModalOpen}
        onClose={() => setIsAddSectionModalOpen(false)}
        title="Add New Section"
        description={`Create a new section for ${selectedCourse?.courseName || 'this course'}`}
        size="md"
      >
        <form onSubmit={handleAddSectionSubmit} className="space-y-4">
          <div className="space-y-2">
            <Label htmlFor="sectionName" className="text-[#215f47]">
              <div className="flex items-center gap-1.5">
                <Bookmark className="h-3.5 w-3.5" />
                Section Name
              </div>
            </Label>
            <Input
              id="sectionName"
              name="sectionName"
              value={sectionFormData.sectionName}
              onChange={handleSectionInputChange}
              required
              className="border-[#215f47]/20 focus-visible:ring-[#215f47]/20 focus-visible:ring-offset-1 focus-visible:ring-offset-[#215f47]/90"
              placeholder="e.g., Section A"
            />
          </div>
          
          <div className="flex justify-end space-x-3 pt-2">
            <Button 
              variant="outline" 
              type="button" 
              onClick={() => setIsAddSectionModalOpen(false)}
              className="border-[#215f47]/20 text-[#215f47] hover:bg-[#215f47]/5 hover:text-[#215f47] flex items-center"
            >
              <X className="mr-2 h-4 w-4" />
              Cancel
            </Button>
            <Button 
              type="submit"
              className="bg-[#215f47] hover:bg-[#215f47]/90 flex items-center"
            >
              <Plus className="mr-2 h-4 w-4" />
              Create Section
            </Button>
          </div>
        </form>
      </AppModal>

      {/* Edit Section Modal */}
      <AppModal
        isOpen={isEditSectionModalOpen}
        onClose={() => setIsEditSectionModalOpen(false)}
        title="Edit Section"
        description={`Update section details for ${selectedSection?.sectionName || 'this section'}`}
        size="md"
      >
        <form onSubmit={handleEditSectionSubmit} className="space-y-4">
          <div className="space-y-2">
            <Label htmlFor="editSectionName" className="text-[#215f47]">
              <div className="flex items-center gap-1.5">
                <Bookmark className="h-3.5 w-3.5" />
                Section Name
              </div>
            </Label>
            <Input
              id="editSectionName"
              name="sectionName"
              value={sectionFormData.sectionName}
              onChange={handleSectionInputChange}
              required
              className="border-[#215f47]/20 focus-visible:ring-[#215f47]/20 focus-visible:ring-offset-1 focus-visible:ring-offset-[#215f47]/90"
            />
          </div>
          
          <div className="flex justify-end space-x-3 pt-2">
            <Button 
              variant="outline" 
              type="button" 
              onClick={() => setIsEditSectionModalOpen(false)}
              className="border-[#215f47]/20 text-[#215f47] hover:bg-[#215f47]/5 hover:text-[#215f47] flex items-center"
            >
              <X className="mr-2 h-4 w-4" />
              Cancel
            </Button>
            <Button 
              type="submit"
              className="bg-[#215f47] hover:bg-[#215f47]/90 flex items-center"
            >
              <Pencil className="mr-2 h-4 w-4" />
              Update Section
            </Button>
          </div>
        </form>
      </AppModal>

      {/* Assign Teacher Modal */}
      <AppModal
        isOpen={isAssignTeacherModalOpen}
        onClose={() => setIsAssignTeacherModalOpen(false)}
        title="Assign Teacher"
        description={`Select a teacher for ${selectedSection?.sectionName || 'this section'}`}
        size="sm"
      >
        <div className="space-y-4">
          <div className="space-y-2">
            <Label htmlFor="teacherSelect" className="text-[#215f47]">
              <div className="flex items-center gap-1.5">
                <Users className="h-3.5 w-3.5" />
                Teacher
              </div>
            </Label>
            <Select 
              value={selectedTeacherId?.toString() || ''} 
              onValueChange={(value) => setSelectedTeacherId(Number(value))}
            >
              <SelectTrigger className="border-[#215f47]/20 focus:ring-[#215f47]/20">
                <SelectValue placeholder="Select a teacher" />
              </SelectTrigger>
              <SelectContent>
                {teachers.map(teacher => (
                  <SelectItem key={teacher.id} value={teacher.id.toString()}>
                    {teacher.firstName} {teacher.lastName}
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>
          </div>
          
          <div className="flex justify-end space-x-3 pt-2">
            <Button 
              variant="outline" 
              type="button" 
              onClick={() => setIsAssignTeacherModalOpen(false)}
              className="border-[#215f47]/20 text-[#215f47] hover:bg-[#215f47]/5 hover:text-[#215f47] flex items-center"
            >
              <X className="mr-2 h-4 w-4" />
              Cancel
            </Button>
            <Button 
              onClick={handleAssignTeacherSubmit}
              disabled={!selectedTeacherId}
              className="bg-[#215f47] hover:bg-[#215f47]/90 flex items-center"
            >
              <CheckCircle2 className="mr-2 h-4 w-4" />
              Assign Teacher
            </Button>
          </div>
        </div>
      </AppModal>

      {/* Delete Section Confirmation Modal */}
      <AppModal
        isOpen={isDeleteSectionModalOpen}
        onClose={() => setIsDeleteSectionModalOpen(false)}
        title="Delete Section"
        description="Are you sure you want to delete this section? This action cannot be undone."
        size="sm"
      >
        {selectedSection && (
          <div className="space-y-4">
            <div className="rounded-md bg-red-50 p-4 border border-red-200">
              <div className="flex items-center">
                <AlertTriangle className="h-5 w-5 text-red-600 mr-3" />
                <div>
                  <h3 className="text-sm font-medium text-red-800">Warning: Permanent Deletion</h3>
                  <div className="mt-2 text-sm text-red-700">
                    <p>You are about to delete:</p>
                    <p className="font-medium mt-1">{selectedSection.sectionName}</p>
                    <p className="mt-2 text-xs">This will delete all schedules, attendance records, and enrollments associated with this section.</p>
                  </div>
                </div>
              </div>
            </div>
            
            <div className="flex justify-end space-x-3 pt-2">
              <Button 
                variant="outline" 
                type="button" 
                onClick={() => setIsDeleteSectionModalOpen(false)}
                className="border-[#215f47]/20 text-[#215f47] hover:bg-[#215f47]/5 hover:text-[#215f47] flex items-center"
              >
                <X className="mr-2 h-4 w-4" />
                Cancel
              </Button>
              <Button 
                variant="destructive"
                onClick={handleDeleteSectionConfirm}
                className="bg-red-600 hover:bg-red-700 flex items-center"
              >
                <Trash2 className="mr-2 h-4 w-4" />
                Delete Section
              </Button>
            </div>
          </div>
        )}
      </AppModal>

      {/* Add Section Modal */}
      <AppModal
        isOpen={isAddSectionModalOpen}
        onClose={() => setIsAddSectionModalOpen(false)}
        title="Add New Section"
        description={`Create a new section for ${selectedCourse?.courseName || 'this course'}`}
        size="md"
      >
        <form onSubmit={handleAddSectionSubmit} className="space-y-4">
          <div className="space-y-2">
            <Label htmlFor="sectionName" className="text-[#215f47]">
              <div className="flex items-center gap-1.5">
                <Bookmark className="h-3.5 w-3.5" />
                Section Name
              </div>
            </Label>
            <Input
              id="sectionName"
              name="sectionName"
              value={sectionFormData.sectionName}
              onChange={handleSectionInputChange}
              required
              className="border-[#215f47]/20 focus-visible:ring-[#215f47]/20 focus-visible:ring-offset-1 focus-visible:ring-offset-[#215f47]/90"
              placeholder="e.g., Section A"
            />
          </div>
          
          <div className="flex justify-end space-x-3 pt-2">
            <Button 
              variant="outline" 
              type="button" 
              onClick={() => setIsAddSectionModalOpen(false)}
              className="border-[#215f47]/20 text-[#215f47] hover:bg-[#215f47]/5 hover:text-[#215f47] flex items-center"
            >
              <X className="mr-2 h-4 w-4" />
              Cancel
            </Button>
            <Button 
              type="submit"
              className="bg-[#215f47] hover:bg-[#215f47]/90 flex items-center"
            >
              <Plus className="mr-2 h-4 w-4" />
              Create Section
            </Button>
          </div>
        </form>
      </AppModal>

      {/* Edit Section Modal */}
      <AppModal
        isOpen={isEditSectionModalOpen}
        onClose={() => setIsEditSectionModalOpen(false)}
        title="Edit Section"
        description={`Update section details for ${selectedSection?.sectionName || 'this section'}`}
        size="md"
      >
        <form onSubmit={handleEditSectionSubmit} className="space-y-4">
          <div className="space-y-2">
            <Label htmlFor="editSectionName" className="text-[#215f47]">
              <div className="flex items-center gap-1.5">
                <Bookmark className="h-3.5 w-3.5" />
                Section Name
              </div>
            </Label>
            <Input
              id="editSectionName"
              name="sectionName"
              value={sectionFormData.sectionName}
              onChange={handleSectionInputChange}
              required
              className="border-[#215f47]/20 focus-visible:ring-[#215f47]/20 focus-visible:ring-offset-1 focus-visible:ring-offset-[#215f47]/90"
            />
          </div>
          
          <div className="flex justify-end space-x-3 pt-2">
            <Button 
              variant="outline" 
              type="button" 
              onClick={() => setIsEditSectionModalOpen(false)}
              className="border-[#215f47]/20 text-[#215f47] hover:bg-[#215f47]/5 hover:text-[#215f47] flex items-center"
            >
              <X className="mr-2 h-4 w-4" />
              Cancel
            </Button>
            <Button 
              type="submit"
              className="bg-[#215f47] hover:bg-[#215f47]/90 flex items-center"
            >
              <Pencil className="mr-2 h-4 w-4" />
              Update Section
            </Button>
          </div>
        </form>
      </AppModal>

      {/* Assign Teacher Modal */}
      <AppModal
        isOpen={isAssignTeacherModalOpen}
        onClose={() => setIsAssignTeacherModalOpen(false)}
        title="Assign Teacher"
        description={`Select a teacher for ${selectedSection?.sectionName || 'this section'}`}
        size="sm"
      >
        <div className="space-y-4">
          <div className="space-y-2">
            <Label htmlFor="teacherSelect" className="text-[#215f47]">
              <div className="flex items-center gap-1.5">
                <Users className="h-3.5 w-3.5" />
                Teacher
              </div>
            </Label>
            <Select 
              value={selectedTeacherId?.toString() || ''} 
              onValueChange={(value) => setSelectedTeacherId(Number(value))}
            >
              <SelectTrigger className="border-[#215f47]/20 focus:ring-[#215f47]/20">
                <SelectValue placeholder="Select a teacher" />
              </SelectTrigger>
              <SelectContent>
                {teachers.map(teacher => (
                  <SelectItem key={teacher.id} value={teacher.id.toString()}>
                    {teacher.firstName} {teacher.lastName}
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>
          </div>
          
          <div className="flex justify-end space-x-3 pt-2">
            <Button 
              variant="outline" 
              type="button" 
              onClick={() => setIsAssignTeacherModalOpen(false)}
              className="border-[#215f47]/20 text-[#215f47] hover:bg-[#215f47]/5 hover:text-[#215f47] flex items-center"
            >
              <X className="mr-2 h-4 w-4" />
              Cancel
            </Button>
            <Button 
              onClick={handleAssignTeacherSubmit}
              disabled={!selectedTeacherId}
              className="bg-[#215f47] hover:bg-[#215f47]/90 flex items-center"
            >
              <CheckCircle2 className="mr-2 h-4 w-4" />
              Assign Teacher
            </Button>
          </div>
        </div>
      </AppModal>

      {/* Delete Section Confirmation Modal */}
      <AppModal
        isOpen={isDeleteSectionModalOpen}
        onClose={() => setIsDeleteSectionModalOpen(false)}
        title="Delete Section"
        description="Are you sure you want to delete this section? This action cannot be undone."
        size="sm"
      >
        {selectedSection && (
          <div className="space-y-4">
            <div className="rounded-md bg-red-50 p-4 border border-red-200">
              <div className="flex items-center">
                <AlertTriangle className="h-5 w-5 text-red-600 mr-3" />
                <div>
                  <h3 className="text-sm font-medium text-red-800">Warning: Permanent Deletion</h3>
                  <div className="mt-2 text-sm text-red-700">
                    <p>You are about to delete:</p>
                    <p className="font-medium mt-1">{selectedSection.sectionName}</p>
                    <p className="mt-2 text-xs">This will delete all schedules, attendance records, and enrollments associated with this section.</p>
                  </div>
                </div>
              </div>
            </div>
            
            <div className="flex justify-end space-x-3 pt-2">
              <Button 
                variant="outline" 
                type="button" 
                onClick={() => setIsDeleteSectionModalOpen(false)}
                className="border-[#215f47]/20 text-[#215f47] hover:bg-[#215f47]/5 hover:text-[#215f47] flex items-center"
              >
                <X className="mr-2 h-4 w-4" />
                Cancel
              </Button>
              <Button 
                variant="destructive"
                onClick={handleDeleteSectionConfirm}
                className="bg-red-600 hover:bg-red-700 flex items-center"
              >
                <Trash2 className="mr-2 h-4 w-4" />
                Delete Permanently
              </Button>
            </div>
          </div>
        )}
      </AppModal>

      {/* Add Schedule Modal */}
      <AppModal
        isOpen={isAddScheduleModalOpen}
        onClose={() => setIsAddScheduleModalOpen(false)}
        title="Add New Schedule"
        description={`Create a new schedule for ${selectedSection?.sectionName || 'this section'}`}
        size="md"
      >
        <form onSubmit={handleAddScheduleSubmit} className="space-y-4">
          <div className="space-y-4">
            {/* Day of Week */}
            <div className="space-y-2">
              <Label htmlFor="dayOfWeek" className="text-[#215f47]">
                <div className="flex items-center gap-1.5">
                  <Calendar className="h-3.5 w-3.5" />
                  Day
                </div>
              </Label>
              <Select 
                name="dayOfWeek" 
                value={scheduleFormData.dayOfWeek.toString()} 
                onValueChange={(value) => setScheduleFormData(prev => ({
                  ...prev,
                  dayOfWeek: parseInt(value)
                }))}
              >
                <SelectTrigger className="border-[#215f47]/20 focus:ring-[#215f47]/20">
                  <SelectValue placeholder="Select day" />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="1">Monday</SelectItem>
                  <SelectItem value="2">Tuesday</SelectItem>
                  <SelectItem value="3">Wednesday</SelectItem>
                  <SelectItem value="4">Thursday</SelectItem>
                  <SelectItem value="5">Friday</SelectItem>
                  <SelectItem value="6">Saturday</SelectItem>
                  <SelectItem value="7">Sunday</SelectItem>
                </SelectContent>
              </Select>
            </div>
            
            {/* Time Start */}
            <div className="space-y-2">
              <Label htmlFor="timeStart" className="text-[#215f47]">
                <div className="flex items-center gap-1.5">
                  <Clock className="h-3.5 w-3.5" />
                  Start Time
                </div>
              </Label>
              <Input
                id="timeStart"
                name="timeStart"
                type="time"
                value={scheduleFormData.timeStart}
                onChange={handleScheduleInputChange}
                required
                className="border-[#215f47]/20 focus-visible:ring-[#215f47]/20 focus-visible:ring-offset-1 focus-visible:ring-offset-[#215f47]/90"
              />
            </div>
            
            {/* Time End */}
            <div className="space-y-2">
              <Label htmlFor="timeEnd" className="text-[#215f47]">
                <div className="flex items-center gap-1.5">
                  <Clock className="h-3.5 w-3.5" />
                  End Time
                </div>
              </Label>
              <Input
                id="timeEnd"
                name="timeEnd"
                type="time"
                value={scheduleFormData.timeEnd}
                onChange={handleScheduleInputChange}
                required
                className="border-[#215f47]/20 focus-visible:ring-[#215f47]/20 focus-visible:ring-offset-1 focus-visible:ring-offset-[#215f47]/90"
              />
            </div>
            
            {/* Room */}
            <div className="space-y-2">
              <Label htmlFor="room" className="text-[#215f47]">
                <div className="flex items-center gap-1.5">
                  <Home className="h-3.5 w-3.5" />
                  Room
                </div>
              </Label>
              <Input
                id="room"
                name="room"
                value={scheduleFormData.room}
                onChange={handleScheduleInputChange}
                required
                className="border-[#215f47]/20 focus-visible:ring-[#215f47]/20 focus-visible:ring-offset-1 focus-visible:ring-offset-[#215f47]/90"
                placeholder="e.g., Room 101"
              />
            </div>
            
            {/* Schedule Type */}
            <div className="space-y-2">
              <Label htmlFor="scheduleType" className="text-[#215f47]">
                <div className="flex items-center gap-1.5">
                  <ListTodo className="h-3.5 w-3.5" />
                  Type
                </div>
              </Label>
              <Select 
                name="scheduleType" 
                value={scheduleFormData.scheduleType} 
                onValueChange={(value) => setScheduleFormData(prev => ({
                  ...prev,
                  scheduleType: value as 'LEC' | 'LAB' | 'REC'
                }))}
              >
                <SelectTrigger className="border-[#215f47]/20 focus:ring-[#215f47]/20">
                  <SelectValue placeholder="Select type" />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="LEC">Lecture</SelectItem>
                  <SelectItem value="LAB">Laboratory</SelectItem>
                  <SelectItem value="REC">Recitation</SelectItem>
                </SelectContent>
              </Select>
            </div>
          </div>
          
          <div className="flex justify-end space-x-3 pt-2">
            <Button 
              variant="outline" 
              type="button" 
              onClick={() => setIsAddScheduleModalOpen(false)}
              className="border-[#215f47]/20 text-[#215f47] hover:bg-[#215f47]/5 hover:text-[#215f47] flex items-center"
            >
              <X className="mr-2 h-4 w-4" />
              Cancel
            </Button>
            <Button 
              type="submit"
              className="bg-[#215f47] hover:bg-[#215f47]/90 flex items-center"
            >
              <Plus className="mr-2 h-4 w-4" />
              Add Schedule
            </Button>
          </div>
        </form>
      </AppModal>

      {/* Edit Schedule Modal */}
      <AppModal
        isOpen={isEditScheduleModalOpen}
        onClose={() => setIsEditScheduleModalOpen(false)}
        title="Edit Schedule"
        description={`Update schedule details for ${selectedSection?.sectionName || 'this section'}`}
        size="md"
      >
        <form onSubmit={handleEditScheduleSubmit} className="space-y-4">
          <div className="space-y-4">
            {/* Day of Week */}
            <div className="space-y-2">
              <Label htmlFor="editDayOfWeek" className="text-[#215f47]">
                <div className="flex items-center gap-1.5">
                  <Calendar className="h-3.5 w-3.5" />
                  Day
                </div>
              </Label>
              <Select 
                name="dayOfWeek" 
                value={scheduleFormData.dayOfWeek.toString()} 
                onValueChange={(value) => setScheduleFormData(prev => ({
                  ...prev,
                  dayOfWeek: parseInt(value)
                }))}
              >
                <SelectTrigger className="border-[#215f47]/20 focus:ring-[#215f47]/20">
                  <SelectValue placeholder="Select day" />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="1">Monday</SelectItem>
                  <SelectItem value="2">Tuesday</SelectItem>
                  <SelectItem value="3">Wednesday</SelectItem>
                  <SelectItem value="4">Thursday</SelectItem>
                  <SelectItem value="5">Friday</SelectItem>
                  <SelectItem value="6">Saturday</SelectItem>
                  <SelectItem value="7">Sunday</SelectItem>
                </SelectContent>
              </Select>
            </div>
            
            {/* Time Start */}
            <div className="space-y-2">
              <Label htmlFor="editTimeStart" className="text-[#215f47]">
                <div className="flex items-center gap-1.5">
                  <Clock className="h-3.5 w-3.5" />
                  Start Time
                </div>
              </Label>
              <Input
                id="editTimeStart"
                name="timeStart"
                type="time"
                value={scheduleFormData.timeStart}
                onChange={handleScheduleInputChange}
                required
                className="border-[#215f47]/20 focus-visible:ring-[#215f47]/20 focus-visible:ring-offset-1 focus-visible:ring-offset-[#215f47]/90"
              />
            </div>
            
            {/* Time End */}
            <div className="space-y-2">
              <Label htmlFor="editTimeEnd" className="text-[#215f47]">
                <div className="flex items-center gap-1.5">
                  <Clock className="h-3.5 w-3.5" />
                  End Time
                </div>
              </Label>
              <Input
                id="editTimeEnd"
                name="timeEnd"
                type="time"
                value={scheduleFormData.timeEnd}
                onChange={handleScheduleInputChange}
                required
                className="border-[#215f47]/20 focus-visible:ring-[#215f47]/20 focus-visible:ring-offset-1 focus-visible:ring-offset-[#215f47]/90"
              />
            </div>
            
            {/* Room */}
            <div className="space-y-2">
              <Label htmlFor="editRoom" className="text-[#215f47]">
                <div className="flex items-center gap-1.5">
                  <Home className="h-3.5 w-3.5" />
                  Room
                </div>
              </Label>
              <Input
                id="editRoom"
                name="room"
                value={scheduleFormData.room}
                onChange={handleScheduleInputChange}
                required
                className="border-[#215f47]/20 focus-visible:ring-[#215f47]/20 focus-visible:ring-offset-1 focus-visible:ring-offset-[#215f47]/90"
              />
            </div>
            
            {/* Schedule Type */}
            <div className="space-y-2">
              <Label htmlFor="editScheduleType" className="text-[#215f47]">
                <div className="flex items-center gap-1.5">
                  <ListTodo className="h-3.5 w-3.5" />
                  Type
                </div>
              </Label>
              <Select 
                name="scheduleType" 
                value={scheduleFormData.scheduleType} 
                onValueChange={(value) => setScheduleFormData(prev => ({
                  ...prev,
                  scheduleType: value as 'LEC' | 'LAB' | 'REC'
                }))}
              >
                <SelectTrigger className="border-[#215f47]/20 focus:ring-[#215f47]/20">
                  <SelectValue placeholder="Select type" />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="LEC">Lecture</SelectItem>
                  <SelectItem value="LAB">Laboratory</SelectItem>
                  <SelectItem value="REC">Recitation</SelectItem>
                </SelectContent>
              </Select>
            </div>
          </div>
          
          <div className="flex justify-end space-x-3 pt-2">
            <Button 
              variant="outline" 
              type="button" 
              onClick={() => setIsEditScheduleModalOpen(false)}
              className="border-[#215f47]/20 text-[#215f47] hover:bg-[#215f47]/5 hover:text-[#215f47] flex items-center"
            >
              <X className="mr-2 h-4 w-4" />
              Cancel
            </Button>
            <Button 
              type="submit"
              className="bg-[#215f47] hover:bg-[#215f47]/90 flex items-center"
            >
              <Pencil className="mr-2 h-4 w-4" />
              Update Schedule
            </Button>
          </div>
        </form>
      </AppModal>

      {/* Delete Schedule Confirmation Modal */}
      <AppModal
        isOpen={isDeleteScheduleModalOpen}
        onClose={() => setIsDeleteScheduleModalOpen(false)}
        title="Delete Schedule"
        description="Are you sure you want to delete this schedule? This action cannot be undone."
        size="sm"
      >
        {selectedSchedule && (
          <div className="space-y-4">
            <div className="rounded-md bg-red-50 p-4 border border-red-200">
              <div className="flex items-center">
                <AlertTriangle className="h-5 w-5 text-red-600 mr-3" />
                <div>
                  <h3 className="text-sm font-medium text-red-800">Warning: Permanent Deletion</h3>
                  <div className="mt-2 text-sm text-red-700">
                    <p>You are about to delete this schedule:</p>
                    <div className="mt-2 bg-white p-2 rounded-md">
                      <p><strong>Day:</strong> {getDayName(selectedSchedule.dayOfWeek)}</p>
                      <p><strong>Time:</strong> {selectedSchedule.timeStart} - {selectedSchedule.timeEnd}</p>
                      <p><strong>Room:</strong> {selectedSchedule.room}</p>
                      <p><strong>Type:</strong> {selectedSchedule.scheduleType}</p>
                    </div>
                  </div>
                </div>
              </div>
            </div>
            
            <div className="flex justify-end space-x-3 pt-2">
              <Button 
                variant="outline" 
                type="button" 
                onClick={() => setIsDeleteScheduleModalOpen(false)}
                className="border-[#215f47]/20 text-[#215f47] hover:bg-[#215f47]/5 hover:text-[#215f47] flex items-center"
              >
                <X className="mr-2 h-4 w-4" />
                Cancel
              </Button>
              <Button 
                variant="destructive"
                onClick={handleDeleteScheduleConfirm}
                className="bg-red-600 hover:bg-red-700 flex items-center"
              >
                <Trash2 className="mr-2 h-4 w-4" />
                Delete Schedule
              </Button>
            </div>
          </div>
        )}
      </AppModal>

      {/* Course Delete Confirmation Modal */}
      <DeleteConfirmationModal
        isOpen={isDeleteCourseModalOpen}
        onClose={() => {
          setIsDeleteCourseModalOpen(false);
          setCourseToDelete(null);
          setIsDeleting(false);
        }}
        onConfirm={handleDelete}
        title="Delete Course"
        itemName={courseToDelete?.name || ''}
        itemType="course"
        warningText="This action cannot be undone. It will permanently delete this course and all associated data, including sections and schedules."
        isLoading={isDeleting}
      />
    </DashboardLayout>
  );
};

export default CourseManagement;
