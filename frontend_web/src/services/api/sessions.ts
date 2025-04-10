import axios from 'axios';

const BASE_URL = 'http://localhost:8080/api';

const getAuthHeader = () => {
  const token = localStorage.getItem('token');
  if (token) {
    return { Authorization: `Bearer ${token}` };
  }
  return {};
};

export interface SessionAttendance {
  id: number;
  studentId: number;
  studentName: string;
  status: 'PRESENT' | 'ABSENT' | 'LATE' | 'UNMARKED';
  checkInTime?: string;
  seat?: {
    id: number;
    row: number;
    column: number;
    label: string;
  };
}

export const sessionApi = {
  // Start a session
  startSession: async (sessionId: number) => {
    return axios.post<{ success: boolean, qrCode: string }>(
      `${BASE_URL}/sessions/${sessionId}/start`, 
      {}, 
      { headers: getAuthHeader() }
    );
  },

  // End a session
  endSession: async (sessionId: number) => {
    return axios.post<{ success: boolean }>(
      `${BASE_URL}/sessions/${sessionId}/end`, 
      {}, 
      { headers: getAuthHeader() }
    );
  },

  // Get session details
  getSession: async (sessionId: number) => {
    return axios.get(
      `${BASE_URL}/sessions/${sessionId}`,
      { headers: getAuthHeader() }
    );
  },

  // Get attendance for a session
  getSessionAttendance: async (sessionId: number) => {
    return axios.get<SessionAttendance[]>(
      `${BASE_URL}/sessions/${sessionId}/attendance`,
      { headers: getAuthHeader() }
    );
  },

  // Mark attendance for a session (student checking in)
  markAttendance: async (sessionId: number, qrCode: string, seatId?: number) => {
    return axios.post<{ success: boolean }>(
      `${BASE_URL}/sessions/${sessionId}/check-in`,
      { qrCode, seatId },
      { headers: getAuthHeader() }
    );
  },

  // Generate a new QR code for a session
  generateQRCode: async (sessionId: number) => {
    return axios.post<{ qrCode: string }>(
      `${BASE_URL}/sessions/${sessionId}/generate-qr`,
      {},
      { headers: getAuthHeader() }
    );
  },

  // Update attendance status for a student
  updateAttendanceStatus: async (attendanceId: number, status: 'PRESENT' | 'ABSENT' | 'LATE') => {
    return axios.put<{ success: boolean }>(
      `${BASE_URL}/attendance/${attendanceId}/status`,
      { status },
      { headers: getAuthHeader() }
    );
  },

  // Mark all students present for a session
  markAllPresent: async (sessionId: number) => {
    return axios.post<{ success: boolean }>(
      `${BASE_URL}/sessions/${sessionId}/mark-all-present`,
      {},
      { headers: getAuthHeader() }
    );
  },

  // Get active sessions for a student
  getActiveSessionsForStudent: async () => {
    return axios.get(
      `${BASE_URL}/sessions/active/student`,
      { headers: getAuthHeader() }
    );
  },

  // Get active sessions for a teacher
  getActiveSessionsForTeacher: async () => {
    return axios.get(
      `${BASE_URL}/sessions/active/teacher`,
      { headers: getAuthHeader() }
    );
  }
};
