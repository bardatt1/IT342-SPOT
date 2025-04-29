import axiosInstance from './axiosInstance';

export interface Schedule {
  id: number;
  section: {
    id: number;
    sectionName: string;
  };
  dayOfWeek: number; // 1 = Monday, 7 = Sunday
  timeStart: string;
  timeEnd: string;
  room: string;
  scheduleType: string; // LEC, LAB
}

export interface ScheduleCreateDto {
  sectionId: number;
  dayOfWeek: number;
  timeStart: string;
  timeEnd: string;
  room: string;
  scheduleType: string;
}

export interface ScheduleUpdateDto {
  dayOfWeek: number;
  timeStart: string;
  timeEnd: string;
  room: string;
  scheduleType: string;
}

// Days of week mapping for display
export const daysOfWeek = [
  { value: 1, label: 'Monday' },
  { value: 2, label: 'Tuesday' },
  { value: 3, label: 'Wednesday' },
  { value: 4, label: 'Thursday' },
  { value: 5, label: 'Friday' },
  { value: 6, label: 'Saturday' },
  { value: 7, label: 'Sunday' }
];

// Schedule types
export const scheduleTypes = [
  { value: 'LEC', label: 'Lecture' },
  { value: 'LAB', label: 'Laboratory' },
  { value: 'REC', label: 'Recitation' }
];

// Helper function to format LocalTime from backend to string (HH:mm format)
const formatTimeFromBackend = (timeString: string): string => {
  if (!timeString) return '';
  
  // Handle ISO time format that includes seconds/milliseconds
  if (timeString.includes('T')) {
    // Extract time from ISO format
    const timePart = timeString.split('T')[1];
    return timePart.substring(0, 5); // Just take HH:MM
  }
  
  // Handle simple time format (HH:mm:ss)
  if (timeString.includes(':')) {
    const parts = timeString.split(':');
    return `${parts[0]}:${parts[1]}`; // Return HH:MM
  }
  
  return timeString;
};

const mapScheduleFromApi = (data: any): Schedule => {
  if (!data) {
    // Return an empty Schedule object if data is null
    return {
      id: 0,
      section: {
        id: 0,
        sectionName: ''
      },
      dayOfWeek: 1,
      timeStart: '',
      timeEnd: '',
      room: 'No Room',
      scheduleType: 'LEC'
    };
  }
  
  return {
    id: data.id,
    section: {
      id: data.sectionId || (data.section?.id || 0),
      sectionName: data.sectionName || (data.section?.sectionName || '')
    },
    dayOfWeek: data.dayOfWeek,
    timeStart: formatTimeFromBackend(data.timeStart),
    timeEnd: formatTimeFromBackend(data.timeEnd),
    room: data.room || 'No Room',
    scheduleType: data.scheduleType || 'LEC'
  };
};

export const scheduleApi = {
  // Get all schedules for a section
  getBySectionId: async (sectionId: number): Promise<Schedule[]> => {
    try {
      // Using the correct endpoint with query parameter for section ID
      const response = await axiosInstance.get('/schedules', {
        params: { sectionId }
      });
      const schedules = response.data?.data || [];
      console.log('Schedules fetched for section', sectionId, ':', schedules);
      return schedules.map(mapScheduleFromApi);
    } catch (error) {
      console.error('Error fetching schedules:', error);
      return [];
    }
  },

  // Get a schedule by id
  getById: async (id: number): Promise<Schedule | null> => {
    try {
      const response = await axiosInstance.get(`/schedules/${id}`);
      return mapScheduleFromApi(response.data?.data);
    } catch (error) {
      console.error(`Error fetching schedule ${id}:`, error);
      return null;
    }
  },

  // Create a new schedule
  create: async (schedule: ScheduleCreateDto): Promise<Schedule | null> => {
    try {
      console.log('Creating schedule with data:', schedule);
      
      // Create a copy of the schedule to avoid modifying the original
      const scheduleData = {
        ...schedule,
        // Ensure time format is valid for backend LocalTime (HH:mm)
        timeStart: schedule.timeStart, 
        timeEnd: schedule.timeEnd
      };
      
      const response = await axiosInstance.post('/schedules', scheduleData);
      console.log('Schedule creation response:', response.data);
      return mapScheduleFromApi(response.data?.data);
    } catch (error) {
      console.error('Error creating schedule:', error);
      throw error;
    }
  },

  // Update a schedule
  update: async (id: number, schedule: ScheduleUpdateDto): Promise<Schedule | null> => {
    try {
      console.log(`Updating schedule ${id} with data:`, schedule);
      
      // Create a copy of the schedule to avoid modifying the original
      const scheduleData = {
        ...schedule,
        // Ensure time format is valid for backend LocalTime (HH:mm)
        timeStart: schedule.timeStart,
        timeEnd: schedule.timeEnd
      };
      
      const response = await axiosInstance.put(`/schedules/${id}`, scheduleData);
      console.log('Schedule update response:', response.data);
      return mapScheduleFromApi(response.data?.data);
    } catch (error) {
      console.error(`Error updating schedule ${id}:`, error);
      throw error;
    }
  },

  // Delete a schedule
  delete: async (id: number): Promise<boolean> => {
    try {
      const response = await axiosInstance.delete(`/schedules/${id}`);
      return response.data?.data || false;
    } catch (error) {
      console.error(`Error deleting schedule ${id}:`, error);
      throw error;
    }
  }
};
