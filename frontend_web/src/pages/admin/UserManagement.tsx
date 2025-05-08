import { useState, useEffect } from 'react';
import DashboardLayout from '../../components/ui/layout/DashboardLayout';
import { studentApi, type Student } from '../../lib/api/student';
import { teacherApi, type Teacher } from '../../lib/api/teacher';
import { Button } from '../../components/ui/button';
import { Input } from '../../components/ui/input';
import { Label } from '../../components/ui/label';
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from '../../components/ui/card';
import { Badge } from '../../components/ui/badge';
import { Alert, AlertDescription } from '../../components/ui/alert';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '../../components/ui/tabs';
import { Plus, Pencil, Trash2, UserCog, Users, AlertTriangle, X } from 'lucide-react';
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '../../components/ui/table';

const UserManagement = () => {
  const [activeTab, setActiveTab] = useState<'students' | 'teachers'>('students');
  const [students, setStudents] = useState<Student[]>([]);
  const [teachers, setTeachers] = useState<Teacher[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  
  // No need for separate error dialog state, we'll use the existing error state
  
  // Form state for adding new users
  const [showForm, setShowForm] = useState(false);
  const [formType, setFormType] = useState<'add' | 'edit'>('add');
  const [selectedUserId, setSelectedUserId] = useState<number | null>(null);
  
  const [formData, setFormData] = useState({
    firstName: '',
    middleName: '',
    lastName: '',
    email: '',
    physicalId: '',
    year: '',
    program: '',
    password: ''
  });

  // Function to generate a secure temporary password based on physical ID
  const generateSecurePassword = (physicalId: string): string => {
    // Create temporary password using first 5 digits of physical ID + "TEMP"
    // Plus a random 3-digit number for added security
    const randomDigits = Math.floor(Math.random() * 900 + 100).toString();
    const plainPassword = physicalId.substring(0, 5).toUpperCase() + 'TEMP' + randomDigits;
    console.log(`Creating temporary password for ID: ${physicalId}`);
    
    // Note: The actual password hashing happens on the backend for security
    return plainPassword;
  };

  useEffect(() => {
    fetchData();
  }, [activeTab]);

  const fetchData = async () => {
    try {
      setIsLoading(true);
      if (activeTab === 'students') {
        const data = await studentApi.getAll();
        setStudents(data);
      } else {
        const data = await teacherApi.getAll();
        setTeachers(data);
      }
    } catch (error) {
      console.error(`Error fetching ${activeTab}:`, error);
      setError(`Failed to load ${activeTab}. Please try again later.`);
    } finally {
      setIsLoading(false);
    }
  };

  const handleTabChange = (tab: 'students' | 'teachers') => {
    setActiveTab(tab);
    resetForm();
  };

  const resetForm = () => {
    setShowForm(false);
    setFormType('add');
    setSelectedUserId(null);
    setFormData({
      firstName: '',
      middleName: '',
      lastName: '',
      email: '',
      physicalId: '',
      year: '',
      program: '',
      password: ''
    });
  };

  const handleAddNew = () => {
    setShowForm(true);
    setFormType('add');
  };

  const handleEdit = (id: number) => {
    setShowForm(true);
    setFormType('edit');
    setSelectedUserId(id);
    
    // Populate form data based on selected user
    if (activeTab === 'students') {
      const student = students.find(s => s.id === id);
      if (student) {
        setFormData({
          firstName: student.firstName,
          middleName: student.middleName || '',
          lastName: student.lastName,
          email: student.email,
          physicalId: student.studentPhysicalId,
          year: student.year,
          program: student.program,
          password: '' // Don't populate password for security
        });
      }
    } else {
      const teacher = teachers.find(t => t.id === id);
      if (teacher) {
        setFormData({
          firstName: teacher.firstName,
          middleName: teacher.middleName || '',
          lastName: teacher.lastName,
          email: teacher.email,
          physicalId: teacher.teacherPhysicalId,
          year: '',
          program: '',
          password: '' // Don't populate password for security
        });
      }
    }
  };

  const handleDelete = async (id: number) => {
    if (!window.confirm(`Are you sure you want to delete this ${activeTab === 'students' ? 'student' : 'teacher'}?`)) {
      return;
    }
    
    try {
      if (activeTab === 'students') {
        await studentApi.delete(id);
        setStudents(students.filter(s => s.id !== id));
      } else {
        await teacherApi.delete(id);
        setTeachers(teachers.filter(t => t.id !== id));
      }
    } catch (error: any) {
      console.error(`Error deleting ${activeTab === 'students' ? 'student' : 'teacher'}:`, error);
      
      // Check for database constraint error
      if (error?.response?.data?.message && error.response.data.message.includes('constraint')) {
        const userType = activeTab === 'students' ? 'student' : 'teacher';
        
        // Create a more user-friendly error message
        let friendlyMessage = `Cannot delete this ${userType}. They have existing records in the system.`;
        
        if (activeTab === 'students') {
          friendlyMessage += ' The student may be enrolled in one or more courses or have attendance records.';
        } else {
          friendlyMessage += ' The teacher may be assigned to one or more sections or courses.';
        }
        
        friendlyMessage += ' Please remove these associations before attempting to delete this record.';
        
        // Use the existing error alert component
        setError(friendlyMessage);
      } else {
        // Generic error message for other types of errors
        setError(`Failed to delete. Please try again later.`);
      }
    }
  };

  // Validation helper functions
  const validatePassword = (password: string): { valid: boolean; message: string } => {
    if (!password) {
      return { valid: false, message: 'Password is required' };
    }
    if (password.length < 8) {
      return { valid: false, message: 'Password must be at least 8 characters long' };
    }
    // Add more password requirements as needed
    return { valid: true, message: '' };
  };
  
  const validatePhysicalId = (physicalId: string): { valid: boolean; message: string } => {
    if (!physicalId) {
      return { valid: false, message: 'Physical ID is required' };
    }
    if (physicalId.length < 5) {
      return { valid: false, message: 'Physical ID must be at least 5 characters long' };
    }
    return { valid: true, message: '' };
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null); // Clear any previous errors
    
    // Validate physical ID for all submissions
    const physicalIdValidation = validatePhysicalId(formData.physicalId);
    if (!physicalIdValidation.valid) {
      setError(physicalIdValidation.message);
      return;
    }
    
    // Validate password for new user creation with custom password
    if (formType === 'add' && formData.password) {
      const passwordValidation = validatePassword(formData.password);
      if (!passwordValidation.valid) {
        setError(passwordValidation.message);
        return;
      }
    }
    
    try {
      if (activeTab === 'students') {
        if (formType === 'edit' && selectedUserId) {
          const studentUpdateData = {
            id: selectedUserId,
            firstName: formData.firstName,
            middleName: formData.middleName || null,
            lastName: formData.lastName,
            email: formData.email,
            studentPhysicalId: formData.physicalId,
            year: formData.year,
            program: formData.program,
            ...(formData.password ? { password: formData.password } : {})
          };
          
          await studentApi.update(studentUpdateData);
          setStudents(students.map(s => s.id === selectedUserId ? { ...s, ...studentUpdateData } : s));
        } else {
          // For new student creation, only the physical ID is required
          // The rest will be filled in by the student on first login
          const studentCreateData = {
            firstName: formData.firstName || 'Temporary', // Placeholder values for required backend fields
            middleName: formData.middleName || null,
            lastName: formData.lastName || 'Account',
            email: formData.email || `${formData.physicalId}@edu-spot.me`, // Generate temporary email if not provided
            studentPhysicalId: formData.physicalId, // This is the only required field
            year: formData.year || '1',
            program: formData.program || 'Temporary Program',
            // Generate a hashed password based on the physical ID
            password: formData.password || generateSecurePassword(formData.physicalId)
          };
          
          // Call the create student API function
          console.log('Creating student:', studentCreateData);
          await studentApi.create(studentCreateData);
        }
      } else {
        if (formType === 'edit' && selectedUserId) {
          const teacherUpdateData = {
            id: selectedUserId,
            firstName: formData.firstName,
            middleName: formData.middleName || null,
            lastName: formData.lastName,
            email: formData.email,
            teacherPhysicalId: formData.physicalId,
            ...(formData.password ? { password: formData.password } : {})
          };
          
          await teacherApi.update(teacherUpdateData);
          setTeachers(teachers.map(t => t.id === selectedUserId ? { ...t, ...teacherUpdateData } : t));
        } else {
          // For new teacher creation, only the physical ID is required
          // The rest will be filled in by the teacher on first login
          const teacherCreateData = {
            firstName: formData.firstName || 'Temporary', // Placeholder values for required backend fields
            middleName: formData.middleName || null,
            lastName: formData.lastName || 'Account',
            email: formData.email || `${formData.physicalId}@edu-spot.me`, // Generate temporary email if not provided
            teacherPhysicalId: formData.physicalId, // This is the only required field
            // Generate a hashed password based on the physical ID
            password: formData.password || generateSecurePassword(formData.physicalId)
          };
          
          // Call the create teacher API function
          console.log('Creating teacher:', teacherCreateData);
          await teacherApi.create(teacherCreateData);
        }
      }
      
      resetForm();
      await fetchData();
    } catch (error) {
      console.error(`Error ${formType === 'add' ? 'creating' : 'updating'} ${activeTab}:`, error);
      setError(`Failed to ${formType === 'add' ? 'create' : 'update'} ${activeTab}. Please try again later.`);
    }
  };

  const handleInputChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target;
    setFormData({ ...formData, [name]: value });
  };

  if (isLoading) {
    return (
      <DashboardLayout>
        <div className="flex h-full items-center justify-center">
          <div className="flex items-center space-x-2">
            <div className="h-4 w-4 animate-spin rounded-full border-2 border-[#215f47] border-t-transparent"></div>
            <p className="text-lg font-medium text-[#215f47]">Loading user data...</p>
          </div>
        </div>
      </DashboardLayout>
    );
  }

  return (
    <DashboardLayout>
      <div className="space-y-6 p-6">
        {/* Error alert component */}
        {error && (
          <Alert variant="destructive" className="mb-4 border-red-600 text-red-600 bg-red-50">
            <AlertTriangle className="h-4 w-4" />
            <AlertDescription>{error}</AlertDescription>
            <Button 
              variant="ghost" 
              size="icon" 
              onClick={() => setError(null)} 
              className="h-6 w-6 ml-auto"
            >
              <X className="h-3 w-3" />
            </Button>
          </Alert>
        )}
        
        <div className="flex flex-col sm:flex-row sm:items-center justify-between mb-6">
          <div>
          <h2 className="text-2xl font-bold text-[#215f47] flex items-center gap-2">
            <UserCog className="h-6 w-6" />
            User Management
          </h2>
          <p className="text-sm text-gray-500 mt-1">Manage student and teacher accounts</p>
        </div>
        
        <div className="mt-4 flex space-x-3 sm:mt-0">
          <Button 
            onClick={handleAddNew} 
            className="bg-[#215f47] hover:bg-[#215f47]/90 text-white gap-2 py-2"
          >
            <Plus className="h-4 w-4" />
            Add New {activeTab === 'students' ? 'Student' : 'Teacher'}
          </Button>
          </div>
        </div>
      

        
        {showForm && (
          <Card className="border-[#215f47]/20 shadow-sm mb-6">
            <CardHeader className="pb-3">
              <div className="flex justify-between items-center">
                <div>
                  <CardTitle className="text-[#215f47]">
                    {formType === 'add' ? `Add New ${activeTab === 'students' ? 'Student' : 'Teacher'}` : `Edit ${activeTab === 'students' ? 'Student' : 'Teacher'}`}
                  </CardTitle>
                  <CardDescription>
                    {formType === 'add' 
                      ? `Add a new ${activeTab === 'students' ? 'student' : 'teacher'} to the system` 
                      : `Update existing ${activeTab === 'students' ? 'student' : 'teacher'} information`}
                  </CardDescription>
                </div>
                <Button 
                  variant="ghost" 
                  size="icon" 
                  onClick={resetForm} 
                  className="h-8 w-8 rounded-full"
                >
                  <X className="h-4 w-4" />
                </Button>
              </div>
            </CardHeader>
            <CardContent>
              <form onSubmit={handleSubmit} className="space-y-4">
                <div className="grid grid-cols-1 gap-4 md:grid-cols-2">
                  <div className="space-y-2">
                    <Label htmlFor="firstName" className="text-gray-700">First Name</Label>
                    <Input
                      id="firstName"
                      type="text"
                      name="firstName"
                      value={formData.firstName}
                      onChange={handleInputChange}
                      className="border-[#215f47]/20 focus:border-[#215f47] focus:ring-2 focus:ring-[#215f47]/20"
                    />
                  </div>
                  
                  <div className="space-y-2">
                    <Label htmlFor="middleName" className="text-gray-700">Middle Name</Label>
                    <Input
                      id="middleName"
                      type="text"
                      name="middleName"
                      value={formData.middleName}
                      onChange={handleInputChange}
                      className="border-[#215f47]/20 focus:border-[#215f47] focus:ring-2 focus:ring-[#215f47]/20"
                    />
                  </div>
                  
                  <div className="space-y-2">
                    <Label htmlFor="lastName" className="text-gray-700">Last Name</Label>
                    <Input
                      id="lastName"
                      type="text"
                      name="lastName"
                      value={formData.lastName}
                      onChange={handleInputChange}
                      className="border-[#215f47]/20 focus:border-[#215f47] focus:ring-2 focus:ring-[#215f47]/20"
                    />
                  </div>
                  
                  <div className="space-y-2">
                    <Label htmlFor="email" className="text-gray-700">Email</Label>
                    <Input
                      id="email"
                      type="email"
                      name="email"
                      value={formData.email}
                      onChange={handleInputChange}
                      placeholder="Leave empty for temporary email"
                      className="border-[#215f47]/20 focus:border-[#215f47] focus:ring-2 focus:ring-[#215f47]/20"
                    />
                  </div>
                  
                  <div className="space-y-2">
                    <div className="flex items-center justify-between">
                      <Label htmlFor="physicalId" className="text-gray-700">Physical ID</Label>
                      <Badge variant="outline" className="bg-[#215f47]/5 text-[#215f47] text-xs">Required</Badge>
                    </div>
                    <Input
                      id="physicalId"
                      type="text"
                      name="physicalId"
                      value={formData.physicalId}
                      onChange={handleInputChange}
                      required
                      className="border-[#215f47]/20 focus:border-[#215f47] focus:ring-2 focus:ring-[#215f47]/20"
                    />
                  </div>
                  
                  <div className="space-y-2">
                    <Label htmlFor="password" className="text-gray-700">Password</Label>
                    <Input
                      id="password"
                      type="password"
                      name="password"
                      value={formData.password}
                      onChange={(e) => {
                        handleInputChange(e);
                        // Clear password error when user changes input
                        if (error && error.includes('Password')) setError(null);
                      }}
                      placeholder="Leave empty for auto-generated password"
                      className="border-[#215f47]/20 focus:border-[#215f47] focus:ring-2 focus:ring-[#215f47]/20"
                    />
                  </div>
                  
                  {activeTab === 'students' && (
                    <>
                      <div className="space-y-2">
                        <Label htmlFor="year" className="text-gray-700">Year</Label>
                        <Input
                          id="year"
                          type="text"
                          name="year"
                          value={formData.year}
                          onChange={handleInputChange}
                          className="border-[#215f47]/20 focus:border-[#215f47] focus:ring-2 focus:ring-[#215f47]/20"
                        />
                      </div>
                      
                      <div className="space-y-2">
                        <Label htmlFor="program" className="text-gray-700">Program</Label>
                        <Input
                          id="program"
                          type="text"
                          name="program"
                          value={formData.program}
                          onChange={handleInputChange}
                          className="border-[#215f47]/20 focus:border-[#215f47] focus:ring-2 focus:ring-[#215f47]/20"
                        />
                      </div>
                    </>
                  )}
                </div>
                <div className="flex justify-end space-x-3 pt-6">
                  <Button
                    type="button"
                    onClick={resetForm}
                    variant="outline"
                    className="border-[#215f47]/20 text-gray-600 hover:text-[#215f47] hover:border-[#215f47]/30"
                  >
                    Cancel
                  </Button>
                  <Button
                    type="submit"
                    className="bg-[#215f47] hover:bg-[#215f47]/90 text-white"
                  >
                    {formType === 'add' ? 'Add' : 'Update'}
                  </Button>
                </div>
              </form>
            </CardContent>
          </Card>
        )}
        
        <Tabs defaultValue="students" value={activeTab} onValueChange={(value: string) => handleTabChange(value as 'students' | 'teachers')} className="space-y-4">
          <TabsList className="bg-[#215f47]/10 p-1">
            <TabsTrigger 
              value="students" 
              className="data-[state=active]:bg-[#215f47] data-[state=active]:text-white data-[state=active]:shadow-sm rounded-md"
            >
              <Users className="mr-2 h-4 w-4" />
              Students
            </TabsTrigger>
            <TabsTrigger 
              value="teachers" 
              className="data-[state=active]:bg-[#215f47] data-[state=active]:text-white data-[state=active]:shadow-sm rounded-md"
            >
              <UserCog className="mr-2 h-4 w-4" />
              Teachers
            </TabsTrigger>
          </TabsList>

          <TabsContent value="students" className="space-y-4">
            {/* Students List */}
            <Card className="border-[#215f47]/20 shadow-sm">
              <CardHeader className="px-6 pb-2 pt-6">
                <CardTitle className="text-lg font-medium text-[#215f47] flex items-center gap-2">
                  <Users className="h-5 w-5" />
                  Students List
                </CardTitle>
                <CardDescription className="text-gray-500">
                  A list of all students in the system
                </CardDescription>
              </CardHeader>
              <CardContent className="px-6 pb-6">
                <div className="rounded-md overflow-hidden border border-[#215f47]/20">
                  <Table>
                    <TableHeader className="bg-[#215f47]/5">
                      <TableRow>
                        <TableHead className="text-[#215f47] font-medium">Name</TableHead>
                        <TableHead className="text-[#215f47] font-medium">Email</TableHead>
                        <TableHead className="text-[#215f47] font-medium">Physical ID</TableHead>
                        <TableHead className="text-[#215f47] font-medium">Year</TableHead>
                        <TableHead className="text-[#215f47] font-medium">Program</TableHead>
                        <TableHead className="text-[#215f47] font-medium text-right w-[100px]">Actions</TableHead>
                      </TableRow>
                    </TableHeader>
                    <TableBody>
                      {students.length > 0 ? (
                        students.map((student) => (
                          <TableRow key={student.id} className="hover:bg-[#215f47]/5 transition-colors">
                            <TableCell>
                              {`${student.firstName} ${student.middleName ? student.middleName + ' ' : ''}${student.lastName}`}
                            </TableCell>
                            <TableCell>{student.email}</TableCell>
                            <TableCell>
                              <Badge variant="outline" className="bg-[#215f47]/5 text-[#215f47] font-mono">
                                {student.studentPhysicalId}
                              </Badge>
                            </TableCell>
                            <TableCell>{student.year}</TableCell>
                            <TableCell>{student.program}</TableCell>
                            <TableCell className="text-right">
                              <Button
                                variant="ghost"
                                size="sm"
                                onClick={() => handleEdit(student.id)}
                                className="h-8 w-8 p-0 text-[#215f47] mr-1"
                              >
                                <Pencil className="h-4 w-4" />
                              </Button>
                              <Button
                                variant="ghost"
                                size="sm"
                                onClick={() => handleDelete(student.id)}
                                className="h-8 w-8 p-0 text-red-500 hover:text-red-700 hover:bg-red-50"
                              >
                                <Trash2 className="h-4 w-4" />
                              </Button>
                            </TableCell>
                          </TableRow>
                        ))
                      ) : (
                        <TableRow>
                          <TableCell colSpan={7} className="h-24 text-center text-muted-foreground">
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

          <TabsContent value="teachers" className="space-y-4">
            {/* Teachers List */}
            <Card className="border-[#215f47]/20 shadow-sm">
              <CardHeader className="px-6 pb-2 pt-6">
                <CardTitle className="text-lg font-medium text-[#215f47] flex items-center gap-2">
                  <UserCog className="h-5 w-5" />
                  Teachers List
                </CardTitle>
                <CardDescription className="text-gray-500">
                  A list of all teachers in the system
                </CardDescription>
              </CardHeader>
              <CardContent className="px-6 pb-6">
                <div className="rounded-md overflow-hidden border border-[#215f47]/20">
                  <Table>
                    <TableHeader className="bg-[#215f47]/5">
                      <TableRow>
                        <TableHead className="text-[#215f47] font-medium">Name</TableHead>
                        <TableHead className="text-[#215f47] font-medium">Email</TableHead>
                        <TableHead className="text-[#215f47] font-medium">Physical ID</TableHead>
                        <TableHead className="text-[#215f47] font-medium text-right w-[100px]">Actions</TableHead>
                      </TableRow>
                    </TableHeader>
                    <TableBody>
                      {teachers.length > 0 ? (
                        teachers.map((teacher) => (
                          <TableRow key={teacher.id} className="hover:bg-[#215f47]/5 transition-colors">
                            <TableCell>
                              {`${teacher.firstName} ${teacher.middleName ? teacher.middleName + ' ' : ''}${teacher.lastName}`}
                            </TableCell>
                            <TableCell>{teacher.email}</TableCell>
                            <TableCell>
                              <Badge variant="outline" className="bg-[#215f47]/5 text-[#215f47] font-mono">
                                {teacher.teacherPhysicalId}
                              </Badge>
                            </TableCell>
                            <TableCell className="text-right">
                              <Button
                                variant="ghost"
                                size="sm"
                                onClick={() => handleEdit(teacher.id)}
                                className="h-8 w-8 p-0 text-[#215f47] mr-1"
                              >
                                <Pencil className="h-4 w-4" />
                              </Button>
                              <Button
                                variant="ghost"
                                size="sm"
                                onClick={() => handleDelete(teacher.id)}
                                className="h-8 w-8 p-0 text-red-500 hover:text-red-700 hover:bg-red-50"
                              >
                                <Trash2 className="h-4 w-4" />
                              </Button>
                            </TableCell>
                          </TableRow>
                        ))
                      ) : (
                        <TableRow>
                          <TableCell colSpan={5} className="h-24 text-center text-muted-foreground">
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
        </Tabs>
      </div>
    </DashboardLayout>
  );
};

export default UserManagement;
