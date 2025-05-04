import axiosInstance from './axiosInstance';

export interface Attendance {
  id: number;
  studentId?: number;
  sectionId: number;
  date: string;
  startTime: string;
  endTime?: string;
  student?: {
    id: number;
    firstName: string;
    lastName: string;
    middleName?: string;
    year?: string;
    program?: string;
    email?: string;
    studentPhysicalId?: string;
  };
  section?: {
    id: number;
    sectionName: string;
    course?: {
      id: number;
      name: string;
      code: string;
    }
  };
}

export interface AttendanceAnalytics {
  totalSessions: number;
  totalStudents: number;
  averageAttendance: number;
  attendanceByDate: {
    date: string;
    count: number;
    percentage: number;
  }[];
  studentAttendance: {
    studentId: number;
    studentName: string;
    attendanceCount: number;
    attendancePercentage: number;
  }[];
}

export const attendanceApi = {
  // Comprehensive diagnostic method to check ALL prerequisites for analytics
  checkSectionAnalyticsData: async (sectionId: number): Promise<any> => {
    try {
      // Get section details
      console.log(`Checking data for section: ${sectionId}`);
      
      // STEP 1: Check enrollments using the correct endpoint
      console.log(`STEP 1: Checking enrollments for section ${sectionId}...`);
      let hasEnrollments = false;
      let enrollmentCount = 0;
      try {
        const enrollmentResponse = await axiosInstance.get(`/enrollments/section/${sectionId}`);
        const enrollments = enrollmentResponse.data?.data || [];
        enrollmentCount = enrollments.length;
        hasEnrollments = enrollmentCount > 0;
        console.log(`✓ Section ${sectionId} has ${enrollmentCount} enrollments`);
      } catch (err: any) {
        console.error(`✗ ERROR checking enrollments:`, err?.message || err);
      }
      
      // STEP 2: Check schedules - THIS IS CRITICAL FOR ANALYTICS
      console.log(`STEP 2: Checking schedules for section ${sectionId}...`);
      let hasSchedules = false;
      let scheduleCount = 0;
      try {
        // Get schedules using the correct endpoint with query parameter
        const scheduleResponse = await axiosInstance.get(`/schedules?sectionId=${sectionId}`);
        const schedules = scheduleResponse.data?.data || [];
        scheduleCount = schedules.length;
        hasSchedules = scheduleCount > 0;
        console.log(`✓ Section ${sectionId} has ${scheduleCount} schedule entries`);
        
        if (scheduleCount === 0) {
          console.warn(
            `Warning: No schedules found for section ${sectionId}. ` +
            `Analytics calculations require at least one schedule to define class days.`
          );
        }
      } catch (err: any) {
        // If endpoint still doesn't exist or returns an error
        console.error(`✗ ERROR checking schedules:`, err?.message || err);
        console.warn('Note: The analytics calculation REQUIRES schedules to be defined');
      }
      
      // STEP 3: Check attendance records
      console.log(`STEP 3: Checking attendance records for section ${sectionId}...`);
      let hasAttendance = false;
      let attendanceCount = 0;
      try {
        const attendanceResponse = await axiosInstance.get(`/attendance/section/${sectionId}`);
        const attendances = attendanceResponse.data?.data || [];
        attendanceCount = attendances.length;
        hasAttendance = attendanceCount > 0;
        console.log(`✓ Section ${sectionId} has ${attendanceCount} attendance records`);
      } catch (err: any) {
        console.error(`✗ ERROR checking attendance:`, err?.message || err);
      }
      
      // Return comprehensive diagnostics
      return {
        // Data counts
        enrollmentCount,
        scheduleCount,
        attendanceCount,
        
        // Prerequisites status
        hasEnrollments,
        hasSchedules,
        hasAttendance,
        
        // Analytics calculation status
        analyticsCanBeCalculated: hasEnrollments && hasSchedules,
        missingPrerequisites: [
          !hasEnrollments ? 'No students enrolled in section' : null,
          !hasSchedules ? 'No schedules defined for section' : null,
          !hasAttendance ? 'No attendance records for section' : null
        ].filter(Boolean)
      };
    } catch (error: any) {
      console.error(`Error checking section ${sectionId} data:`, error);
      return {
        error: true,
        message: error?.message || 'Unknown error occurred'
      };
    }
  },
  // Get attendance logs for a section
  getSectionAttendance: async (sectionId: number): Promise<Attendance[]> => {
    try {
      console.log(`Fetching attendance for section: ${sectionId}`);
      const response = await axiosInstance.get(`/attendance/section/${sectionId}`);
      console.log('Attendance API raw response:', response);
      
      // Safely extract the attendance data
      let attendanceData = [];
      
      if (response.data) {
        if (Array.isArray(response.data)) {
          attendanceData = response.data;
        } else if (response.data.data && Array.isArray(response.data.data)) {
          attendanceData = response.data.data;
        }
      }
      
      console.log(`Retrieved ${attendanceData.length} attendance records:`, attendanceData);
      return attendanceData;
    } catch (error) {
      console.error(`Error fetching attendance for section ${sectionId}:`, error);
      return [];
    }
  },
  
  // Get attendance logs for a student
  getStudentAttendance: async (studentId: number): Promise<Attendance[]> => {
    const response = await axiosInstance.get(`/attendance/student/${studentId}`);
    return response.data;
  },
  
  // Generate QR code for attendance
  generateQrCode: async (sectionId: number): Promise<{ imageBase64: string, url: string, expiresInSeconds: number }> => {
    try {
      // Backend expects sectionId as a request parameter, not in request body
      const response = await axiosInstance.post(`/attendance/generate-qr?sectionId=${sectionId}`);
      
      console.log('QR code response:', response.data);
      
      // Properly handle the nested response structure
      if (response.data && response.data.data) {
        const qrData = response.data.data;
        
        // Use new URL format for attendance logging
        const newUrl = `https://spot-edu.me/attendance/log/${sectionId}`;
        
        return {
          imageBase64: qrData.qrCodeImageBase64,
          url: newUrl, // Use the new URL format instead of qrData.qrCodeUrl
          expiresInSeconds: qrData.expiresInSeconds || 300 // Default 5 minutes if not provided
        };
      } else {
        console.error('Unexpected response format:', response.data);
        throw new Error('Invalid response format from server');
      }
    } catch (error) {
      console.error('QR code generation error:', error);
      throw error;
    }
  },
  
  // Get analytics for a section
  getSectionAnalytics: async (sectionId: number): Promise<AttendanceAnalytics> => {
    try {
      console.log(`Fetching analytics for section: ${sectionId}`);
      const response = await axiosInstance.get(`/analytics/${sectionId}`);
      console.log('Analytics API raw response:', response);

      // Default values for the analytics object
      const defaultAnalytics: AttendanceAnalytics = {
        totalSessions: 0,
        totalStudents: 0,
        averageAttendance: 0,
        attendanceByDate: [],
        studentAttendance: []
      };
      
      // Safely extract the analytics data
      let analyticsData = defaultAnalytics;
      
      if (response.data) {
        if (typeof response.data === 'object') {
          // Merge with defaults to ensure all properties exist
          analyticsData = { ...defaultAnalytics, ...response.data };
        } else if (response.data.data && typeof response.data.data === 'object') {
          // Sometimes the data might be nested further
          analyticsData = { ...defaultAnalytics, ...response.data.data };
        }
      }
      
      console.log('Processed analytics data:', analyticsData);
      return analyticsData;
    } catch (error) {
      console.error(`Error fetching analytics for section ${sectionId}:`, error);
      // Return a default analytics object instead of throwing an error
      return {
        totalSessions: 0,
        totalStudents: 0,
        averageAttendance: 0,
        attendanceByDate: [],
        studentAttendance: []
      };
    }
  }
};
