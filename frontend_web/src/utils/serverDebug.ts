/**
 * Utility for diagnosing server-side issues
 */
import { env } from '@/config/env';

// This interface represents the shape of endpoint test results
// The result objects follow this structure even though we're not explicitly typing them
interface ServerTestResults {
  error?: string;
  tokenInfo?: {
    payload?: any;
    expiresAt?: string;
    isExpired?: boolean | string;
    role?: string;
    userId?: number;
    subject?: string;
    error?: string;
  };
  [key: string]: any;
}

/**
 * Makes direct fetch calls to test endpoints with different authentication approaches
 */
export const testServerEndpoints = async (): Promise<ServerTestResults> => {
  const results: ServerTestResults = {};
  const token = localStorage.getItem('token');
  const userJson = localStorage.getItem('user');
  
  if (!token) {
    return { error: 'No token available in localStorage' };
  }
  
  try {
    // Log the user data for debugging
    if (userJson) {
      try {
        const userData = JSON.parse(userJson);
        results.userInfo = {
          id: userData.id,
          email: userData.email,
          role: userData.role,
        };
      } catch (e) {
        results.userInfo = { error: 'Failed to parse user data' };
      }
    }
    // Test 1: Standard Bearer token
    console.log('Test 1: Standard Bearer token');
    const test1 = await fetch(`${env.apiUrl}/api/courses`, {
      method: 'GET',
      headers: {
        'Authorization': `Bearer ${token}`,
        'Accept': 'application/json',
        'Content-Type': 'application/json'
      },
      credentials: 'include'
    });
    
    results.test1 = {
      status: test1.status,
      statusText: test1.statusText,
      ok: test1.ok,
      headers: Object.fromEntries(test1.headers.entries()),
      data: test1.ok ? await test1.json().catch(() => 'Error parsing JSON') : 'N/A'
    };
    
    // Test 2: Token without Bearer prefix
    console.log('Test 2: Token without Bearer prefix');
    const test2 = await fetch(`${env.apiUrl}/api/courses`, {
      method: 'GET',
      headers: {
        'Authorization': token,
        'Accept': 'application/json',
        'Content-Type': 'application/json'
      },
      credentials: 'include'
    });
    
    results.test2 = {
      status: test2.status,
      statusText: test2.statusText,
      ok: test2.ok,
      headers: Object.fromEntries(test2.headers.entries()),
      data: test2.ok ? await test2.json().catch(() => 'Error parsing JSON') : 'N/A'
    };
    
    // Test 3: Using a non-protected endpoint (if available)
    console.log('Test 3: Public endpoint test');
    const test3 = await fetch(`${env.apiUrl}/api/public/health`, {
      method: 'GET',
      headers: {
        'Accept': 'application/json',
        'Content-Type': 'application/json'
      }
    });
    
    // Test 3b: Try auth/me endpoint
    console.log('Test 3b: Testing auth/me endpoint');
    const test3b = await fetch(`${env.apiUrl}/api/auth/me`, {
      method: 'GET',
      headers: {
        'Authorization': `Bearer ${token}`,
        'Accept': 'application/json',
        'Content-Type': 'application/json'
      },
      credentials: 'include'
    });
    
    results.test3 = {
      status: test3.status,
      statusText: test3.statusText,
      ok: test3.ok,
      headers: Object.fromEntries(test3.headers.entries()),
      data: test3.ok ? await test3.json().catch(() => 'Error parsing JSON') : 'N/A'
    };
    
    results.test3b = {
      status: test3b.status,
      statusText: test3b.statusText,
      ok: test3b.ok,
      headers: Object.fromEntries(test3b.headers.entries()),
      data: test3b.ok ? await test3b.json().catch(() => 'Error parsing JSON') : 'N/A'
    };
    
    // Test 4: Using OPTIONS request to check CORS
    console.log('Test 4: CORS preflight check');
    const test4 = await fetch(`${env.apiUrl}/api/courses`, {
      method: 'OPTIONS',
      headers: {
        'Accept': 'application/json',
        'Content-Type': 'application/json',
        'Access-Control-Request-Method': 'GET',
        'Access-Control-Request-Headers': 'Authorization,Content-Type',
        'Origin': window.location.origin
      }
    });
    
    // Test 5: Using token as URL parameter (query string)
    console.log('Test 5: Token as query parameter');
    const test5 = await fetch(`${env.apiUrl}/api/courses?token=${encodeURIComponent(token)}`, {
      method: 'GET',
      headers: {
        'Accept': 'application/json',
        'Content-Type': 'application/json'
      },
      credentials: 'include'
    });
    
    // Test 6: Using a different protected endpoint
    console.log('Test 6: Testing sessions endpoint');
    const test6 = await fetch(`${env.apiUrl}/api/sessions`, {
      method: 'GET',
      headers: {
        'Authorization': `Bearer ${token}`,
        'Accept': 'application/json',
        'Content-Type': 'application/json'
      },
      credentials: 'include'
    });
    
    // Test 7: POST request to /api/auth/validate
    console.log('Test 7: Testing explicit token validation');
    const test7 = await fetch(`${env.apiUrl}/api/auth/validate`, {
      method: 'POST',
      headers: {
        'Accept': 'application/json',
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({ token }),
      credentials: 'include'
    });
    
    results.test4 = {
      status: test4.status,
      statusText: test4.statusText,
      ok: test4.ok,
      headers: Object.fromEntries(test4.headers.entries())
    };
    
    results.test5 = {
      status: test5.status,
      statusText: test5.statusText,
      ok: test5.ok,
      headers: Object.fromEntries(test5.headers.entries()),
      data: test5.ok ? await test5.json().catch(() => 'Error parsing JSON') : 'N/A'
    };
    
    results.test6 = {
      status: test6.status,
      statusText: test6.statusText,
      ok: test6.ok,
      headers: Object.fromEntries(test6.headers.entries()),
      data: test6.ok ? await test6.json().catch(() => 'Error parsing JSON') : 'N/A'
    };
    
    results.test7 = {
      status: test7.status,
      statusText: test7.statusText,
      ok: test7.ok,
      headers: Object.fromEntries(test7.headers.entries()),
      data: test7.ok ? await test7.json().catch(() => 'Error parsing JSON') : 'N/A'
    };
    
    // Decode the token to examine its contents
    try {
      const tokenParts = token.split('.');
      if (tokenParts.length === 3) {
        const payload = JSON.parse(atob(tokenParts[1]));
        results.tokenInfo = {
          payload,
          expiresAt: payload.exp ? new Date(payload.exp * 1000).toISOString() : 'Unknown',
          isExpired: payload.exp ? (payload.exp * 1000 < Date.now()) : 'Unknown',
          role: payload.role || 'No role found',
          userId: payload.userId,
          subject: payload.sub
        };
      }
    } catch (e) {
      results.tokenInfo = { error: 'Failed to decode token' };
    }
    
    return results;
  } catch (error: any) {
    console.error('Server diagnostics failed:', error);
    return {
      error: error.message,
      results
    };
  }
};

/**
 * Checks specific role-based access patterns
 */
export const checkRoleBasedAccess = async (): Promise<Record<string, any>> => {
  const results: Record<string, any> = {};
  const token = localStorage.getItem('token');
  const userString = localStorage.getItem('user');
  
  if (!token) {
    return { error: 'No token available in localStorage' };
  }
  
  try {
    // Get user info
    let role = 'unknown';
    if (userString) {
      try {
        const user = JSON.parse(userString);
        role = user.role || 'unknown';
        results.userRole = role;
      } catch (e) {
        results.userRole = { error: 'Failed to parse user data' };
      }
    }
    
    // Test endpoints that should be accessible to different roles
    const endpoints = [
      { name: 'courses', url: `${env.apiUrl}/api/courses`, expectedRoles: ['ADMIN', 'TEACHER'] },
      { name: 'sessions', url: `${env.apiUrl}/api/sessions`, expectedRoles: ['ADMIN', 'TEACHER'] },
      { name: 'students', url: `${env.apiUrl}/api/students`, expectedRoles: ['ADMIN', 'TEACHER'] },
      { name: 'teachers', url: `${env.apiUrl}/api/teachers`, expectedRoles: ['ADMIN'] },
    ];
    
    for (const endpoint of endpoints) {
      console.log(`Testing ${endpoint.name} endpoint (allowed roles: ${endpoint.expectedRoles.join(', ')})`);
      const response = await fetch(endpoint.url, {
        method: 'GET',
        headers: {
          'Authorization': `Bearer ${token}`,
          'Accept': 'application/json',
          'Content-Type': 'application/json'
        },
        credentials: 'include'
      });
      
      results[endpoint.name] = {
        url: endpoint.url,
        status: response.status,
        statusText: response.statusText,
        ok: response.ok,
        shouldHaveAccess: endpoint.expectedRoles.includes(role),
        accessGranted: response.ok,
        data: response.ok ? await response.json().catch(() => 'Error parsing JSON') : 'N/A'
      };
    }
    
    return results;
  } catch (error: any) {
    console.error('Role-based access check failed:', error);
    return {
      error: error.message,
      results
    };
  }
};

export default { testServerEndpoints, checkRoleBasedAccess };
