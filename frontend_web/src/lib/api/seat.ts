import axiosInstance from './axiosInstance';

export interface Seat {
  id: number;
  sectionId: number;
  studentId: number;
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
    const response = await axiosInstance.get(`/seats/${sectionId}`);
    return response.data;
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
  overrideSeat: async (sectionId: number, studentId: number, row: number, column: number): Promise<Seat> => {
    const response = await axiosInstance.post('/seats/override', {
      sectionId,
      studentId,
      row,
      column
    });
    return response.data;
  },
  
  // Get seat map for a section (combines seats with layout information)
  getSeatMap: async (sectionId: number): Promise<SeatMap> => {
    const response = await axiosInstance.get(`/seats/${sectionId}/map`);
    return response.data;
  }
};
