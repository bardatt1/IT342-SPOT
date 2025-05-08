import axiosInstance from './axiosInstance';

export interface Teacher {
  id: number;
  firstName: string;
  middleName?: string | null;
  lastName: string;
  email: string;
  teacherPhysicalId: string;
}

export interface TeacherCreateDto {
  firstName: string;
  middleName?: string | null;
  lastName: string;
  email: string;
  teacherPhysicalId: string;
  password?: string;
}

export const teacherApi = {
  // Get all teachers
  getAll: async (): Promise<Teacher[]> => {
    try {
      // Try to use system-admin endpoint first, fall back to admin endpoint
      try {
        const response = await axiosInstance.get('/system-admin/teachers');
        return response.data.data || response.data || [];
      } catch (systemAdminError) {
        // If the system-admin endpoint fails, try the regular admin endpoint
        console.log('Falling back to admin endpoint for teachers');
        const response = await axiosInstance.get('/admin/teachers');
        return response.data.data || response.data || [];
      }
    } catch (error) {
      console.error('Error fetching teachers:', error);
      return []; // Return empty array on error
    }
  },

  // Get teacher by ID (admin only)
  getById: async (id: number): Promise<Teacher> => {
    const response = await axiosInstance.get(`/admin/teachers/${id}`);
    return response.data.data || response.data;
  },
  
  // Get current teacher details (for teacher self-service)
  getCurrentTeacher: async (): Promise<Teacher> => {
    try {
      // Use the dedicated teacher profile endpoint
      const response = await axiosInstance.get('/teacher-profile/me');
      return response.data.data || response.data;
    } catch (error) {
      console.error('Error fetching current teacher details:', error);
      throw error;
    }
  },

  // Update teacher details (admin only or specific ID)
  update: async (teacher: Partial<Teacher>): Promise<Teacher> => {
    const response = await axiosInstance.put(`/teachers/${teacher.id}`, teacher);
    return response.data.data || response.data;
  },
  
  // Update current teacher details (for teacher self-service)
  updateCurrentTeacher: async (teacherData: Partial<Omit<Teacher, 'id'>>): Promise<Teacher> => {
    try {
      // Use the dedicated teacher profile endpoint
      const response = await axiosInstance.put('/teacher-profile/me', teacherData);
      return response.data.data || response.data;
    } catch (error) {
      console.error('Error updating current teacher profile:', error);
      throw error;
    }
  },

  // Create a new teacher
  create: async (teacher: TeacherCreateDto): Promise<Teacher> => {
    try {
      console.log('Creating teacher with:', teacher);
      const response = await axiosInstance.post('/admin/create-teacher', teacher);
      console.log('Teacher creation response:', response.data);
      return response.data.data || response.data;
    } catch (error) {
      console.error('Error creating teacher:', error);
      throw error;
    }
  },

  // Assign teacher to section
  assignToSection: async (teacherId: number, sectionId: number): Promise<void> => {
    await axiosInstance.post(`/teachers/${teacherId}/assign/${sectionId}`);
  },

  // Delete a teacher
  delete: async (id: number): Promise<boolean> => {
    console.log(`Attempting to delete teacher with ID: ${id}`);
    try {
      // Since the backend API expects a Long id parameter, we need to ensure proper formatting
      // Use the standard endpoint with axiosInstance which handles the base URL correctly
      const response = await axiosInstance.delete(`/admin/teachers/${id}`);
      console.log(`Successfully deleted teacher with ID: ${id}`);
      return response.data.data || response.data;
    } catch (error) {
      console.error('Error deleting teacher:', error);
      throw error;
    }
  },
};
