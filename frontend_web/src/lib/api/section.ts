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
  teacher?: TeacherDto | null;
  course?: CourseDto;
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
  // Get sections for a specific course or all sections if no courseId provided
  getAll: async (courseId?: number): Promise<Section[]> => {
    try {
      if (courseId) {
        // If courseId is provided, fetch sections for that course
        const response = await axiosInstance.get(`/sections?courseId=${courseId}`);
        const sectionsData = response.data.data || response.data || [];
        
        // Convert backend DTOs to frontend model
        return sectionsData.map((sectionDto: SectionDto) => ({
          id: sectionDto.id,
          courseId: sectionDto.course?.id,
          teacherId: sectionDto.teacher?.id,
          teacher: sectionDto.teacher, // Keep the full teacher object
          course: sectionDto.course, // Include full course object for display
          sectionName: sectionDto.sectionName,
          enrollmentKey: sectionDto.enrollmentKey,
          enrollmentOpen: sectionDto.enrollmentOpen,
          enrollmentCount: sectionDto.enrollmentCount
        }));
      } else {
        // If no courseId provided, use getAllSections to get all sections
        return await sectionApi.getAllSections();
      }
    } catch (error) {
      console.error(`Error fetching sections${courseId ? ` for courseId ${courseId}` : ''}:`, error);
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
            teacher: sectionDto.teacher, // Keep the full teacher object
            course: sectionDto.course, // Include full course object for display
            sectionName: sectionDto.sectionName,
            enrollmentKey: sectionDto.enrollmentKey,
            enrollmentOpen: sectionDto.enrollmentOpen,
            enrollmentCount: sectionDto.enrollmentCount
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
      
      // The backend doesn't have a direct API for getting sections by teacherId
      // We need to get all courses, then get sections for each course, and filter by teacher
      
      // Step 1: Get all courses
      const coursesResponse = await axiosInstance.get('/courses');
      const courses = coursesResponse.data.data || coursesResponse.data || [];
      
      let teacherSections: Section[] = [];
      
      // Step 2: Process each course to get its sections
      for (const course of courses) {
        try {
          const sectionsResponse = await axiosInstance.get('/sections', {
            params: { courseId: course.id }
          });
          
          const sections = sectionsResponse.data.data || sectionsResponse.data || [];
          
          // Step 3: Filter for sections assigned to this teacher
          const teacherSectionsForCourse = sections.filter((section: any) => {
            // In the backend DTO, teacher is a nested object - not just a teacherId
            // So we need to check section.teacher?.id or section.teacher.id
            const sectionTeacherId = section.teacher?.id || 
                                     (section.teacher ? section.teacher.id : null);
                                     
            console.log(`Section ${section.id} has teacher:`, section.teacher, 
                      `- Extracted teacherId:`, sectionTeacherId);
                      
            // Check if the section has this teacher assigned
            return sectionTeacherId === teacherId;
          });
          
          if (teacherSectionsForCourse.length > 0) {
            console.log(`Found ${teacherSectionsForCourse.length} sections for teacher in course ${course.id}`);
          }
          
          teacherSections = [...teacherSections, ...teacherSectionsForCourse];
        } catch (courseError) {
          console.warn(`Skipping course ${course.id} due to error:`, courseError);
          // Continue with next course
        }
      }
      
      console.log(`Total teacher sections found: ${teacherSections.length}`);
      return teacherSections;
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
      // Backend has an issue with the /end endpoint deleting seats
      // Let's use a multi-step process with working endpoints instead
      
      // Step 1: Remove teacher from section
      // This sets the teacher to null and updates the teacher's sections collection
      try {
        await axiosInstance.post(`/sections/${sectionId}/removeTeacher`);
        console.log(`Teacher removed from section ${sectionId}`);
      } catch (teacherError) {
        console.warn(`Could not remove teacher from section ${sectionId}:`, teacherError);
        // Continue even if this fails
      }
      
      // Step 2: Close enrollment for the section
      try {
        await axiosInstance.post(`/sections/${sectionId}/close`);
        console.log(`Enrollment closed for section ${sectionId}`);
      } catch (enrollmentError) {
        console.warn(`Could not close enrollment for section ${sectionId}:`, enrollmentError);
        // Continue even if this fails
      }
      
      // Step 3: Reset the enrollment key
      try {
        const update = {
          id: sectionId,
          enrollmentKey: null
        };
        await axiosInstance.put(`/sections/${sectionId}`, update);
        console.log(`Enrollment key reset for section ${sectionId}`);
      } catch (keyError) {
        console.warn(`Could not reset enrollment key for section ${sectionId}:`, keyError);
        // Continue even if this fails
      }
      
      return;
    } catch (error) {
      console.error(`Error ending section ${sectionId} with workaround:`, error);
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
  
  // Generate class code (enrollment key) for a section
  generateClassCode: async (sectionId: number): Promise<Section> => {
    try {
      // Generate a random 6-character alphanumeric code
      const generateRandomCode = () => {
        const chars = 'ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789';
        let code = '';
        for (let i = 0; i < 6; i++) {
          code += chars.charAt(Math.floor(Math.random() * chars.length));
        }
        return code;
      };
      
      const enrollmentKey = generateRandomCode();
      
      // Update the section with the new enrollment key
      const updateData: SectionUpdateDto = {
        id: sectionId,
        enrollmentKey: enrollmentKey
      };
      
      const response = await axiosInstance.put(`/sections/${sectionId}`, updateData);
      return response.data.data || response.data;
    } catch (error) {
      console.error(`Error generating class code for section ${sectionId}:`, error);
      throw error;
    }
  },
};
