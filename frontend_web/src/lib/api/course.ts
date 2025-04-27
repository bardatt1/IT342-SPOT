import axiosInstance from './axiosInstance';

export interface Course {
  id: number;
  courseName: string;
  courseDescription: string;
  courseCode: string;
}

export interface CourseCreateDto {
  courseName: string;
  courseDescription: string;
  courseCode: string;
}

export interface CourseUpdateDto {
  id: number;
  courseName?: string;
  courseDescription?: string;
  courseCode?: string;
}

export const courseApi = {
  // Get all courses
  getAll: async (): Promise<Course[]> => {
    try {
      const response = await axiosInstance.get('/courses');
      return response.data.data || response.data || [];
    } catch (error) {
      console.error('Error fetching courses:', error);
      return []; // Return empty array on error
    }
  },

  // Get course by ID
  getById: async (id: number): Promise<Course> => {
    const response = await axiosInstance.get(`/courses/${id}`);
    return response.data.data || response.data;
  },

  // Create a new course
  create: async (course: CourseCreateDto): Promise<Course> => {
    try {
      console.log('Creating course with:', course);
      const response = await axiosInstance.post('/courses', course);
      console.log('Course creation response:', response.data);
      return response.data.data || response.data;
    } catch (error) {
      console.error('Error creating course:', error);
      throw error;
    }
  },

  // Update course
  update: async (course: CourseUpdateDto): Promise<Course> => {
    try {
      const response = await axiosInstance.put(`/courses/${course.id}`, course);
      return response.data.data || response.data;
    } catch (error) {
      console.error(`Error updating course ${course.id}:`, error);
      throw error;
    }
  },

  // Delete course
  delete: async (id: number): Promise<void> => {
    try {
      await axiosInstance.delete(`/courses/${id}`);
    } catch (error) {
      console.error(`Error deleting course ${id}:`, error);
      throw error;
    }
  },
};
