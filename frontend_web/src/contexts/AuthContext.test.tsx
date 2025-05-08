import { render, screen, waitFor, act } from '../test-utils';
import userEvent from '@testing-library/user-event';
import { AuthProvider, useAuth } from './AuthContext';
import axios from 'axios';

// Mock axios
jest.mock('axios');
const mockedAxios = axios as jest.Mocked<typeof axios>;

// Mock API URL (matching what's in AuthContext)
const API_URL = 'https://backend.spot-edu.me/api';

// Create a test component that uses the auth context
const TestComponent = () => {
  const { isAuthenticated, user, login, logout, isLoading } = useAuth();
  
  return (
    <div>
      <div data-testid="loading-state">{isLoading ? 'Loading' : 'Not Loading'}</div>
      <div data-testid="auth-state">{isAuthenticated ? 'Authenticated' : 'Not Authenticated'}</div>
      {user && <div data-testid="user-info">{user.name} ({user.role})</div>}
      
      <button 
        onClick={() => login('test@example.com', 'password123')}
        data-testid="login-button"
      >
        Login
      </button>
      
      <button 
        onClick={() => logout()}
        data-testid="logout-button"
      >
        Logout
      </button>
    </div>
  );
};

describe('AuthContext', () => {
  beforeEach(() => {
    // Clear mocks between tests
    jest.clearAllMocks();
    
    // Clear localStorage
    localStorage.clear();
  });
  
  it('provides initial unauthenticated state', () => {
    render(
      <AuthProvider>
        <TestComponent />
      </AuthProvider>
    );
    
    expect(screen.getByTestId('auth-state')).toHaveTextContent('Not Authenticated');
    expect(screen.getByTestId('loading-state')).toHaveTextContent('Not Loading');
    expect(screen.queryByTestId('user-info')).not.toBeInTheDocument();
  });
  
  it('updates state on successful login', async () => {
    // Mock successful login response
    mockedAxios.post.mockResolvedValueOnce({
      data: {
        token: 'fake-jwt-token',
        user: {
          id: 1,
          firstName: 'John',
          lastName: 'Doe',
          email: 'test@example.com',
          role: 'TEACHER'
        }
      }
    });
    
    render(
      <AuthProvider>
        <TestComponent />
      </AuthProvider>
    );
    
    // Click login button
    const loginButton = screen.getByTestId('login-button');
    act(() => {
      userEvent.click(loginButton);
    });
    
    // Check that login API was called
    expect(mockedAxios.post).toHaveBeenCalledWith(
      `${API_URL}/auth/login`,
      {
        email: 'test@example.com',
        password: 'password123'
      }
    );
    
    // Wait for auth state to update
    await waitFor(() => {
      expect(screen.getByTestId('auth-state')).toHaveTextContent('Authenticated');
    });
    
    // Check user info is displayed
    expect(screen.getByTestId('user-info')).toHaveTextContent('John Doe (TEACHER)');
    
    // Check that token was stored in localStorage
    expect(localStorage.getItem('auth_token')).toBe('fake-jwt-token');
  });
  
  it('handles login failure correctly', async () => {
    // Mock login failure
    mockedAxios.post.mockRejectedValueOnce({
      response: {
        status: 401,
        data: { message: 'Invalid credentials' }
      }
    });
    
    render(
      <AuthProvider>
        <TestComponent />
      </AuthProvider>
    );
    
    // Click login button
    const loginButton = screen.getByTestId('login-button');
    act(() => {
      userEvent.click(loginButton);
    });
    
    // Check that login API was called
    expect(mockedAxios.post).toHaveBeenCalled();
    
    // Auth state should remain not authenticated
    await waitFor(() => {
      expect(screen.getByTestId('auth-state')).toHaveTextContent('Not Authenticated');
    });
    
    // No user info should be displayed
    expect(screen.queryByTestId('user-info')).not.toBeInTheDocument();
    
    // No token should be stored
    expect(localStorage.getItem('token')).toBeNull();
  });
  
  it('clears auth state on logout', async () => {
    // Mock the JWT decode function to return a valid token
    jest.spyOn(require('jwt-decode'), 'jwtDecode').mockReturnValue({
      sub: 'test@example.com',
      exp: Math.floor(Date.now() / 1000) + 3600, // 1 hour from now
      role: 'TEACHER',
      userId: 1
    });
    
    // Setup authenticated state with token only
    localStorage.setItem('token', 'fake-jwt-token');
    
    // Mock the user api call that happens during init
    mockedAxios.get.mockResolvedValueOnce({
      // This should match the endpoint in AuthContext.tsx that fetches user data
      url: `${API_URL}/users/me`,
      data: {
        id: 1,
        firstName: 'John',
        lastName: 'Doe',
        email: 'test@example.com',
        role: 'TEACHER'
      }
    });
    
    render(
      <AuthProvider>
        <TestComponent />
      </AuthProvider>
    );
    
    // Initial state should be authenticated
    await waitFor(() => {
      expect(screen.getByTestId('auth-state')).toHaveTextContent('Authenticated');
    });
    expect(screen.getByTestId('user-info')).toHaveTextContent('John Doe (TEACHER)');
    
    // Click logout button
    const logoutButton = screen.getByTestId('logout-button');
    act(() => {
      userEvent.click(logoutButton);
    });
    
    // State should update to unauthenticated
    await waitFor(() => {
      expect(screen.getByTestId('auth-state')).toHaveTextContent('Not Authenticated');
    });
    
    // User info should be removed
    expect(screen.queryByTestId('user-info')).not.toBeInTheDocument();
    
    // LocalStorage should be cleared
    expect(localStorage.getItem('token')).toBeNull();
  });
});
