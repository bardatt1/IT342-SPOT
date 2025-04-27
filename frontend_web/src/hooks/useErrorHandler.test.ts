import { renderHook, act } from '@testing-library/react';
import { useErrorHandler, tryCatch, tryCatchSync } from './useErrorHandler';

describe('useErrorHandler hook', () => {
  it('should initialize with default error state', () => {
    const { result } = renderHook(() => useErrorHandler());
    
    expect(result.current.hasError).toBe(false);
    expect(result.current.error).toBeNull();
    expect(result.current.info).toBe('');
  });
  
  it('should initialize with custom error state if provided', () => {
    const initialState = {
      hasError: true,
      error: new Error('Initial error'),
      info: 'Initial info'
    };
    
    const { result } = renderHook(() => useErrorHandler(initialState));
    
    expect(result.current.hasError).toBe(true);
    expect(result.current.error).toEqual(initialState.error);
    expect(result.current.info).toBe('Initial info');
  });
  
  it('should handle error when handleError is called', () => {
    const { result } = renderHook(() => useErrorHandler());
    const testError = new Error('Test error');
    
    act(() => {
      result.current.handleError(testError, 'Error context');
    });
    
    expect(result.current.hasError).toBe(true);
    expect(result.current.error).toEqual(testError);
    expect(result.current.info).toBe('Error context');
  });
  
  it('should convert non-Error objects to Error when handleError is called', () => {
    const { result } = renderHook(() => useErrorHandler());
    
    act(() => {
      result.current.handleError('String error', 'Error context');
    });
    
    expect(result.current.hasError).toBe(true);
    expect(result.current.error).toBeInstanceOf(Error);
    expect(result.current.error?.message).toBe('String error');
    expect(result.current.info).toBe('Error context');
  });
  
  it('should clear error state when clearError is called', () => {
    const initialState = {
      hasError: true,
      error: new Error('Initial error'),
      info: 'Initial info'
    };
    
    const { result } = renderHook(() => useErrorHandler(initialState));
    
    act(() => {
      result.current.clearError();
    });
    
    expect(result.current.hasError).toBe(false);
    expect(result.current.error).toBeNull();
    expect(result.current.info).toBe('');
  });
});

describe('tryCatch utility', () => {
  it('should return the result of the function when it succeeds', async () => {
    const mockFn = jest.fn().mockResolvedValue('success');
    const errorHandler = jest.fn();
    
    const result = await tryCatch(mockFn, errorHandler, 'Info');
    
    expect(result).toBe('success');
    expect(mockFn).toHaveBeenCalled();
    expect(errorHandler).not.toHaveBeenCalled();
  });
  
  it('should call errorHandler when the function throws', async () => {
    const testError = new Error('Test error');
    const mockFn = jest.fn().mockRejectedValue(testError);
    const errorHandler = jest.fn();
    
    const result = await tryCatch(mockFn, errorHandler, 'Error info');
    
    expect(result).toBeNull();
    expect(mockFn).toHaveBeenCalled();
    expect(errorHandler).toHaveBeenCalledWith(testError, 'Error info');
  });
});

describe('tryCatchSync utility', () => {
  it('should return the result of the function when it succeeds', () => {
    const mockFn = jest.fn().mockReturnValue('success');
    const errorHandler = jest.fn();
    
    const result = tryCatchSync(mockFn, errorHandler, 'Info');
    
    expect(result).toBe('success');
    expect(mockFn).toHaveBeenCalled();
    expect(errorHandler).not.toHaveBeenCalled();
  });
  
  it('should call errorHandler when the function throws', () => {
    const testError = new Error('Test error');
    const mockFn = jest.fn().mockImplementation(() => {
      throw testError;
    });
    const errorHandler = jest.fn();
    
    const result = tryCatchSync(mockFn, errorHandler, 'Error info');
    
    expect(result).toBeNull();
    expect(mockFn).toHaveBeenCalled();
    expect(errorHandler).toHaveBeenCalledWith(testError, 'Error info');
  });
});
