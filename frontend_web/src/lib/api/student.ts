import axiosInstance from './axiosInstance';

export interface Student {
  id: number;
  firstName: string;
  middleName?: string | null;
  lastName: string;
  year: string;
  program: string;
  email: string;
  studentPhysicalId: string;
}

export interface StudentCreateDto {
  firstName: string;
  middleName?: string | null;
  lastName: string;
  year: string;
  program: string;
  email: string;
  studentPhysicalId: string;
  password: string; // Required by backend validation
}

export const studentApi = {
  // Get all students - admin or system-admin endpoint
  getAll: async (): Promise<Student[]> => {
    try {
      // Try to use system-admin endpoint first, fall back to admin endpoint
      try {
        const response = await axiosInstance.get('/system-admin/students');
        console.log('Student data response from system-admin endpoint:', response.data);
        return response.data?.data || [];
      } catch (systemAdminError) {
        // If the system-admin endpoint fails, try the regular admin endpoint
        console.log('Falling back to admin endpoint for students');
        const response = await axiosInstance.get('/admin/students');
        console.log('Student data response from admin endpoint:', response.data);
        return response.data?.data || [];
      }
    } catch (error: any) {
      console.error('Error fetching students:', error);
      // Check if it's an authorization error and provide more context
      if (error.response?.status === 403 || error.response?.status === 401) {
        console.warn('Authorization error: User likely doesn\'t have admin permissions');
      }
      return []; // Return empty array on error
    }
  },
  
  // Get students by section ID - teacher friendly endpoint
  getBySection: async (sectionId: number): Promise<Student[]> => {
    try {
      // Use the enrollments endpoint which is accessible to teachers
      const response = await axiosInstance.get(`/enrollments/section/${sectionId}`);
      console.log('Section enrollments response:', response.data);
      
      // Extract student information from enrollments
      const enrollments = response.data?.data || [];
      
      // Map enrollment data to student data
      const students = enrollments.map((enrollment: any) => ({
        id: enrollment.student?.id,
        firstName: enrollment.student?.firstName || '',
        lastName: enrollment.student?.lastName || '',
        middleName: enrollment.student?.middleName,
        year: enrollment.student?.year || '',
        program: enrollment.student?.program || '',
        email: enrollment.student?.email || '',
        studentPhysicalId: enrollment.student?.studentPhysicalId || ''
      }));
      
      return students.filter((s: any) => s.id); // Filter out any invalid entries
    } catch (error: any) {
      console.error(`Error fetching students for section ${sectionId}:`, error);
      return []; // Return empty array on error
    }
  },

  // Get student by ID
  getById: async (id: number): Promise<Student | null> => {
    try {
      const response = await axiosInstance.get(`/admin/students/${id}`);
      return response.data?.data || null;
    } catch (error: any) {
      console.error(`Error fetching student ${id}:`, error);
      return null;
    }
  },

  // Update student details
  update: async (student: Partial<Student>): Promise<Student> => {
    const response = await axiosInstance.put(`/students/${student.id}`, student);
    return response.data.data || response.data;
  },

  // Create a new student
  create: async (student: StudentCreateDto): Promise<Student> => {
    try {
      console.log('Creating student with:', student);
      // Log the actual request being sent
      console.log('Student creation payload:', JSON.stringify(student, null, 2));
      const response = await axiosInstance.post('/admin/create-student', student);
      console.log('Student creation response:', response.data);
      return response.data.data || response.data;
    } catch (error: any) {
      console.error('Error creating student:', error);
      // Enhanced error logging to show validation details
      if (error.response) {
        console.error('Error details:', error.response.status, JSON.stringify(error.response.data, null, 2));
        // Show full validation errors if available
        if (error.response.data && error.response.data.data) {
          console.error('Validation errors:', error.response.data.data);
        }
      }
      throw error;
    }
  },

  // Delete student
  delete: async (id: number): Promise<void> => {
    await axiosInstance.delete(`/admin/students/${id}`);
  },
};
