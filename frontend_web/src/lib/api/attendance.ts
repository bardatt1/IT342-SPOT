import axiosInstance from './axiosInstance';

export interface Attendance {
  id: number;
  studentId: number;
  sectionId: number;
  date: string;
  startTime: string;
  endTime?: string;
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
  // Get attendance logs for a section
  getSectionAttendance: async (sectionId: number): Promise<Attendance[]> => {
    const response = await axiosInstance.get(`/attendance/section/${sectionId}`);
    return response.data;
  },
  
  // Get attendance logs for a student
  getStudentAttendance: async (studentId: number): Promise<Attendance[]> => {
    const response = await axiosInstance.get(`/attendance/student/${studentId}`);
    return response.data;
  },
  
  // Generate QR code for attendance
  generateQrCode: async (sectionId: number): Promise<string> => {
    const response = await axiosInstance.post('/attendance/generate-qr', { sectionId });
    return response.data.qrCodeData;
  },
  
  // Get analytics for a section
  getSectionAnalytics: async (sectionId: number): Promise<AttendanceAnalytics> => {
    const response = await axiosInstance.get(`/analytics/${sectionId}`);
    return response.data;
  }
};
