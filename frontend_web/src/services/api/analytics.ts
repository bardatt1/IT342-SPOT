import axios from 'axios'
import { getAuthHeader } from './utils'

export interface AttendanceMetrics {
  overallAttendanceRate: number
  averageLateStudents: number
  totalClasses: number
  totalStudents: number
  attendanceTrend: {
    date: string
    attendanceRate: number
  }[]
  classwiseAttendance: {
    courseCode: string
    courseName: string
    attendanceRate: number
  }[]
}

const API_URL = 'http://localhost:8080/api'

export const analyticsApi = {
  // Get overall attendance metrics
  getAttendanceMetrics: async () => {
    return axios.get(`${API_URL}/analytics/attendance`, {
      headers: getAuthHeader(),
    })
  },

  // Get attendance analytics for a specific course
  getCourseAttendanceAnalytics: async (courseId: number) => {
    return axios.get(`${API_URL}/analytics/courses/${courseId}/attendance`, {
      headers: getAuthHeader(),
    })
  },

  // Get attendance trend over time
  getAttendanceTrend: async (startDate?: string, endDate?: string) => {
    let url = `${API_URL}/analytics/attendance/trend`
    
    if (startDate && endDate) {
      url += `?startDate=${startDate}&endDate=${endDate}`
    }
    
    return axios.get(url, {
      headers: getAuthHeader(),
    })
  },

  // Get most attending students
  getMostAttendingStudents: async (limit: number = 5) => {
    return axios.get(`${API_URL}/analytics/students/most-attending?limit=${limit}`, {
      headers: getAuthHeader(),
    })
  },

  // Get least attending students
  getLeastAttendingStudents: async (limit: number = 5) => {
    return axios.get(`${API_URL}/analytics/students/least-attending?limit=${limit}`, {
      headers: getAuthHeader(),
    })
  },

  // Get attendance by seat position
  getAttendanceBySeatPosition: async (courseId: number) => {
    return axios.get(`${API_URL}/analytics/courses/${courseId}/attendance/by-seat`, {
      headers: getAuthHeader(),
    })
  }
}
