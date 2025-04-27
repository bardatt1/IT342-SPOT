// Mock Axios for testing
const mockAxios = {
  create: jest.fn(() => mockAxios),
  get: jest.fn().mockResolvedValue({ data: {} }),
  post: jest.fn().mockResolvedValue({ 
    data: { 
      token: 'mock-jwt-token',
      user: {
        id: 1,
        firstName: 'Test',
        lastName: 'User',
        email: 'test@example.com',
        role: 'TEACHER'
      }
    }
  }),
  put: jest.fn().mockResolvedValue({ data: {} }),
  delete: jest.fn().mockResolvedValue({ data: {} }),
  interceptors: {
    request: {
      use: jest.fn(),
      eject: jest.fn()
    },
    response: {
      use: jest.fn(),
      eject: jest.fn()
    }
  },
  defaults: {
    baseURL: '',
    headers: {
      common: {}
    }
  }
};

export default mockAxios;
