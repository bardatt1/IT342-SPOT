import { useState, useEffect } from 'react';
import DashboardLayout from '../../components/ui/layout/DashboardLayout';
import { sectionApi, type Section, type SectionCreateDto } from '../../lib/api/section';
import { courseApi, type Course } from '../../lib/api/course';
import { teacherApi, type Teacher } from '../../lib/api/teacher';
import { Button } from '../../components/ui/button';
import { Plus, Pencil, Trash2, Users } from 'lucide-react';

const SectionManagement = () => {
  const [sections, setSections] = useState<Section[]>([]);
  const [courses, setCourses] = useState<Course[]>([]);
  const [teachers, setTeachers] = useState<Teacher[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  
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

  const handleEndSection = async (sectionId: number) => {
    if (!window.confirm('Are you sure you want to end this section? This will clear all enrollments, seats, and teacher assignments.')) {
      return;
    }
    
    try {
      await sectionApi.endSection(sectionId);
      
      // Update the section in our state
      setSections(sections.map(section => 
        section.id === sectionId 
          ? { ...section, teacherId: undefined } 
          : section
      ));
    } catch (error) {
      console.error('Error ending section:', error);
      setError('Failed to end section. Please try again later.');
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
        <div className="flex h-full items-center justify-center">
          <p className="text-lg">Loading sections...</p>
        </div>
      </DashboardLayout>
    );
  }

  return (
    <DashboardLayout>
      <div className="space-y-6">
        <div className="flex flex-col justify-between sm:flex-row sm:items-center">
          <h2 className="text-xl font-semibold">Section Management</h2>
          
          <div className="mt-4 sm:mt-0">
            <Button onClick={handleAddNew} className="flex items-center">
              <Plus className="mr-1 h-4 w-4" />
              Add New Section
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
              {formType === 'add' ? 'Add New' : 'Edit'} Section
            </h3>
            
            <form onSubmit={handleSubmit} className="space-y-4">
              {formType === 'add' && (
                <div>
                  <label htmlFor="courseId" className="block text-sm font-medium text-gray-700">
                    Course
                  </label>
                  <select
                    id="courseId"
                    name="courseId"
                    value={formData.courseId}
                    onChange={handleInputChange}
                    required
                    className="mt-1 block w-full rounded-md border border-gray-300 bg-white px-3 py-2 shadow-sm focus:border-blue-500 focus:outline-none focus:ring-blue-500 sm:text-sm"
                  >
                    <option value="">Select a course</option>
                    {courses.map(course => (
                      <option key={course.id} value={course.id}>
                        {course.courseCode} - {course.courseName}
                      </option>
                    ))}
                  </select>
                </div>
              )}
              
              <div>
                <label htmlFor="sectionName" className="block text-sm font-medium text-gray-700">
                  Section Name
                </label>
                <input
                  type="text"
                  id="sectionName"
                  name="sectionName"
                  value={formData.sectionName}
                  onChange={handleInputChange}
                  required
                  className="mt-1 block w-full rounded-md border border-gray-300 px-3 py-2 shadow-sm focus:border-blue-500 focus:outline-none focus:ring-blue-500 sm:text-sm"
                  placeholder="e.g., Morning Section" 
                />
              </div>
              
              {/* Room field removed - it's not in the backend model */}
              
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
        
        {/* Assign Teacher Modal */}
        {showAssignTeacherModal && (
          <div className="fixed inset-0 z-10 overflow-y-auto">
            <div className="flex min-h-screen items-end justify-center px-4 pb-20 pt-4 text-center sm:block sm:p-0">
              <div className="fixed inset-0 transition-opacity" aria-hidden="true">
                <div className="absolute inset-0 bg-gray-500 opacity-75"></div>
              </div>
              
              <span className="hidden sm:inline-block sm:h-screen sm:align-middle" aria-hidden="true">&#8203;</span>
              
              <div className="inline-block transform overflow-hidden rounded-lg bg-white text-left align-bottom shadow-xl transition-all sm:my-8 sm:w-full sm:max-w-lg sm:align-middle">
                <div className="bg-white px-4 pb-4 pt-5 sm:p-6 sm:pb-4">
                  <div className="sm:flex sm:items-start">
                    <div className="mx-auto flex h-12 w-12 flex-shrink-0 items-center justify-center rounded-full bg-blue-100 sm:mx-0 sm:h-10 sm:w-10">
                      <Users className="h-6 w-6 text-blue-600" />
                    </div>
                    <div className="mt-3 text-center sm:ml-4 sm:mt-0 sm:text-left">
                      <h3 className="text-lg font-medium leading-6 text-gray-900">
                        Assign Teacher to Section
                      </h3>
                      <div className="mt-2">
                        <p className="text-sm text-gray-500">
                          Select a teacher to assign to this section
                        </p>
                      </div>
                      
                      <div className="mt-4">
                        <select
                          value={selectedTeacherId}
                          onChange={(e) => setSelectedTeacherId(e.target.value)}
                          className="mt-1 block w-full rounded-md border border-gray-300 bg-white px-3 py-2 shadow-sm focus:border-blue-500 focus:outline-none focus:ring-blue-500 sm:text-sm"
                        >
                          <option value="">Select a teacher</option>
                          {teachers.map(teacher => (
                            <option key={teacher.id} value={teacher.id}>
                              {teacher.firstName} {teacher.lastName} ({teacher.email})
                            </option>
                          ))}
                        </select>
                      </div>
                    </div>
                  </div>
                </div>
                
                <div className="bg-gray-50 px-4 py-3 sm:flex sm:flex-row-reverse sm:px-6">
                  <Button
                    onClick={handleAssignTeacher}
                    className="inline-flex w-full justify-center sm:ml-3 sm:w-auto"
                  >
                    Assign
                  </Button>
                  <Button
                    variant="outline"
                    onClick={resetAssignTeacherModal}
                    className="mt-3 inline-flex w-full justify-center sm:mt-0 sm:w-auto"
                  >
                    Cancel
                  </Button>
                </div>
              </div>
            </div>
          </div>
        )}
        
        <div className="overflow-hidden rounded-lg bg-white shadow">
          {sections.length === 0 ? (
            <div className="p-6 text-center text-gray-500">
              No sections found. Click "Add New Section" to create one.
            </div>
          ) : (
            <div className="overflow-x-auto">
              <table className="min-w-full divide-y divide-gray-200">
                <thead className="bg-gray-50">
                  <tr>
                    <th className="px-6 py-3 text-left text-xs font-medium uppercase tracking-wider text-gray-500">
                      ID
                    </th>
                    <th className="px-6 py-3 text-left text-xs font-medium uppercase tracking-wider text-gray-500">
                      Course
                    </th>
                    <th className="px-6 py-3 text-left text-xs font-medium uppercase tracking-wider text-gray-500">
                      Section Name
                    </th>
                    <th className="px-6 py-3 text-left text-xs font-medium uppercase tracking-wider text-gray-500">
                      Teacher
                    </th>
                    <th className="px-6 py-3 text-left text-xs font-medium uppercase tracking-wider text-gray-500">
                      Enrollment Key
                    </th>
                    <th className="px-6 py-3 text-right text-xs font-medium uppercase tracking-wider text-gray-500">
                      Actions
                    </th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-gray-200 bg-white">
                  {sections.map((section) => (
                    <tr key={section.id}>
                      <td className="whitespace-nowrap px-6 py-4 text-sm font-medium text-gray-900">
                        {section.id}
                      </td>
                      <td className="whitespace-nowrap px-6 py-4 text-sm text-gray-500">
                        {getCourseName(section.courseId)}
                      </td>
                      <td className="whitespace-nowrap px-6 py-4 text-sm text-gray-500">
                        {section.sectionName}
                      </td>
                      <td className="whitespace-nowrap px-6 py-4 text-sm text-gray-500">
                        {getTeacherName(section.teacherId)}
                      </td>
                      <td className="whitespace-nowrap px-6 py-4 text-sm text-gray-500">
                        {section.enrollmentKey || 'Not generated'}
                      </td>
                      <td className="whitespace-nowrap px-6 py-4 text-right text-sm font-medium">
                        <Button
                          variant="ghost"
                          size="sm"
                          onClick={() => handleOpenAssignTeacher(section.id)}
                          className="mr-2 text-blue-600 hover:text-blue-900"
                        >
                          Assign Teacher
                        </Button>
                        <Button
                          variant="ghost"
                          size="icon"
                          onClick={() => handleEdit(section.id)}
                          className="text-blue-600 hover:text-blue-900"
                        >
                          <Pencil className="h-4 w-4" />
                        </Button>
                        <Button
                          variant="ghost"
                          size="icon"
                          onClick={() => handleDelete(section.id)}
                          className="text-red-600 hover:text-red-900"
                        >
                          <Trash2 className="h-4 w-4" />
                        </Button>
                        <Button
                          variant="ghost"
                          size="sm"
                          onClick={() => handleEndSection(section.id)}
                          className="ml-2 text-yellow-600 hover:text-yellow-900"
                        >
                          End Section
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

export default SectionManagement;
