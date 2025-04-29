# System Patterns

## Coding Patterns

### Component Patterns
- **Functional Components**: All React components are implemented as functional components with TypeScript
- **Hooks Pattern**: useState, useEffect, useContext, and custom hooks for state management and side effects
- **Render Props/Children as Function**: Used in `ProtectedRoute` component to allow passing user data to child components
- **Compound Components**: Used for complex UI elements like forms and dashboards
- **Container/Presentation Pattern**: Separating data fetching logic from presentation components

### State Management
- **Context API**: Used for global state (auth, theme) through the AuthContext
- **Local Component State**: useState for component-specific state
- **Form State Management**: Controlled components for form inputs

### Styling Patterns
- **Utility-First CSS**: Tailwind CSS for styling components
- **Component Libraries**: Shadcn UI components as base building blocks
- **Responsive Design**: Mobile-first approach with responsive breakpoints
- **CSS Variables**: Used for theming and consistent styling

### TypeScript Patterns
- **Interface-First Development**: Define interfaces before implementation
- **Type Guards**: For safe type narrowing
- **Union Types**: For state management (e.g., loading/error/success states)
- **Generics**: For reusable components and functions

## Architecture Patterns

### Authentication & Authorization
- **JWT Authentication**: Token-based auth with secure storage
- **Protected Routes**: Route-based access control using the `ProtectedRoute` component
- **Role-Based Access Control**: Different interfaces for Admin, Teacher (Student is mobile-only)
- **OAuth Integration**: Google authentication for seamless login

### API Communication
- **Axios Instance**: Centralized API client with interceptors
- **API Service Modules**: Separate modules for each entity (student, teacher, course, etc.)
- **Data Transformation**: Converting between API and UI data models
- **Error Handling**: Consistent error capture and display

### Application Structure
- **Feature-Based Organization**: Code organized by feature (auth, admin, teacher)
- **Shared UI Components**: Common UI elements in a shared directory
- **Route-Based Code Splitting**: Each route as a separate bundle
- **Lazy Loading**: Import components only when needed

## Testing Patterns
- **Component Testing**: React Testing Library for component tests
- **API Mocking**: Mock API responses for predictable tests
- **End-to-End Testing**: Critical user flows tested with Cypress
- **Snapshot Testing**: For UI regression testing

[2025-04-27 16:59:43] - Initial system patterns definition
[2025-04-27 17:45:12] - Updated with detailed implementation patterns
