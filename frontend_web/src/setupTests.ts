// jest-dom adds custom jest matchers for asserting on DOM nodes.
// allows you to do things like:
// expect(element).toHaveTextContent(/react/i)
// learn more: https://github.com/testing-library/jest-dom
import '@testing-library/jest-dom';
import { configure } from '@testing-library/react';
import React from 'react';

// Configure React Testing Library
configure({ testIdAttribute: 'data-testid' });

// Mock for window.matchMedia
Object.defineProperty(window, 'matchMedia', {
  writable: true,
  value: jest.fn().mockImplementation(query => ({
    matches: false,
    media: query,
    onchange: null,
    addListener: jest.fn(), // Deprecated
    removeListener: jest.fn(), // Deprecated
    addEventListener: jest.fn(),
    removeEventListener: jest.fn(),
    dispatchEvent: jest.fn(),
  })),
});

// Mock for window.scrollTo
window.scrollTo = jest.fn();

// Mock for window.alert
window.alert = jest.fn();

// Mock for window.confirm
window.confirm = jest.fn(() => true);

// Mock for window.fetch
window.fetch = jest.fn();

// Mock for ResizeObserver
global.ResizeObserver = jest.fn().mockImplementation(() => ({
  observe: jest.fn(),
  unobserve: jest.fn(),
  disconnect: jest.fn(),
}));

// Mock for IntersectionObserver
global.IntersectionObserver = jest.fn().mockImplementation(() => ({
  observe: jest.fn(),
  unobserve: jest.fn(),
  disconnect: jest.fn(),
  root: null,
  rootMargin: '',
  thresholds: [],
}));

// Disable React error boundary error logging in tests
const originalError = console.error;
console.error = (...args: any[]) => {
  if (/Error boundaries should be used|React will try to recreate this component tree|Warning.*not wrapped in act/.test(args[0])) {
    // Suppress specific React error boundary and testing warnings
    return;
  }
  originalError.call(console, ...args);
};

// Mock for Google OAuth
jest.mock('@react-oauth/google', () => ({
  GoogleOAuthProvider: ({ children }: { children: React.ReactNode }) => children,
  useGoogleLogin: jest.fn(() => jest.fn()),
  googleLogout: jest.fn(),
}));

// Mock for axios
jest.mock('axios', () => ({
  create: jest.fn(() => ({
    interceptors: {
      request: { use: jest.fn(), eject: jest.fn() },
      response: { use: jest.fn(), eject: jest.fn() }
    },
    get: jest.fn(),
    post: jest.fn(),
    put: jest.fn(),
    delete: jest.fn(),
    patch: jest.fn()
  })),
  defaults: {
    adapter: jest.fn()
  }
}));
