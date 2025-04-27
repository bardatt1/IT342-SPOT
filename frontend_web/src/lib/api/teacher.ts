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
      // Using admin endpoint for getting all teachers
      const response = await axiosInstance.get('/admin/teachers');
      return response.data.data || response.data || [];
    } catch (error) {
      console.error('Error fetching teachers:', error);
      return []; // Return empty array on error
    }
  },

  // Get teacher by ID
  getById: async (id: number): Promise<Teacher> => {
    const response = await axiosInstance.get(`/admin/teachers/${id}`);
    return response.data.data || response.data;
  },

  // Update teacher details
  update: async (teacher: Partial<Teacher>): Promise<Teacher> => {
    const response = await axiosInstance.put(`/teachers/${teacher.id}`, teacher);
    return response.data.data || response.data;
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
};
