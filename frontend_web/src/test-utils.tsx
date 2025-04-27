import { ReactElement, ReactNode } from 'react';
import { render, RenderOptions } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';

// Mock for Google OAuth provider
const MockGoogleOAuthProvider = ({ children }: { children: ReactNode }) => {
  return <>{children}</>;
};

// Custom renderer that includes providers
const AllProviders = ({ children }: { children: ReactNode }) => {
  return (
    <MockGoogleOAuthProvider>
      <MemoryRouter initialEntries={["/"]}>
        {children}
      </MemoryRouter>
    </MockGoogleOAuthProvider>
  );
};

const customRender = (
  ui: ReactElement,
  options?: Omit<RenderOptions, 'wrapper'>,
) => render(ui, { wrapper: AllProviders, ...options });

// Mock localStorage
const localStorageMock = (() => {
  let store: Record<string, string> = {};
  return {
    getItem: (key: string) => store[key] || null,
    setItem: (key: string, value: string) => {
      store[key] = value.toString();
    },
    removeItem: (key: string) => {
      delete store[key];
    },
    clear: () => {
      store = {};
    }
  };
})();

// Replace global localStorage with mock
Object.defineProperty(window, 'localStorage', {
  value: localStorageMock
});

// Re-export everything from testing-library
export * from '@testing-library/react';

// Override render method
export { customRender as render };
