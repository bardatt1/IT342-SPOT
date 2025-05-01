import { useState, useCallback } from 'react';

interface ErrorState {
  hasError: boolean;
  error: Error | null;
  info: string;
}

export function useErrorHandler(initialState: ErrorState = { hasError: false, error: null, info: '' }) {
  const [errorState, setErrorState] = useState<ErrorState>(initialState);

  const handleError = useCallback((error: unknown, info: string = '') => {
    console.error('Error caught by useErrorHandler:', error, info);
    
    if (error instanceof Error) {
      setErrorState({
        hasError: true,
        error,
        info
      });
    } else {
      setErrorState({
        hasError: true,
        error: new Error(String(error)),
        info
      });
    }
  }, []);

  const clearError = useCallback(() => {
    setErrorState({
      hasError: false,
      error: null,
      info: ''
    });
  }, []);

  return {
    ...errorState,
    handleError,
    clearError
  };
}

// This function can be used in async functions to wrap the error handling
export async function tryCatch<T>(
  fn: () => Promise<T>,
  errorHandler: (error: unknown, info?: string) => void,
  errorInfo: string = ''
): Promise<T | null> {
  try {
    return await fn();
  } catch (error) {
    errorHandler(error, errorInfo);
    return null;
  }
}

// This function can be used to safely execute synchronous functions
export function tryCatchSync<T>(
  fn: () => T,
  errorHandler: (error: unknown, info?: string) => void,
  errorInfo: string = ''
): T | null {
  try {
    return fn();
  } catch (error) {
    errorHandler(error, errorInfo);
    return null;
  }
}
