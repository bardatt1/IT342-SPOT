import axiosInstance from './axiosInstance';

// Interfaces matching the backend DTOs
interface TeacherDto {
  id: number;
  firstName: string;
  lastName: string;
  email: string;
  teacherPhysicalId: string;
}

interface CourseDto {
  id: number;
  courseCode: string;
  courseName: string;
}

// Backend SectionDto structure
interface SectionDto {
  id: number;
  course: CourseDto;
  teacher: TeacherDto | null;
  sectionName: string;
  enrollmentKey: string;
  enrollmentOpen: boolean;
  enrollmentCount: number;
}

// Frontend Section structure
export interface Section {
  id: number;
  courseId: number;
  teacherId?: number;
  sectionName: string;
  enrollmentKey: string;
  enrollmentOpen: boolean;
  enrollmentCount: number;
  // Note: room field removed - it's not in the backend Section entity
}

export interface SectionCreateDto {
  courseId: number;
  sectionName: string; // Added to match backend expectation
}

export interface SectionUpdateDto {
  id: number;
  sectionName?: string;
  enrollmentKey?: string;
}

export const sectionApi = {
  // Get sections for a specific course
  getAll: async (courseId: number): Promise<Section[]> => {
    try {
      // Backend requires courseId as a query parameter
      const response = await axiosInstance.get(`/sections?courseId=${courseId}`);
      const sectionsData = response.data.data || response.data || [];
      
      // Convert backend DTOs to frontend model
      return sectionsData.map((sectionDto: SectionDto) => ({
        id: sectionDto.id,
        courseId: sectionDto.course?.id,
        teacherId: sectionDto.teacher?.id,
        sectionName: sectionDto.sectionName,
        enrollmentKey: sectionDto.enrollmentKey,
        enrollmentOpen: sectionDto.enrollmentOpen,
        enrollmentCount: sectionDto.enrollmentCount,
        room: sectionDto.sectionName // Using sectionName as room since backend doesn't have room
      }));
    } catch (error) {
      console.error(`Error fetching sections for courseId ${courseId}:`, error);
      return []; // Return empty array on error to avoid breaking the UI
    }
  },
  
  // Get all sections for admin dashboard by fetching from all courses
  getAllSections: async (): Promise<Section[]> => {
    try {
      // First get all courses
      const coursesResponse = await axiosInstance.get('/courses');
      const courses = coursesResponse.data.data || coursesResponse.data || [];
      
      // Then get sections for each course and combine them
      let allSections: Section[] = [];
      for (const course of courses) {
        try {
          const sectionsResponse = await axiosInstance.get(`/sections?courseId=${course.id}`);
          const sectionsData = sectionsResponse.data.data || sectionsResponse.data || [];
          
          // Convert backend DTOs to frontend model
          const convertedSections = sectionsData.map((sectionDto: SectionDto) => ({
            id: sectionDto.id,
            courseId: sectionDto.course?.id,
            teacherId: sectionDto.teacher?.id,
            sectionName: sectionDto.sectionName,
            enrollmentKey: sectionDto.enrollmentKey,
            enrollmentOpen: sectionDto.enrollmentOpen,
            enrollmentCount: sectionDto.enrollmentCount,
            room: sectionDto.sectionName // Using sectionName as room for now
          }));
          
          allSections = [...allSections, ...convertedSections];
        } catch (error) {
          console.error(`Error fetching sections for courseId ${course.id}:`, error);
          // Continue to next course even if one fails
        }
      }
      return allSections;
    } catch (error) {
      console.error('Error fetching all sections:', error);
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
      // Send teacherId as a query parameter, not in the request body
      // This matches what the backend expects (@RequestParam)
      await axiosInstance.post(`/sections/${sectionId}/assign?teacherId=${teacherId}`);
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
