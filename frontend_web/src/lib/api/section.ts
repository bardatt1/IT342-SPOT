import axiosInstance from './axiosInstance';

export interface Section {
  id: number;
  courseId: number;
  teacherId?: number;
  enrollmentKey: string;
  room: string;
}

export interface SectionCreateDto {
  courseId: number;
  room: string;
}

export interface SectionUpdateDto {
  id: number;
  room?: string;
  enrollmentKey?: string;
}

export const sectionApi = {
  // Get all sections for admin dashboard
  getAll: async (): Promise<Section[]> => {
    try {
      // Use the correct endpoint for getting all sections
      // According to API docs, there is no /admin/sections endpoint
      const response = await axiosInstance.get('/sections');
      return response.data.data || response.data || [];
    } catch (error) {
      console.error('Error fetching sections:', error);
      return []; // Return empty array on error to avoid breaking the UI
    }
  },
  
  // Get sections for a specific teacher
  getByTeacherId: async (teacherId: number): Promise<Section[]> => {
    try {
      console.log(`Fetching sections for teacher ID: ${teacherId}`);
      // Add teacherId as query parameter
      const response = await axiosInstance.get(`/sections`, {
        params: { teacherId }
      });
      console.log('Teacher sections response:', response.data);
      return response.data.data || response.data || [];
    } catch (error) {
      console.error(`Error fetching sections for teacher ${teacherId}:`, error);
      return []; // Return empty array on error to avoid breaking the UI
    }
  },

  // Get section by ID
  getById: async (id: number): Promise<Section> => {
    try {
      const response = await axiosInstance.get(`/sections/${id}`);
      return response.data.data || response.data;
    } catch (error) {
      console.error(`Error fetching section ${id}:`, error);
      throw error;
    }
  },

  // Create a new section
  create: async (section: SectionCreateDto): Promise<Section> => {
    try {
      console.log('Creating section with:', section);
      const response = await axiosInstance.post('/sections', section);
      console.log('Section creation response:', response.data);
      return response.data.data || response.data;
    } catch (error) {
      console.error('Error creating section:', error);
      throw error;
    }
  },

  // Update section
  update: async (section: SectionUpdateDto): Promise<Section> => {
    try {
      const response = await axiosInstance.put(`/sections/${section.id}`, section);
      return response.data.data || response.data;
    } catch (error) {
      console.error(`Error updating section ${section.id}:`, error);
      throw error;
    }
  },

  // Delete section
  delete: async (id: number): Promise<void> => {
    try {
      await axiosInstance.delete(`/sections/${id}`);
    } catch (error) {
      console.error(`Error deleting section ${id}:`, error);
      throw error;
    }
  },

  // Assign teacher to section
  assignTeacher: async (sectionId: number, teacherId: number): Promise<void> => {
    try {
      await axiosInstance.post(`/sections/${sectionId}/assign`, { teacherId });
    } catch (error) {
      console.error(`Error assigning teacher ${teacherId} to section ${sectionId}:`, error);
      throw error;
    }
  },

  // End section (clear everything including seats and teacher)
  endSection: async (sectionId: number): Promise<void> => {
    try {
      await axiosInstance.post(`/sections/${sectionId}/end`);
    } catch (error) {
      console.error(`Error ending section ${sectionId}:`, error);
      throw error;
    }
  },
  
  // Open enrollment
  openEnrollment: async (sectionId: number): Promise<void> => {
    try {
      await axiosInstance.post(`/sections/${sectionId}/open`);
    } catch (error) {
      console.error(`Error opening enrollment for section ${sectionId}:`, error);
      throw error;
    }
  },
  
  // Close enrollment
  closeEnrollment: async (sectionId: number): Promise<void> => {
    try {
      await axiosInstance.post(`/sections/${sectionId}/close`);
    } catch (error) {
      console.error(`Error closing enrollment for section ${sectionId}:`, error);
      throw error;
    }
  },
};
