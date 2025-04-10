import axios from 'axios'
import { getAuthHeader } from './utils'
import { env } from '@/config/env'

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

const API_URL = `${env.apiUrl}/api`

export const seatPlanApi = {
  // Get all seat plans for a course
  getSeatPlans: async (courseId: number) => {
    try {
      // Use courseId/classId as the path parameter correctly
      const response = await axios.get(`${API_URL}/seats/${courseId}`, {
        headers: getAuthHeader(),
      })
      
      // Handle empty response
      if (!response.data) {
        return { data: [] }
      }
      
      // The API returns an object with a "seatPlans" array, not the array directly
      if (response.data.seatPlans && Array.isArray(response.data.seatPlans)) {
        // Transform the data to match our frontend model
        const transformedData = response.data.seatPlans.map((plan: any) => {
          return {
            id: plan.id,
            name: plan.name,
            courseId: plan.course?.id,
            courseName: plan.course?.name,
            isActive: plan.active || (plan.id === response.data.activePlanId),
            seats: [] // We'll need to get seat data separately or construct from rows/columns
          }
        })
        
        return { data: transformedData }
      }
      
      // If the response already has the exact format we expect (unlikely)
      if (Array.isArray(response.data)) {
        return { data: response.data }
      }
      
      // If response data is an object but not in the expected format
      if (typeof response.data === 'object') {
        console.warn('Unexpected API response format for seat plans:', response.data)
        
        // The API might return a single seat plan directly
        if (response.data.id || response.data.name) {
          return { 
            data: [{
              id: response.data.id,
              name: response.data.name,
              courseId: response.data.course?.id,
              courseName: response.data.course?.name,
              isActive: response.data.active,
              seats: []
            }] 
          }
        }
      }
      
      // Fallback to empty array if none of the above conditions are met
      console.warn('Could not parse seat plans data, returning empty array')
      return { data: [] }
    } catch (error) {
      console.error('Error fetching seat plans:', error)
      return { data: [] }
    }
  },

  // Get a specific seat plan
  getSeatPlan: async (seatPlanId: number, courseId: number) => {
    try {
      // We need to use courseId as the path parameter and seatPlanId as a query parameter
      const response = await axios.get(`${API_URL}/seats/${courseId}`, {
        headers: getAuthHeader(),
        params: {
          planId: seatPlanId
        }
      })
      
      if (!response.data) {
        throw new Error('No data returned from server')
      }
      
      // First check if the response has seatPlans array
      if (response.data.seatPlans && Array.isArray(response.data.seatPlans)) {
        // Find the plan with the matching ID
        const plan = response.data.seatPlans.find((p: any) => p.id === seatPlanId)
        
        if (plan) {
          // Get any assignments data if available
          const assignments = response.data.assignments?.[plan.id] || []
          
          // Create a grid of seats based on the rows and columns
          const seats: Seat[] = []
          for (let row = 0; row < (plan.rows || 5); row++) {
            for (let col = 0; col < (plan.columns || 6); col++) {
              const seatId = row * (plan.columns || 6) + col
              
              // Check if this seat has an assignment
              const assignment = assignments.find((a: any) => 
                a.rowIndex === row && a.columnIndex === col
              )
              
              seats.push({
                id: seatId,
                row,
                column: col,
                label: `R${row+1}C${col+1}`,
                studentId: assignment?.student?.id,
                studentName: assignment?.student 
                  ? `${assignment.student.firstName} ${assignment.student.lastName}`
                  : undefined
              })
            }
          }
          
          return {
            data: {
              id: plan.id,
              name: plan.name,
              courseId: plan.course?.id,
              courseName: plan.course?.name,
              isActive: plan.active || (plan.id === response.data.activePlanId),
              seats
            }
          }
        }
      }
      
      // If we couldn't find the plan, throw an error
      throw new Error(`Could not find seat plan with ID ${seatPlanId}`)
    } catch (error) {
      console.error(`Error fetching seat plan ${seatPlanId}:`, error)
      throw error
    }
  },

  // Create a new seat plan
  createSeatPlan: async (courseId: number, name: string, rows: number, columns: number) => {
    // Use courseId as the path parameter
    return axios.post(
      `${API_URL}/seats/${courseId}`,
      { name, rows, columns },
      { headers: getAuthHeader() }
    )
  },

  // Update a seat plan (name only)
  updateSeatPlan: async (seatPlanId: number, courseId: number, name: string) => {
    // Use courseId as the path parameter and include planId in the request body
    return axios.put(
      `${API_URL}/seats/${courseId}`,
      { id: seatPlanId, name },
      { headers: getAuthHeader() }
    )
  },

  // Delete a seat plan
  deleteSeatPlan: async (seatPlanId: number, courseId: number) => {
    // Use courseId as path parameter and planId as query parameter
    return axios.delete(`${API_URL}/seats/${courseId}`, {
      headers: getAuthHeader(),
      params: {
        planId: seatPlanId
      }
    })
  },

  // Set a seat plan as active
  setActiveSeatPlan: async (seatPlanId: number, courseId: number) => {
    // Use courseId as path parameter and include planId in the request body for setting active
    return axios.put(
      `${API_URL}/seats/${courseId}`,
      { id: seatPlanId, active: true },
      { headers: getAuthHeader() }
    )
  },

  // Assign a student to a seat
  assignStudentToSeat: async (seatPlanId: number, seatId: number, studentId: number, courseId: number) => {
    // Use the seatPlanId as a query parameter
    return axios.post(
      `${API_URL}/seats/${courseId}/assign`,
      {},
      { 
        headers: getAuthHeader(),
        params: {
          planId: seatPlanId,
          studentId,
          row: seatId, // This assumes seatId corresponds to a row value
          column: 0    // We'll need proper column info to make this work
        }
      }
    )
  },

  // Remove a student from a seat
  removeStudentFromSeat: async (seatPlanId: number, seatId: number, courseId: number) => {
    // Use courseId as path parameter
    return axios.delete(`${API_URL}/seats/${courseId}/assign`, {
      headers: getAuthHeader(),
      params: {
        planId: seatPlanId,
        studentId: seatId // This assumes seatId corresponds to studentId, which may need adjustment
      }
    })
  },

  // Update seat label
  updateSeatLabel: async (seatPlanId: number, seatId: number, label: string, courseId: number) => {
    // Use courseId as path parameter
    return axios.put(
      `${API_URL}/seats/${courseId}`,
      { 
        id: seatPlanId,
        seatUpdates: [{ id: seatId, label }]
      },
      { headers: getAuthHeader() }
    )
  }
}
