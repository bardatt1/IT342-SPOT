import { render, screen } from '@testing-library/react';
import ErrorBoundary from './ErrorBoundary';
import ErrorFallback from './ErrorFallback';
import '@testing-library/jest-dom';

// Suppress React error boundary error console output
// This is needed because React will log errors thrown inside components
const originalError = console.error;
beforeAll(() => {
  console.error = jest.fn();
});

afterAll(() => {
  console.error = originalError;
});

// Simplified error throwing component with props to control when it throws
const ErrorThrowingComponent = ({ shouldThrow = true }: { shouldThrow?: boolean }) => {
  if (shouldThrow) {
    throw new Error('Test error');
  }
  return <div>No error thrown</div>;
};

describe('ErrorBoundary', () => {
  // This is crucial for testing error boundaries
  const originalConsoleError = console.error;
  
  beforeAll(() => {
    // Mock console.error to prevent noise in test output
    console.error = jest.fn();
  });
  
  afterAll(() => {
    console.error = originalConsoleError;
  });
  
  // Reset console.error mock after each test
  afterEach(() => {
    (console.error as jest.Mock).mockClear();
  });
  
  it('renders children when there is no error', () => {
    render(
      <ErrorBoundary>
        <div data-testid="child">Child Content</div>
      </ErrorBoundary>
    );
    
    expect(screen.getByTestId('child')).toBeInTheDocument();
    expect(screen.getByText('Child Content')).toBeInTheDocument();
  });

  it('renders default fallback UI when an error occurs', () => {
    // In React 18+, we need to handle error boundaries with special considerations for testing.
    // We'll deliberately suppress the React error boundary warning during component rendering.
    const spy = jest.spyOn(console, 'error');
    spy.mockImplementation(() => {});

    // Render the component tree with the error boundary
    render(
      <ErrorBoundary>
        <ErrorThrowingComponent shouldThrow={true} />
      </ErrorBoundary>
    );
    
    // No need to worry about act warnings in jest with our mocked console

    // Check that the error message is displayed
    expect(screen.getByText('Something went wrong')).toBeInTheDocument();
    expect(screen.getByText('An error occurred in this component.')).toBeInTheDocument();
    
    // Clean up the specific mock for this test
    spy.mockRestore();
  });

  it('renders custom fallback UI when error occurs', () => {
    // Same as above, handle React 18's error handling in tests
    const spy = jest.spyOn(console, 'error');
    spy.mockImplementation(() => {});
    
    // Create a simple custom fallback UI
    const customFallback = <div data-testid="custom-fallback">Custom Error UI</div>;

    render(
      <ErrorBoundary fallback={customFallback}>
        <ErrorThrowingComponent shouldThrow={true} />
      </ErrorBoundary>
    );
    
    // Check that the custom fallback UI is displayed
    expect(screen.getByTestId('custom-fallback')).toBeInTheDocument();
    expect(screen.getByText('Custom Error UI')).toBeInTheDocument();
    
    // Clean up the specific mock for this test
    spy.mockRestore();
  });

  it('renders ErrorFallback component correctly', () => {
    const error = new Error('Test error message');
    
    render(
      <ErrorFallback 
        error={error} 
        resetErrorBoundary={() => {}} 
        message="Test Error Message" 
        showDetails={true} 
      />
    );
    
    expect(screen.getByText('Test Error Message')).toBeInTheDocument();
    expect(screen.getByText(/Test error message/)).toBeInTheDocument();
    expect(screen.getByRole('button')).toBeInTheDocument();
    expect(screen.getByText('We encountered an error while processing your request. Please try again.')).toBeInTheDocument();
  });
});
