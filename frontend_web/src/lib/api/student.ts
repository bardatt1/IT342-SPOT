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
  // Get all students
  getAll: async (): Promise<Student[]> => {
    try {
      // Using admin endpoint for getting all students
      const response = await axiosInstance.get('/admin/students');
      return response.data.data || response.data || [];
    } catch (error) {
      console.error('Error fetching students:', error);
      return []; // Return empty array on error
    }
  },

  // Get student by ID
  getById: async (id: number): Promise<Student> => {
    const response = await axiosInstance.get(`/admin/students/${id}`);
    return response.data.data || response.data;
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
