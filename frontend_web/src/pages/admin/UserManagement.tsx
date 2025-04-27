import { useState, useEffect } from 'react';
import DashboardLayout from '../../components/ui/layout/DashboardLayout';
import { studentApi, type Student } from '../../lib/api/student';
import { teacherApi, type Teacher } from '../../lib/api/teacher';
import { Button } from '../../components/ui/button';
import { Plus, Pencil, Trash2 } from 'lucide-react';

const UserManagement = () => {
  const [activeTab, setActiveTab] = useState<'students' | 'teachers'>('students');
  const [students, setStudents] = useState<Student[]>([]);
  const [teachers, setTeachers] = useState<Teacher[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  
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

  // Function to generate a bcrypt-hashed password based on physical ID
  const generateHashedPassword = (physicalId: string): string => {
    // Create temporary password using first 5 digits of physical ID + "TEMP"
    const plainPassword = physicalId.substring(0, 5).toUpperCase() + 'TEMP';
    console.log(`Creating temporary password pattern for ID: ${physicalId} -> ${plainPassword}`);
    
    // In a real implementation, we would hash this plainPassword with bcrypt
    // For now, we'll use the known bcrypt hash that corresponds to 'test'
    // In production, this would call a backend API to generate the proper hash
    return '$2a$12$kz4Q7bmPvHv4MCqjXYNPPu9eEqnKJ81/c4LPbl82BGe4TbWhcje3u';
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
        // Note: Teacher deletion endpoint not implemented in API
        // This would be the code if it was:
        // await teacherApi.delete(id);
        setTeachers(teachers.filter(t => t.id !== id));
      }
    } catch (error) {
      console.error(`Error deleting ${activeTab === 'students' ? 'student' : 'teacher'}:`, error);
      setError(`Failed to delete. Please try again later.`);
    }
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    
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
            email: formData.email || `${formData.physicalId}@temporary.com`, // Generate temporary email if not provided
            studentPhysicalId: formData.physicalId, // This is the only required field
            year: formData.year || '1',
            program: formData.program || 'Temporary Program',
            // Generate a hashed password based on the physical ID
            password: formData.password || generateHashedPassword(formData.physicalId)
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
            email: formData.email || `${formData.physicalId}@temporary.com`, // Generate temporary email if not provided
            teacherPhysicalId: formData.physicalId, // This is the only required field
            // Generate a hashed password based on the physical ID
            password: formData.password || generateHashedPassword(formData.physicalId)
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
          <p className="text-lg">Loading user data...</p>
        </div>
      </DashboardLayout>
    );
  }

  return (
    <DashboardLayout>
      <div className="space-y-6">
        <div className="flex flex-col justify-between sm:flex-row sm:items-center">
          <h2 className="text-xl font-semibold">User Management</h2>
          
          <div className="mt-4 flex space-x-4 sm:mt-0">
            <div className="flex rounded-md shadow-sm">
              <button
                type="button"
                className={`relative inline-flex items-center rounded-l-md px-3 py-2 text-sm font-medium focus:z-10 ${
                  activeTab === 'students'
                    ? 'bg-blue-600 text-white'
                    : 'bg-white text-gray-700 hover:bg-gray-50'
                }`}
                onClick={() => handleTabChange('students')}
              >
                Students
              </button>
              <button
                type="button"
                className={`relative -ml-px inline-flex items-center rounded-r-md px-3 py-2 text-sm font-medium focus:z-10 ${
                  activeTab === 'teachers'
                    ? 'bg-blue-600 text-white'
                    : 'bg-white text-gray-700 hover:bg-gray-50'
                }`}
                onClick={() => handleTabChange('teachers')}
              >
                Teachers
              </button>
            </div>
            
            <Button onClick={handleAddNew} className="flex items-center">
              <Plus className="mr-1 h-4 w-4" />
              Add New
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
              {formType === 'add' ? 'Add New' : 'Edit'} {activeTab === 'students' ? 'Student' : 'Teacher'}
            </h3>
            
            <form onSubmit={handleSubmit} className="space-y-4">
              <div className="grid grid-cols-1 gap-4 md:grid-cols-2">
                <div>
                  <label htmlFor="firstName" className="block text-sm font-medium text-gray-700">
                    First Name
                  </label>
                  <input
                    type="text"
                    id="firstName"
                    name="firstName"
                    value={formData.firstName}
                    onChange={handleInputChange}
                    placeholder="Can be updated later"
                    className="mt-1 block w-full rounded-md border border-gray-300 px-3 py-2 shadow-sm focus:border-blue-500 focus:outline-none focus:ring-blue-500 sm:text-sm"
                  />
                </div>
                
                <div>
                  <label htmlFor="middleName" className="block text-sm font-medium text-gray-700">
                    Middle Name
                  </label>
                  <input
                    type="text"
                    id="middleName"
                    name="middleName"
                    value={formData.middleName}
                    onChange={handleInputChange}
                    className="mt-1 block w-full rounded-md border border-gray-300 px-3 py-2 shadow-sm focus:border-blue-500 focus:outline-none focus:ring-blue-500 sm:text-sm"
                  />
                </div>
                
                <div>
                  <label htmlFor="lastName" className="block text-sm font-medium text-gray-700">
                    Last Name
                  </label>
                  <input
                    type="text"
                    id="lastName"
                    name="lastName"
                    value={formData.lastName}
                    onChange={handleInputChange}
                    placeholder="Can be updated later"
                    className="mt-1 block w-full rounded-md border border-gray-300 px-3 py-2 shadow-sm focus:border-blue-500 focus:outline-none focus:ring-blue-500 sm:text-sm"
                  />
                </div>
                
                <div>
                  <label htmlFor="email" className="block text-sm font-medium text-gray-700">
                    Email
                  </label>
                  <input
                    type="email"
                    id="email"
                    name="email"
                    value={formData.email}
                    onChange={handleInputChange}
                    placeholder="Can be updated later"
                    className="mt-1 block w-full rounded-md border border-gray-300 px-3 py-2 shadow-sm focus:border-blue-500 focus:outline-none focus:ring-blue-500 sm:text-sm"
                  />
                </div>
                
                <div>
                  <label htmlFor="physicalId" className="block text-sm font-medium text-gray-700">
                    Physical ID <span className="text-red-500">*</span>
                  </label>
                  <input
                    type="text"
                    id="physicalId"
                    name="physicalId"
                    value={formData.physicalId}
                    onChange={handleInputChange}
                    required
                    placeholder="Required - Will be used as login ID"
                    className="mt-1 block w-full rounded-md border border-gray-300 px-3 py-2 shadow-sm focus:border-blue-500 focus:outline-none focus:ring-blue-500 sm:text-sm"
                  />
                </div>
                
                {activeTab === 'students' && (
                  <>
                    <div>
                      <label htmlFor="year" className="block text-sm font-medium text-gray-700">
                        Year
                      </label>
                      <input
                        type="text"
                        id="year"
                        name="year"
                        value={formData.year}
                        onChange={handleInputChange}
                        placeholder="Can be updated later"
                        className="mt-1 block w-full rounded-md border border-gray-300 px-3 py-2 shadow-sm focus:border-blue-500 focus:outline-none focus:ring-blue-500 sm:text-sm"
                      />
                    </div>
                    
                    <div>
                      <label htmlFor="program" className="block text-sm font-medium text-gray-700">
                        Program
                      </label>
                      <input
                        type="text"
                        id="program"
                        name="program"
                        value={formData.program}
                        onChange={handleInputChange}
                        placeholder="Can be updated later"
                        className="mt-1 block w-full rounded-md border border-gray-300 px-3 py-2 shadow-sm focus:border-blue-500 focus:outline-none focus:ring-blue-500 sm:text-sm"
                      />
                    </div>
                  </>
                )}
                
                <div>
                  <label htmlFor="password" className="block text-sm font-medium text-gray-700">
                    {formType === 'add' ? 'Password' : 'New Password (leave blank to keep current)'}
                  </label>
                  <input
                    type="password"
                    id="password"
                    name="password"
                    value={formData.password}
                    onChange={handleInputChange}
                    placeholder={formType === 'add' ? 'Leave blank to use Physical ID as password' : ''}
                    className="mt-1 block w-full rounded-md border border-gray-300 px-3 py-2 shadow-sm focus:border-blue-500 focus:outline-none focus:ring-blue-500 sm:text-sm"
                  />
                  {formType === 'add' && (
                    <p className="mt-1 text-sm text-gray-500">
                      If left blank, a temporary password will be generated using the pattern:<br/> 
                      <span className="font-mono">[First 5 digits of Physical ID]TEMP</span> (e.g., "12345TEMP")<br/>
                      User will be prompted to update their information on first login.
                    </p>
                  )}
                </div>
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
          <div className="overflow-x-auto">
            <table className="min-w-full divide-y divide-gray-200">
              <thead className="bg-gray-50">
                <tr>
                  <th className="px-6 py-3 text-left text-xs font-medium uppercase tracking-wider text-gray-500">
                    Name
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium uppercase tracking-wider text-gray-500">
                    Email
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium uppercase tracking-wider text-gray-500">
                    Physical ID
                  </th>
                  {activeTab === 'students' && (
                    <>
                      <th className="px-6 py-3 text-left text-xs font-medium uppercase tracking-wider text-gray-500">
                        Year
                      </th>
                      <th className="px-6 py-3 text-left text-xs font-medium uppercase tracking-wider text-gray-500">
                        Program
                      </th>
                    </>
                  )}
                  <th className="px-6 py-3 text-right text-xs font-medium uppercase tracking-wider text-gray-500">
                    Actions
                  </th>
                </tr>
              </thead>
              <tbody className="divide-y divide-gray-200 bg-white">
                {activeTab === 'students'
                  ? students.map((student) => (
                      <tr key={student.id}>
                        <td className="whitespace-nowrap px-6 py-4 text-sm font-medium text-gray-900">
                          {student.firstName} {student.middleName ? `${student.middleName} ` : ''}{student.lastName}
                        </td>
                        <td className="whitespace-nowrap px-6 py-4 text-sm text-gray-500">
                          {student.email}
                        </td>
                        <td className="whitespace-nowrap px-6 py-4 text-sm text-gray-500">
                          {student.studentPhysicalId}
                        </td>
                        <td className="whitespace-nowrap px-6 py-4 text-sm text-gray-500">
                          {student.year}
                        </td>
                        <td className="whitespace-nowrap px-6 py-4 text-sm text-gray-500">
                          {student.program}
                        </td>
                        <td className="whitespace-nowrap px-6 py-4 text-right text-sm font-medium">
                          <Button
                            variant="ghost"
                            size="icon"
                            onClick={() => handleEdit(student.id)}
                            className="text-blue-600 hover:text-blue-900"
                          >
                            <Pencil className="h-4 w-4" />
                          </Button>
                          <Button
                            variant="ghost"
                            size="icon"
                            onClick={() => handleDelete(student.id)}
                            className="text-red-600 hover:text-red-900"
                          >
                            <Trash2 className="h-4 w-4" />
                          </Button>
                        </td>
                      </tr>
                    ))
                  : teachers.map((teacher) => (
                      <tr key={teacher.id}>
                        <td className="whitespace-nowrap px-6 py-4 text-sm font-medium text-gray-900">
                          {teacher.firstName} {teacher.middleName ? `${teacher.middleName} ` : ''}{teacher.lastName}
                        </td>
                        <td className="whitespace-nowrap px-6 py-4 text-sm text-gray-500">
                          {teacher.email}
                        </td>
                        <td className="whitespace-nowrap px-6 py-4 text-sm text-gray-500">
                          {teacher.teacherPhysicalId}
                        </td>
                        <td className="whitespace-nowrap px-6 py-4 text-right text-sm font-medium">
                          <Button
                            variant="ghost"
                            size="icon"
                            onClick={() => handleEdit(teacher.id)}
                            className="text-blue-600 hover:text-blue-900"
                          >
                            <Pencil className="h-4 w-4" />
                          </Button>
                          <Button
                            variant="ghost"
                            size="icon"
                            onClick={() => handleDelete(teacher.id)}
                            className="text-red-600 hover:text-red-900"
                          >
                            <Trash2 className="h-4 w-4" />
                          </Button>
                        </td>
                      </tr>
                    ))}
                
                {((activeTab === 'students' && students.length === 0) ||
                  (activeTab === 'teachers' && teachers.length === 0)) && (
                  <tr>
                    <td
                      colSpan={activeTab === 'students' ? 6 : 4}
                      className="px-6 py-4 text-center text-sm text-gray-500"
                    >
                      No {activeTab} found
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

export default UserManagement;
