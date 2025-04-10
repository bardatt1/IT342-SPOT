import api from '@/services/api';

export interface Course {
  id: number;
  name: string;
  courseCode: string;
  description: string;
  schedule: string;
  room: string;
  teacher: {
    id: number;
    firstName: string;
    lastName: string;
  };
  students: Array<{
    id: number;
    firstName: string;
    lastName: string;
  }>;
}

export interface Session {
  id: number;
  course: Course;
  startTime: string;
  endTime: string;
  status: 'SCHEDULED' | 'ACTIVE' | 'COMPLETED' | 'CANCELED';
  seatPlan?: {
    id: number;
    name: string;
  };
}

// Helper to ensure token is included properly in requests
const getAuthHeaders = () => {
  const token = localStorage.getItem('token');
  if (!token) {
    console.warn('No token available for API request');
    return {};
  }
  
  return {
    'Authorization': `Bearer ${token.trim()}`,
    'Accept': 'application/json'
  };
};

export const courseApi = {
  // Get all courses for a teacher
  getCourses: async () => {
    console.log('Fetching courses with explicit auth headers');
    return api.get<Course[]>(`/api/courses`, {
      headers: getAuthHeaders()
    });
  },

  // Get a specific course
  getCourse: async (courseId: number) => {
    return api.get<Course>(`/api/courses/${courseId}`, {
      headers: getAuthHeaders()
    });
  },

  // Get all upcoming sessions for a teacher
  getUpcomingSessions: async () => {
    console.log('Fetching upcoming sessions with explicit auth headers');
    return api.get<Session[]>(`/api/sessions/upcoming`, {
      headers: getAuthHeaders()
    });
  },

  // Get active sessions for a student
  getActiveSessionsForStudent: async () => {
    console.log('Fetching active sessions for student with explicit auth headers');
    return api.get<Session[]>(`/api/sessions/active`, {
      headers: getAuthHeaders()
    });
  },

  // Create a new course
  createCourse: async (courseData: Omit<Course, 'id' | 'teacher' | 'students'>) => {
    return api.post<Course>(`/api/courses`, courseData, {
      headers: getAuthHeaders()
    });
  },

  // Update a course
  updateCourse: async (courseId: number, courseData: Partial<Course>) => {
    return api.put<Course>(`/api/courses/${courseId}`, courseData, {
      headers: getAuthHeaders()
    });
  },

  // Delete a course
  deleteCourse: async (courseId: number) => {
    return api.delete(`/api/courses/${courseId}`, {
      headers: getAuthHeaders()
    });
  }
};
