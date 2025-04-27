import { ReactNode } from 'react';

// Create mock implementations for react-router-dom functions and components
export const useNavigate = jest.fn();
export const useLocation = jest.fn().mockReturnValue({ pathname: '/' });
export const useParams = jest.fn().mockReturnValue({});
export const useSearchParams = jest.fn().mockReturnValue([new URLSearchParams(), jest.fn()]);

export const Link = ({ children, to, ...rest }: { children: ReactNode; to: string; [key: string]: any }) => (
  <a href={to} {...rest}>{children}</a>
);

export const Navigate = ({ to }: { to: string }) => <div data-testid="navigate" data-to={to} />;

export const Routes = ({ children }: { children: ReactNode }) => <>{children}</>;

export const Route = ({ path, element }: { path: string; element: ReactNode }) => (
  <div data-testid="route" data-path={path}>
    {element}
  </div>
);

export const MemoryRouter = ({ children, initialEntries = ['/'] }: { children: ReactNode; initialEntries?: string[] }) => (
  <div data-testid="memory-router" data-initial-entries={JSON.stringify(initialEntries)}>
    {children}
  </div>
);

export const Outlet = () => <div data-testid="outlet" />;
