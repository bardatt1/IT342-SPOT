import axiosInstance from './axiosInstance';

export interface Admin {
  id: number;
  firstName: string;
  middleName?: string;
  lastName: string;
  email: string;
  systemAdmin: boolean;
}

export const adminApi = {
  getAll: async (): Promise<Admin[]> => {
    try {
      const response = await axiosInstance.get('/system-admin/admins');
      return response.data?.data || [];
    } catch (error) {
      console.error('Error fetching all admins:', error);
      throw error;
    }
  },

  getAllSystemAdmins: async (): Promise<Admin[]> => {
    try {
      const response = await axiosInstance.get('/system-admin/system-admins');
      return response.data?.data || [];
    } catch (error) {
      console.error('Error fetching all system admins:', error);
      throw error;
    }
  },

  getById: async (id: number): Promise<Admin> => {
    try {
      const response = await axiosInstance.get(`/admin/${id}`);
      return response.data?.data;
    } catch (error) {
      console.error(`Error fetching admin with id ${id}:`, error);
      throw error;
    }
  },

  createAdmin: async (adminData: any): Promise<Admin> => {
    try {
      const response = await axiosInstance.post('/system-admin/create-admin', adminData);
      return response.data?.data;
    } catch (error) {
      console.error('Error creating admin:', error);
      throw error;
    }
  },

  createSystemAdmin: async (adminData: any): Promise<Admin> => {
    try {
      const response = await axiosInstance.post('/system-admin/create-system-admin', adminData);
      return response.data?.data;
    } catch (error) {
      console.error('Error creating system admin:', error);
      throw error;
    }
  },

  promoteToSystemAdmin: async (id: number): Promise<Admin> => {
    try {
      const response = await axiosInstance.put(`/system-admin/promote/${id}`);
      return response.data?.data;
    } catch (error) {
      console.error(`Error promoting admin with id ${id} to system admin:`, error);
      throw error;
    }
  },

  demoteFromSystemAdmin: async (id: number): Promise<Admin> => {
    try {
      const response = await axiosInstance.put(`/system-admin/demote/${id}`);
      return response.data?.data;
    } catch (error) {
      console.error(`Error demoting system admin with id ${id}:`, error);
      throw error;
    }
  },

  deleteAdmin: async (id: number): Promise<boolean> => {
    try {
      const response = await axiosInstance.delete(`/system-admin/admin/${id}`);
      return response.data?.data || false;
    } catch (error) {
      console.error(`Error deleting admin with id ${id}:`, error);
      throw error;
    }
  }
};
