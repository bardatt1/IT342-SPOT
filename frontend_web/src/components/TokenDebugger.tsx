import React, { useState } from 'react';
import { Button } from '@/components/ui/button';
import tokenDebugUtils from '@/utils/tokenDebug';
import { useToast } from '@/components/ui/use-toast';

/**
 * A debugging component that can be added to any page to diagnose auth issues
 */
export const TokenDebugger: React.FC = () => {
  const [isOpen, setIsOpen] = useState(false);
  const [results, setResults] = useState<any>(null);
  const [loading, setLoading] = useState(false);
  const { toast } = useToast();

  const testToken = async () => {
    setLoading(true);
    try {
      const result = await tokenDebugUtils.testToken();
      setResults(result);
      toast({
        title: result.valid ? 'Token is valid' : 'Token is invalid',
        description: result.valid 
          ? 'Your authentication token is working correctly'
          : `Token validation failed: ${result.error?.message || 'Unknown error'}`,
        variant: result.valid ? 'default' : 'destructive'
      });
    } catch (err) {
      console.error('Error testing token:', err);
      setResults({ error: err });
      toast({
        title: 'Error',
        description: 'Failed to test token',
        variant: 'destructive'
      });
    } finally {
      setLoading(false);
    }
  };

  const inspectToken = () => {
    const result = tokenDebugUtils.inspectToken();
    setResults(result);
  };

  const resetAuth = () => {
    tokenDebugUtils.resetAuth();
    setResults({ message: 'Auth state reset' });
    toast({
      title: 'Auth Reset',
      description: 'Authentication state has been reset'
    });
  };

  const testCoursesApi = async () => {
    setLoading(true);
    try {
      // Testing the direct API call with explicit headers
      const token = localStorage.getItem('token');
      const authHeaders = {
        'Authorization': `Bearer ${token?.trim()}`,
        'Accept': 'application/json',
        'Content-Type': 'application/json'
      };
      
      // Make a manual request to the API using fetch instead of axios
      toast({
        title: 'Testing API',
        description: 'Making direct fetch request to courses API...'
      });
      
      console.log('Attempting direct fetch with headers:', authHeaders);
      
      const result = await fetch('http://localhost:8080/api/courses', {
        method: 'GET',
        headers: authHeaders,
        credentials: 'include' // Try with credentials
      });
      
      let data;
      try {
        data = await result.json();
      } catch (jsonErr) {
        console.warn('Could not parse JSON response:', jsonErr);
        // Try to get text content if JSON parsing fails
        const text = await result.text();
        data = { rawContent: text || '(empty response)' };
      }
      
      const resultInfo = {
        status: result.status,
        ok: result.ok,
        data,
        headers: Object.fromEntries(result.headers.entries()),
        tokenUsed: token?.substring(0, 15) + '...'
      };
      
      console.log('API test result:', resultInfo);
      setResults(resultInfo);
      
      toast({
        title: result.ok ? 'API call successful' : 'API call failed',
        description: `Status: ${result.status}`,
        variant: result.ok ? 'default' : 'destructive'
      });
    } catch (err: any) {
      console.error('Error calling API:', err);
      setResults({ error: err.message });
      toast({
        title: 'Error',
        description: 'Failed to call API: ' + err.message,
        variant: 'destructive'
      });
    } finally {
      setLoading(false);
    }
  };

  if (!isOpen) {
    return (
      <div className="fixed bottom-4 right-4 z-50">
        <Button 
          variant="outline" 
          className="bg-yellow-100 hover:bg-yellow-200 text-yellow-800 border-yellow-300"
          onClick={() => setIsOpen(true)}
        >
          Debug Auth
        </Button>
      </div>
    );
  }

  return (
    <div className="fixed bottom-4 right-4 z-50 p-4 bg-white rounded-lg shadow-lg border border-gray-200 w-96">
      <div className="flex justify-between mb-2">
        <h3 className="font-bold">Auth Debugger</h3>
        <Button variant="ghost" size="sm" onClick={() => setIsOpen(false)}>✕</Button>
      </div>
      
      <div className="space-y-2 mb-4">
        <Button 
          variant="outline" 
          className="w-full"
          onClick={testToken} 
          disabled={loading}
        >
          Test Token
        </Button>
        
        <Button 
          variant="outline" 
          className="w-full"
          onClick={inspectToken} 
          disabled={loading}
        >
          Inspect Token
        </Button>
        
        <Button 
          variant="outline" 
          className="w-full"
          onClick={testCoursesApi} 
          disabled={loading}
        >
          Test Courses API
        </Button>
        
        <Button 
          variant="destructive" 
          className="w-full"
          onClick={resetAuth} 
          disabled={loading}
        >
          Reset Auth
        </Button>
      </div>
      
      {results && (
        <div className="bg-gray-100 rounded p-2 max-h-60 overflow-auto">
          <pre className="text-xs">
            {JSON.stringify(results, null, 2)}
          </pre>
        </div>
      )}
      
      {loading && (
        <div className="flex justify-center mt-2">
          <div className="animate-spin rounded-full h-5 w-5 border-t-2 border-b-2 border-primary"></div>
        </div>
      )}
    </div>
  );
};

export default TokenDebugger;
