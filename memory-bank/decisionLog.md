# Decision Log

## Technical Decisions

[2025-04-30 19:52:00] - **Class Code Generation Implementation**
- **Decision**: Implement client-side class code generation for teacher sections
- **Rationale**: Teachers need to easily generate enrollment keys for their sections
- **Implementation**: Added generateClassCode method to sectionApi that generates a random 6-character alphanumeric code and updates the section via API
- **Alternatives**: Server-side code generation, fixed code patterns, manual code entry
- **Implications**: Improved teacher workflow, better security with random codes, potential for collisions mitigated by uniqueness checks

[2025-04-30 19:52:00] - **Student Information Display in Seat Management**
- **Decision**: Use IIFE pattern to resolve issues with multiple function calls in JSX
- **Rationale**: Multiple calls to findStudentBySeat were causing inconsistent displays and empty student names
- **Implementation**: Added immediately-invoked function expression to store student data in local variable before rendering
- **Alternatives**: Create separate component, use React effect hooks, move logic to parent component
- **Implications**: More reliable display of student information, better performance by reducing redundant function calls

[2025-04-27 16:59:43] - **Frontend Framework Selection**
- Decision: Use Vite with React and TypeScript for the web frontend
- Rationale: Vite provides fast development experience, React is industry standard, and TypeScript ensures type safety
- Implications: Better developer experience, type safety, and maintainability

[2025-04-27 16:59:43] - **UI Component Library**
- Decision: Use Shadcn UI with Tailwind CSS
- Rationale: Shadcn provides high-quality, accessible components that can be easily customized with Tailwind
- Implications: Consistent UI, faster development, and better maintainability

[2025-04-27 16:59:43] - **Authentication Strategy**
- Decision: Implement JWT for authentication with Google OAuth integration
- Rationale: JWT provides secure, stateless authentication, while Google OAuth allows for easy third-party login
- Implications: Secure authentication system that follows industry standards

### Authentication Approach
- **Decision**: Implement JWT-based authentication with Google OAuth integration
- **Rationale**: JWT provides stateless authentication that works well with our architecture. Google OAuth adds convenience and security.
- **Alternatives**: Session-based authentication, Auth0, Firebase Auth
- **Implications**: Need to implement token refresh, secure storage, and proper validation

[2025-04-27 23:56:45] - **API Endpoint Standardization**
- **Decision**: Align all frontend API calls with backend endpoint naming conventions
- **Rationale**: Frontend was using singular resource names (/section, /student) while backend expected plural (/sections, /students)
- **Implementation**: Updated all service modules to use correct endpoint paths matching backend documentation
- **Alternatives**: Customize backend routing to support both formats, add proxy layer to translate URLs
- **Implications**: Improved API reliability, eliminated 500/404 errors, standardized API access patterns

[2025-04-28 01:58:42] - **Password Validation with Bcrypt Hashing**
- **Decision**: Use pre-hashed bcrypt passwords for user creation instead of plaintext passwords
- **Rationale**: Backend validation requires passwords to be bcrypt-hashed, not plaintext, causing 400 errors when creating users
- **Implementation**: Added default bcrypt-hashed password for user creation, updated form to make password optional with default value, improved user feedback
- **Alternatives**: Implement client-side bcrypt hashing (more complex and requires additional libraries), modify backend to accept plaintext passwords
- **Implications**: Simplified user creation process, better security by using hashed passwords, improved user experience with default credentials

[2025-04-28 02:01:46] - **Temporary Account Creation System**
- **Decision**: Implement a streamlined temporary account creation system where only Physical ID is required
- **Rationale**: Creates a more efficient process for admins to generate draft accounts that users will update themselves
- **Implementation**: Made Physical ID the only required field, generated temporary passwords using pattern `[First 5 digits of Physical ID]TEMP`, populated other fields with default values
- **Alternatives**: Require all fields at creation time, use random password generation, implement email verification system
- **Implications**: Faster user onboarding, predictable credential system for first-time users, need for first-login detection to prompt information updates

[2025-04-28 02:07:03] - **Teacher Dashboard API Parameter Fix**
- **Decision**: Add a dedicated method to fetch sections by teacher ID with proper query parameters
- **Rationale**: Backend requires a `teacherId` parameter when teachers access sections, causing 500 errors when missing
- **Implementation**: Created `getByTeacherId` method in section API that passes the ID as a query parameter, updated teacher dashboard to use this method
- **Alternatives**: Modify backend to support requests without teacherId, implement middleware adapter, change authentication flow
- **Implications**: Fixed 500 errors in teacher dashboard, better alignment with backend expectations, more maintainable API structure

[2025-04-28 01:43:21] - **Comprehensive API Error Handling**
- **Decision**: Enhance all API operations with detailed error handling and connect UI to API services
- **Rationale**: UserManagement component wasn't actually calling API services, and API calls lacked proper error handling and diagnostics
- **Implementation**: Added try-catch blocks with detailed error logging to all API operations, connected UserManagement to API services, fixed type issues for optional fields
- **Alternatives**: Create middleware error interceptor, implement global error handling at a higher level, create mock backend for testing
- **Implications**: Improved error diagnostics, better developer experience, more reliable creation operations, easier troubleshooting

[2025-04-28 01:26:11] - **API Create Operations Fix**
- **Decision**: Add missing create operations and fix endpoint URLs for all API services
- **Rationale**: Create operations were missing or using incorrect endpoints, resulting in 200 responses but no actual data creation
- **Implementation**: Added proper create functions to student/teacher API services with correct /admin/create-* endpoints, fixed all endpoint URLs, and standardized response handling for nested data structures
- **Alternatives**: Implement middleware adapter, create mock data services for development, modify backend to match frontend expectations
- **Implications**: Fully functional create operations, consistent response structure handling, improved API reliability

[2025-04-28 01:09:25] - **API Endpoint Structure Alignment**
- **Decision**: Refactor API service files to match backend endpoint structure and add error handling
- **Rationale**: Frontend was using incorrect endpoints causing 500 errors; need to align with actual backend routes and response structures
- **Implementation**: Updated teacher, student, and section API calls to use correct admin endpoints, added fallbacks for response parsing, implemented error handling
- **Alternatives**: Create middleware proxy layer, modify backend to match frontend expectations, implement mock data for development
- **Implications**: More resilient front-end, better error handling, unified response structure handling

[2025-04-28 00:31:36] - **Direct Database Authentication**
- **Decision**: Abandon token bypass approach entirely and use direct database authentication
- **Rationale**: Database contains actual user data that should be used for authentication; token bypass was a workaround that's no longer needed
- **Implementation**: Modify authentication flow to use direct API calls for login and session management, properly handle token validation with the backend
- **Alternatives**: Continue with token bypass workarounds, implement client-side mocking, create separate authentication microservice
- **Implications**: More robust and secure authentication, improved data consistency, better alignment with backend expectations

[2025-04-28 00:24:41] - **Authentication State Persistence**
- **Decision**: Eliminate token bypass API calls and use localStorage for authentication state persistence
- **Rationale**: Token bypass login approach (making API calls with dummy credentials) was causing 401 errors during initialization
- **Implementation**: Store complete user data in localStorage during initial login, retrieve directly during initialization instead of making API calls
- **Alternatives**: Modify backend to accept token-based re-authentication, implement separate user data endpoint, create custom middleware
- **Implications**: More reliable authentication flow, elimination of unnecessary API calls, improved user experience without logout redirects

[2025-04-28 00:19:36] - **API Response Structure Standardization**
- **Decision**: Refactor authentication flow to handle nested API response structure
- **Rationale**: Backend returns {result, message, data} where data contains the actual payload, causing undefined field errors in the frontend
- **Implementation**: Updated response extraction to handle nested structure, corrected field mapping (userType → role, firstName+lastName → name), added robust fallbacks
- **Alternatives**: Modify backend to match frontend expectations, create middleware adapter, add response transformation layer
- **Implications**: More resilient frontend that can handle backend response format variations, clearer error messages, more reliable authentication

[2025-04-28 00:13:06] - **Enhanced API Error Handling and Debugging**
- **Decision**: Implement comprehensive error logging and fallback mechanisms for API requests
- **Rationale**: API calls were failing with 401 and 500 errors without sufficient diagnostic information
- **Implementation**: Enhanced axiosInstance with detailed request/response logging, improved AuthContext with better token validation, implemented timeout handling
- **Alternatives**: Server-side logging only, middleware approach, external monitoring tools
- **Implications**: Better visibility into API issues, more resilient error recovery, easier debugging of authorization problems

[2025-04-27 23:47:14] - **Authentication Flow Redesign**
- **Decision**: Implement hybrid token-API approach for authentication that combines JWT token validation with direct API calls
- **Rationale**: JWT tokens from backend contained minimal information (email, iat, exp) without role or user ID which are needed for authorization
- **Implementation**: Extract email from token during initialization, make login API call to get complete user data, implement fallbacks for errors
- **Alternatives**: Modify backend JWT payload, create separate user info endpoint, store complete user data in localStorage
- **Implications**: More resilient authentication with better error recovery at the cost of slightly increased API load

### UI Component Framework
- **Decision**: Use Shadcn UI with Tailwind CSS
- **Rationale**: Shadcn provides accessible, customizable components that work well with React and Tailwind
- **Alternatives**: Material UI, Chakra UI, Ant Design
- **Implications**: Need to ensure proper Tailwind configuration

### State Management
- **Decision**: Use React Context for global state (auth, theme)
- **Rationale**: The application's state needs are relatively simple and don't warrant a more complex solution
- **Alternatives**: Redux, Zustand, Jotai
- **Implications**: Potential performance considerations with Context for complex states

### API Communication
- **Decision**: Use Axios with interceptors for API calls
- **Rationale**: Axios provides a clean API, interceptors for auth tokens, and good error handling
- **Alternatives**: Fetch API, React Query, SWR
- **Implications**: Need to handle token expiration and refresh in interceptors

### Analytics Implementation
- **Decision**: Create a dedicated Analytics page for teachers with visualizations and exports
- **Rationale**: Provides teachers with insights into attendance patterns and student participation
- **Alternatives**: Embed analytics in other pages, use third-party analytics tools
- **Implications**: Need to ensure efficient data retrieval and processing for analytics

### Seat Management Implementation
- **Decision**: Implement a visual seat map for teachers to manage classroom seating
- **Rationale**: Helps teachers organize their classroom and track where students are sitting
- **Alternatives**: Text-based seat assignments, no seat management
- **Implications**: Need to handle concurrent seat assignments and updates

[2025-04-27 17:01:22] - Initial decision log created
[2025-04-27 17:39:00] - Added analytics and seat management decisions
