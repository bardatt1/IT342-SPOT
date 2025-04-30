import axiosInstance from './axiosInstance';

import { Student } from './student';

export interface Seat {
  id: number;
  sectionId: number;
  studentId?: number; // Mark as optional since the API might use student object instead
  student?: Student; // Add the student object that's actually returned by the API
  column: number;
  row: number;
}

export interface SeatMap {
  rows: number;
  columns: number;
  seats: Seat[];
}

export const seatApi = {
  // Get all seats for a section
  getSectionSeats: async (sectionId: number): Promise<Seat[]> => {
    try {
      const response = await axiosInstance.get('/seats', {
        params: { sectionId }
      });
      return response.data?.data || [];
    } catch (error) {
      console.error(`Error fetching seats for section ${sectionId}:`, error);
      return [];
    }
  },
  
  // Pick a seat (student action)
  pickSeat: async (sectionId: number, studentId: number, row: number, column: number): Promise<Seat> => {
    const response = await axiosInstance.post('/seats/pick', {
      sectionId,
      studentId,
      row,
      column
    });
    return response.data;
  },
  
  // Override a student's seat (teacher action)
  overrideSeat: async (sectionId: number, studentId: number, row: number, column: number, teacherId: number): Promise<Seat> => {
    const response = await axiosInstance.post('/seats/override', {
      sectionId,
      studentId,
      row,
      column,
      teacherId // Add the teacher ID to the request payload
    });
    return response.data?.data || null;
  },
  
  // Get seat map for a section (combines seats with layout information)
  getSeatMap: async (sectionId: number): Promise<SeatMap> => {
    try {
      // Based on API list, we need to check if this endpoint is `/api/seats/section/{sectionId}`
      const response = await axiosInstance.get('/seats', {
        params: { sectionId }
      });
      
      // Transform response to expected SeatMap format if needed
      const seats = response.data?.data || [];
      console.log('Seat data received:', seats);
      
      // Return a fixed structure with 5 rows by 6 columns
      return {
        rows: 5,
        columns: 6,
        seats: seats
      };
    } catch (error) {
      console.error(`Error fetching seat map for section ${sectionId}:`, error);
      // Return an empty seat map as fallback
      return { rows: 0, columns: 0, seats: [] };
    }
  }
};
