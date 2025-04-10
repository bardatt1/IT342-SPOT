import axios from 'axios'
import { getAuthHeader } from './utils'

export interface Seat {
  id: number
  row: number
  column: number
  label: string
  studentId?: number
  studentName?: string
}

export interface SeatPlan {
  id: number
  name: string
  courseId: number
  courseName?: string
  isActive: boolean
  seats: Seat[]
}

const API_URL = 'http://localhost:8080/api'

export const seatPlanApi = {
  // Get all seat plans for a course
  getSeatPlans: async (courseId: number) => {
    return axios.get(`${API_URL}/courses/${courseId}/seatplans`, {
      headers: getAuthHeader(),
    })
  },

  // Get a specific seat plan
  getSeatPlan: async (seatPlanId: number) => {
    return axios.get(`${API_URL}/seatplans/${seatPlanId}`, {
      headers: getAuthHeader(),
    })
  },

  // Create a new seat plan
  createSeatPlan: async (courseId: number, name: string, rows: number, columns: number) => {
    return axios.post(
      `${API_URL}/courses/${courseId}/seatplans`,
      { name, rows, columns },
      { headers: getAuthHeader() }
    )
  },

  // Update a seat plan (name only)
  updateSeatPlan: async (seatPlanId: number, name: string) => {
    return axios.put(
      `${API_URL}/seatplans/${seatPlanId}`,
      { name },
      { headers: getAuthHeader() }
    )
  },

  // Delete a seat plan
  deleteSeatPlan: async (seatPlanId: number) => {
    return axios.delete(`${API_URL}/seatplans/${seatPlanId}`, {
      headers: getAuthHeader(),
    })
  },

  // Set a seat plan as active
  setActiveSeatPlan: async (seatPlanId: number, courseId: number) => {
    return axios.put(
      `${API_URL}/courses/${courseId}/seatplans/${seatPlanId}/active`,
      {},
      { headers: getAuthHeader() }
    )
  },

  // Assign a student to a seat
  assignStudentToSeat: async (seatPlanId: number, seatId: number, studentId: number) => {
    return axios.put(
      `${API_URL}/seatplans/${seatPlanId}/seats/${seatId}/assign`,
      { studentId },
      { headers: getAuthHeader() }
    )
  },

  // Remove a student from a seat
  removeStudentFromSeat: async (seatPlanId: number, seatId: number) => {
    return axios.delete(`${API_URL}/seatplans/${seatPlanId}/seats/${seatId}/assign`, {
      headers: getAuthHeader(),
    })
  },

  // Update seat label
  updateSeatLabel: async (seatPlanId: number, seatId: number, label: string) => {
    return axios.put(
      `${API_URL}/seatplans/${seatPlanId}/seats/${seatId}`,
      { label },
      { headers: getAuthHeader() }
    )
  }
}
