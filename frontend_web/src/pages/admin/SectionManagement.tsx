import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import DashboardLayout from '../../components/ui/layout/DashboardLayout';
import { sectionApi, type Section, type SectionCreateDto } from '../../lib/api/section';
import { courseApi, type Course } from '../../lib/api/course';
import { teacherApi, type Teacher } from '../../lib/api/teacher';
import { Button } from '../../components/ui/button';
import { Input } from '../../components/ui/input';
import { Label } from '../../components/ui/label';
import { Card, CardContent, CardHeader, CardTitle, CardDescription, CardFooter } from '../../components/ui/card';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '../../components/ui/select';
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '../../components/ui/table';
import { Dialog, DialogContent, DialogDescription, DialogFooter, DialogHeader, DialogTitle } from '../../components/ui/dialog';
import { Badge } from '../../components/ui/badge';
import { Alert, AlertDescription, AlertTitle } from '../../components/ui/alert';
import { Tabs, TabsList, TabsTrigger } from '../../components/ui/tabs';
import { Plus, Pencil, Trash2, UserCog, Book, BookOpen, AlertTriangle, CalendarDays } from 'lucide-react';

const SectionManagement = () => {
  const navigate = useNavigate();
  const [sections, setSections] = useState<Section[]>([]);
  const [courses, setCourses] = useState<Course[]>([]);
  const [teachers, setTeachers] = useState<Teacher[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  
  // Handle tab change
  const handleTabChange = (value: string) => {
    if (value === 'sections') {
      // Already on sections tab, no navigation needed
    } else if (value === 'schedules') {
      navigate('/admin/schedules');
    }
  };
  
  // Form state
  const [showForm, setShowForm] = useState(false);
  const [formType, setFormType] = useState<'add' | 'edit'>('add');
  const [selectedSectionId, setSelectedSectionId] = useState<number | null>(null);
  
  const [formData, setFormData] = useState({
    courseId: '',
    sectionName: '',
    teacherId: '',
    enrollmentKey: ''
  });

  // State for assigning teacher modal
  const [showAssignTeacherModal, setShowAssignTeacherModal] = useState(false);
  const [selectedTeacherId, setSelectedTeacherId] = useState<string>('');

  useEffect(() => {
    fetchData();
  }, []);

  const fetchData = async () => {
    try {
      setIsLoading(true);
      const [sectionsData, coursesData, teachersData] = await Promise.all([
        sectionApi.getAllSections(), // Using the new getAllSections method that doesn't require courseId
        courseApi.getAll(),
        teacherApi.getAll()
      ]);
      
      setSections(sectionsData);
      setCourses(coursesData);
      setTeachers(teachersData);
    } catch (error) {
      console.error('Error fetching data:', error);
      setError('Failed to load data. Please try again later.');
    } finally {
      setIsLoading(false);
    }
  };

  const resetForm = () => {
    setShowForm(false);
    setFormType('add');
    setSelectedSectionId(null);
    setFormData({
      courseId: '',
      sectionName: '',
      teacherId: '',
      enrollmentKey: ''
    });
  };

  const resetAssignTeacherModal = () => {
    setShowAssignTeacherModal(false);
    setSelectedSectionId(null);
    setSelectedTeacherId('');
  };

  const handleAddNew = () => {
    setShowForm(true);
    setFormType('add');
  };

  const handleEdit = (id: number) => {
    setShowForm(true);
    setFormType('edit');
    setSelectedSectionId(id);
    
    // Populate form data based on selected section
    const section = sections.find(s => s.id === id);
    if (section) {
      setFormData({
        courseId: section.courseId.toString(),
        sectionName: section.sectionName || '',
        teacherId: section.teacherId ? section.teacherId.toString() : '',
        enrollmentKey: section.enrollmentKey
      });
    }
  };

  const handleDelete = async (id: number) => {
    if (!window.confirm('Are you sure you want to delete this section?')) {
      return;
    }
    
    try {
      await sectionApi.delete(id);
      setSections(sections.filter(s => s.id !== id));
    } catch (error) {
      console.error('Error deleting section:', error);
      setError('Failed to delete section. Please try again later.');
    }
  };

  const handleOpenAssignTeacher = (sectionId: number) => {
    setSelectedSectionId(sectionId);
    setShowAssignTeacherModal(true);
    
    // Pre-select current teacher if one is assigned
    const section = sections.find(s => s.id === sectionId);
    if (section && section.teacherId) {
      setSelectedTeacherId(section.teacherId.toString());
    } else {
      setSelectedTeacherId('');
    }
  };

  const handleAssignTeacher = async () => {
    if (!selectedSectionId || !selectedTeacherId) {
      setError('Please select a teacher to assign.');
      return;
    }
    
    try {
      await sectionApi.assignTeacher(selectedSectionId, parseInt(selectedTeacherId));
      
      // Update the sections list with the newly assigned teacher
      setSections(sections.map(section => 
        section.id === selectedSectionId 
          ? { ...section, teacherId: parseInt(selectedTeacherId) } 
          : section
      ));
      
      resetAssignTeacherModal();
    } catch (error) {
      console.error('Error assigning teacher:', error);
      setError('Failed to assign teacher. Please try again later.');
    }
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    
    try {
      if (formType === 'edit' && selectedSectionId) {
        const sectionData = {
          id: selectedSectionId,
          sectionName: formData.sectionName
        };
        
        const updatedSection = await sectionApi.update(sectionData);
        setSections(sections.map(s => s.id === selectedSectionId ? updatedSection : s));
      } else {
        const sectionData: SectionCreateDto = {
          courseId: parseInt(formData.courseId),
          sectionName: formData.sectionName || `New Section ${new Date().toISOString().slice(0, 10)}` // Fallback to date if no name provided
        };
        
        const newSection = await sectionApi.create(sectionData);
        setSections([...sections, newSection]);
      }
      
      resetForm();
    } catch (error) {
      console.error(`Error ${formType === 'add' ? 'creating' : 'updating'} section:`, error);
      setError(`Failed to ${formType === 'add' ? 'create' : 'update'} section. Please try again later.`);
    }
  };

  const handleInputChange = (e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>) => {
    const { name, value } = e.target;
    setFormData({ ...formData, [name]: value });
  };

  const getCourseName = (courseId: number) => {
    const course = courses.find(c => c.id === courseId);
    return course ? `${course.courseCode} - ${course.courseName}` : 'Unknown Course';
  };

  const getTeacherName = (teacherId?: number) => {
    if (!teacherId) return 'Not Assigned';
    const teacher = teachers.find(t => t.id === teacherId);
    return teacher ? `${teacher.firstName} ${teacher.lastName}` : 'Unknown Teacher';
  };

  if (isLoading) {
    return (
      <DashboardLayout>
        <div className="flex h-full items-center justify-center p-6">
          <div className="flex flex-col items-center space-y-4 text-center">
            <div className="animate-spin text-[#215f47]">
              <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                <path d="M21 12a9 9 0 1 1-6.219-8.56"/>
              </svg>
            </div>
            <p className="text-lg font-medium text-gray-700">Loading sections...</p>
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
              Sections & Schedules
            </h2>
            <p className="text-gray-500 mt-1">Manage academic sections and their class schedules</p>
          </div>
          
          
          <div className="mt-4 sm:mt-0">
            <Button 
              onClick={handleAddNew} 
              className="flex items-center bg-[#215f47] hover:bg-[#215f47]/90 text-white"
            >
              <Plus className="mr-2 h-4 w-4" />
              Add New Section
            </Button>
          </div>
        </div>

        <Tabs defaultValue="sections" className="w-full" onValueChange={handleTabChange}>
          <TabsList className="mb-4 grid w-full grid-cols-2 bg-[#f8f9fa]">
            <TabsTrigger 
              value="sections" 
              className="data-[state=active]:bg-[#215f47] data-[state=active]:text-white"
            >
              <BookOpen className="mr-2 h-4 w-4" />
              Sections Management
            </TabsTrigger>
            <TabsTrigger 
              value="schedules" 
              className="data-[state=active]:bg-[#215f47] data-[state=active]:text-white"
            >
              <CalendarDays className="mr-2 h-4 w-4" />
              Schedules Management
            </TabsTrigger>
          </TabsList>
        </Tabs>
        
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
              <CardTitle className="text-lg font-medium text-[#215f47]">
                {formType === 'add' ? 'Add New' : 'Edit'} Section
              </CardTitle>
              <CardDescription>
                {formType === 'add' ? 'Create a new section in your course' : 'Update section details'}
              </CardDescription>
            </CardHeader>
            
            <CardContent>
              <form onSubmit={handleSubmit} className="space-y-4">
                {formType === 'add' && (
                  <div className="space-y-2">
                    <Label htmlFor="courseId" className="text-gray-700">Course</Label>
                    <Select
                      name="courseId"
                      value={formData.courseId}
                      onValueChange={(value: string) => setFormData({ ...formData, courseId: value })}
                      required
                    >
                      <SelectTrigger className="border-[#215f47]/20 focus:ring-[#215f47]/20 focus:border-[#215f47]">
                        <SelectValue placeholder="Select a course" />
                      </SelectTrigger>
                      <SelectContent>
                        {courses.map(course => (
                          <SelectItem key={course.id} value={course.id.toString()}>
                            {course.courseCode} - {course.courseName}
                          </SelectItem>
                        ))}
                      </SelectContent>
                    </Select>
                  </div>
                )}
                
                <div className="space-y-2">
                  <Label htmlFor="sectionName" className="text-gray-700">Section Name</Label>
                  <Input
                    id="sectionName"
                    name="sectionName"
                    value={formData.sectionName}
                    onChange={handleInputChange}
                    required
                    className="border-[#215f47]/20 focus:ring-[#215f47]/20 focus:border-[#215f47]"
                    placeholder="e.g., Morning Section" 
                  />
                </div>
              </form>
            </CardContent>
            
            <CardFooter className="flex justify-end space-x-3 pt-2">
              <Button 
                variant="outline" 
                type="button" 
                onClick={resetForm}
                className="border-[#215f47]/20 text-[#215f47] hover:bg-[#215f47]/5"
              >
                Cancel
              </Button>
              <Button 
                type="button"
                onClick={handleSubmit}
                className="bg-[#215f47] hover:bg-[#215f47]/90 text-white"
              >
                {formType === 'add' ? 'Create' : 'Update'}
              </Button>
            </CardFooter>
          </Card>
        )}
        
        {/* Assign Teacher Modal */}
        <Dialog open={showAssignTeacherModal} onOpenChange={(open) => !open && resetAssignTeacherModal()}>
          <DialogContent className="bg-white">
            <DialogHeader>
              <DialogTitle className="text-[#215f47] flex items-center gap-2">
                <UserCog className="h-5 w-5" />
                Assign Teacher to Section
              </DialogTitle>
              <DialogDescription>
                Select a teacher to assign to this section
              </DialogDescription>
            </DialogHeader>
            
            <div className="py-4">
              <Label htmlFor="teacherId" className="text-gray-700 mb-2 block">Teacher</Label>
              <Select
                value={selectedTeacherId}
                onValueChange={(value: string) => setSelectedTeacherId(value)}
              >
                <SelectTrigger className="border-[#215f47]/20 focus:ring-[#215f47]/20 focus:border-[#215f47]">
                  <SelectValue placeholder="Select a teacher" />
                </SelectTrigger>
                <SelectContent>
                  {teachers.map(teacher => (
                    <SelectItem key={teacher.id} value={teacher.id.toString()}>
                      {teacher.firstName} {teacher.lastName} ({teacher.email})
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>
            
            <DialogFooter>
              <Button
                variant="outline"
                onClick={resetAssignTeacherModal}
                className="border-[#215f47]/20 text-[#215f47] hover:bg-[#215f47]/5"
              >
                Cancel
              </Button>
              <Button
                onClick={handleAssignTeacher}
                className="bg-[#215f47] hover:bg-[#215f47]/90 text-white"
              >
                Assign
              </Button>
            </DialogFooter>
          </DialogContent>
        </Dialog>
        
        <Card className="border-[#215f47]/20 shadow-sm">
          <CardHeader className="px-6 pb-2 pt-6">
            <CardTitle className="text-lg font-medium text-[#215f47] flex items-center gap-2">
              <Book className="h-5 w-5" />
              Sections List
            </CardTitle>
            <CardDescription className="text-gray-500">
              A list of all course sections in the system
            </CardDescription>
          </CardHeader>
          <CardContent className="px-6 pb-6">
            {sections.length === 0 ? (
              <div className="py-12 text-center text-gray-500 border border-dashed border-[#215f47]/20 rounded-md bg-[#215f47]/5">
                <Book className="w-12 h-12 text-[#215f47]/40 mx-auto mb-3" />
                <p className="text-sm text-gray-600">No sections found</p>
                <p className="text-xs text-gray-500 mt-1">Click "Add New Section" to create one</p>
              </div>
            ) : (
              <div className="rounded-md overflow-hidden border border-[#215f47]/20">
                <Table>
                  <TableHeader className="bg-[#215f47]/5">
                    <TableRow>
                      <TableHead className="text-[#215f47] font-medium w-[60px]">ID</TableHead>
                      <TableHead className="text-[#215f47] font-medium">Course</TableHead>
                      <TableHead className="text-[#215f47] font-medium">Section Name</TableHead>
                      <TableHead className="text-[#215f47] font-medium">Teacher</TableHead>
                      <TableHead className="text-[#215f47] font-medium">Enrollment Key</TableHead>
                      <TableHead className="text-[#215f47] font-medium text-right">Actions</TableHead>
                    </TableRow>
                  </TableHeader>
                  <TableBody>
                    {sections.map((section) => (
                      <TableRow key={section.id} className="hover:bg-[#215f47]/5 transition-colors">
                        <TableCell className="font-medium">{section.id}</TableCell>
                        <TableCell>
                          <span className="font-medium">{getCourseName(section.courseId)}</span>
                        </TableCell>
                        <TableCell>{section.sectionName}</TableCell>
                        <TableCell>
                          {section.teacherId ? (
                            <Badge variant="outline" className="bg-[#215f47]/5 text-[#215f47]">
                              {getTeacherName(section.teacherId)}
                            </Badge>
                          ) : (
                            <Badge variant="outline" className="bg-gray-100 text-gray-500">
                              Not Assigned
                            </Badge>
                          )}
                        </TableCell>
                        <TableCell>
                          {section.enrollmentKey ? (
                            <Badge variant="outline" className="bg-[#215f47]/5 text-[#215f47] font-mono">
                              {section.enrollmentKey}
                            </Badge>
                          ) : (
                            <span className="text-gray-500 text-sm">Not generated</span>
                          )}
                        </TableCell>
                        <TableCell className="text-right">
                          <Button
                            variant="ghost"
                            size="sm"
                            onClick={() => handleOpenAssignTeacher(section.id)}
                            className="h-8 py-1 px-2 text-[#215f47] hover:bg-[#215f47]/5 mr-1"
                          >
                            <UserCog className="h-4 w-4 mr-1" />
                            Assign
                          </Button>
                          <Button
                            variant="ghost"
                            size="sm"
                            onClick={() => handleEdit(section.id)}
                            className="h-8 w-8 p-0 text-[#215f47] mr-1"
                          >
                            <Pencil className="h-4 w-4" />
                          </Button>
                          <Button
                            variant="ghost"
                            size="sm"
                            onClick={() => handleDelete(section.id)}
                            className="h-8 w-8 p-0 text-red-500 hover:text-red-700 hover:bg-red-50 mr-1"
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

export default SectionManagement;
