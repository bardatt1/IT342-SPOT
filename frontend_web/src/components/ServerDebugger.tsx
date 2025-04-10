import React, { useState } from 'react';
import { testServerEndpoints, checkRoleBasedAccess } from '@/utils/serverDebug';

/**
 * A debugging component for testing server endpoints with different authentication strategies
 */
const ServerDebugger: React.FC = () => {
  const [results, setResults] = useState<any>(null);
  const [roleResults, setRoleResults] = useState<any>(null);
  const [loading, setLoading] = useState(false);
  const [roleLoading, setRoleLoading] = useState(false);
  const [activeTab, setActiveTab] = useState('summary');
  const [tokenInfoOpen, setTokenInfoOpen] = useState(false);

  const runServerTests = async () => {
    setLoading(true);
    try {
      const testResults = await testServerEndpoints();
      console.log('Server diagnostics results:', testResults);
      setResults(testResults);
      alert('Server tests complete!');
    } catch (error: any) {
      console.error('Error running server tests:', error);
      alert(`Failed to run tests: ${error.message}`);
      setResults({ error: error.message });
    } finally {
      setLoading(false);
    }
  };
  
  const runRoleTests = async () => {
    setRoleLoading(true);
    try {
      const accessResults = await checkRoleBasedAccess();
      console.log('Role-based access results:', accessResults);
      setRoleResults(accessResults);
      alert('Role access tests complete!');
    } catch (error: any) {
      console.error('Error running role access tests:', error);
      alert(`Failed to run tests: ${error.message}`);
      setRoleResults({ error: error.message });
    } finally {
      setRoleLoading(false);
    }
  };

  const formatJson = (data: any) => {
    try {
      return typeof data === 'object' 
        ? JSON.stringify(data, null, 2) 
        : data;
    } catch (e) {
      return 'Error formatting data';
    }
  };

  return (
    <div className="w-full max-w-4xl mx-auto bg-white shadow-md rounded-lg p-6">
      <div className="mb-6">
        <h2 className="text-xl font-bold mb-2">Server Authentication Debugger</h2>
        <p className="text-gray-600">
          Test server endpoints with different authentication strategies to diagnose 403 errors
        </p>
      </div>
      
      <div className="mb-6">
        <div className="flex gap-4 mb-4">
          <button 
            onClick={runServerTests} 
            disabled={loading}
            className="flex-1 bg-blue-500 hover:bg-blue-600 text-white py-2 px-4 rounded disabled:bg-blue-300"
          >
            {loading ? 'Running Server Tests...' : 'Run Server Tests'}
          </button>
          
          <button 
            onClick={runRoleTests} 
            disabled={roleLoading}
            className="flex-1 bg-green-500 hover:bg-green-600 text-white py-2 px-4 rounded disabled:bg-green-300"
          >
            {roleLoading ? 'Checking Roles...' : 'Check Role Access'}
          </button>
        </div>
      </div>
      
      {results && (
        <div className="border rounded-lg overflow-hidden">
          <div className="bg-gray-100 p-3 border-b">
            <div className="flex space-x-4">
              <button 
                className="px-3 py-1 rounded hover:bg-gray-200" 
                onClick={() => setActiveTab('summary')}
              >
                Summary
              </button>
              <button 
                className="px-3 py-1 rounded hover:bg-gray-200" 
                onClick={() => setActiveTab('test1')}
              >
                Bearer Token
              </button>
              <button 
                className="px-3 py-1 rounded hover:bg-gray-200" 
                onClick={() => setActiveTab('test2')}
              >
                Raw Token
              </button>
              <button 
                className="px-3 py-1 rounded hover:bg-gray-200" 
                onClick={() => setActiveTab('test3')}
              >
                Public API
              </button>
              <button 
                className="px-3 py-1 rounded hover:bg-gray-200" 
                onClick={() => setActiveTab('test4')}
              >
                CORS Check
              </button>
              <button 
                className="px-3 py-1 rounded hover:bg-gray-200" 
                onClick={() => setActiveTab('roles')}
              >
                Role Access
              </button>
            </div>
          </div>
          
          {activeTab === 'summary' && (
            <div className="p-4 border rounded-md mt-2">
              <h3 className="text-lg font-semibold mb-2">Test Summary</h3>
              
              {results.error ? (
                <div className="p-3 bg-red-50 text-red-500 rounded-md mb-3">
                  {results.error}
                </div>
              ) : (
                <div className="space-y-2">
                  <div className="flex justify-between p-2 bg-gray-50 rounded">
                    <span>Test 1 (Bearer Token):</span>
                    <span style={{ color: results.test1?.ok ? 'green' : 'red' }}>
                      {results.test1?.status} {results.test1?.statusText}
                    </span>
                  </div>
                  <div className="flex justify-between p-2 bg-gray-50 rounded">
                    <span>Test 2 (Raw Token):</span>
                    <span style={{ color: results.test2?.ok ? 'green' : 'red' }}>
                      {results.test2?.status} {results.test2?.statusText}
                    </span>
                  </div>
                  <div className="flex justify-between p-2 bg-gray-50 rounded">
                    <span>Test 3 (Public API):</span>
                    <span style={{ color: results.test3?.ok ? 'green' : 'red' }}>
                      {results.test3?.status} {results.test3?.statusText}
                    </span>
                  </div>
                  <div className="flex justify-between p-2 bg-gray-50 rounded">
                    <span>Test 4 (CORS):</span>
                    <span style={{ color: results.test4?.ok ? 'green' : 'red' }}>
                      {results.test4?.status} {results.test4?.statusText}
                    </span>
                  </div>
                </div>
              )}
              
              {results.tokenInfo && (
                <div className="mt-4 border rounded p-3">
                  <button 
                    className="flex justify-between w-full py-2"
                    onClick={() => setTokenInfoOpen(!tokenInfoOpen)}
                  >
                    <span className="font-medium">Token Information</span>
                    <span>{tokenInfoOpen ? '▲' : '▼'}</span>
                  </button>
                  
                  {tokenInfoOpen && (
                    <div className="pt-2 space-y-1 text-sm">
                      <div className="flex justify-between">
                        <span className="font-medium">User ID:</span>
                        <span>{results.tokenInfo.userId}</span>
                      </div>
                      <div className="flex justify-between">
                        <span className="font-medium">Subject:</span>
                        <span>{results.tokenInfo.subject}</span>
                      </div>
                      <div className="flex justify-between">
                        <span className="font-medium">Role:</span>
                        <span>{results.tokenInfo.role}</span>
                      </div>
                      <div className="flex justify-between">
                        <span className="font-medium">Expires:</span>
                        <span>{results.tokenInfo.expiresAt}</span>
                      </div>
                      <div className="flex justify-between">
                        <span className="font-medium">Expired:</span>
                        <span style={{ color: results.tokenInfo.isExpired === true ? 'red' : 'green' }}>
                          {String(results.tokenInfo.isExpired)}
                        </span>
                      </div>
                    </div>
                  )}
                </div>
              )}
            </div>
          )}
          
          {activeTab === 'test1' && (
            <div className="p-4 border rounded-md mt-2">
              <h3 className="text-lg font-semibold mb-2">Bearer Token Test</h3>
              <pre className="p-4 bg-gray-50 overflow-x-auto text-sm rounded-md">
                {formatJson(results.test1)}
              </pre>
            </div>
          )}
          
          {activeTab === 'test2' && (
            <div className="p-4 border rounded-md mt-2">
              <h3 className="text-lg font-semibold mb-2">Raw Token Test</h3>
              <pre className="p-4 bg-gray-50 overflow-x-auto text-sm rounded-md">
                {formatJson(results.test2)}
              </pre>
            </div>
          )}
          
          {activeTab === 'test3' && (
            <div className="p-4 border rounded-md mt-2">
              <h3 className="text-lg font-semibold mb-2">Public API Test</h3>
              <pre className="p-4 bg-gray-50 overflow-x-auto text-sm rounded-md">
                {formatJson(results.test3)}
              </pre>
            </div>
          )}
          
          {activeTab === 'test4' && (
            <div className="p-4 border rounded-md mt-2">
              <h3 className="text-lg font-semibold mb-2">CORS Check</h3>
              <pre className="p-4 bg-gray-50 overflow-x-auto text-sm rounded-md">
                {formatJson(results.test4)}
              </pre>
            </div>
          )}
          
          {activeTab === 'roles' && (
            <div className="p-4 border rounded-md mt-2">
              <h3 className="text-lg font-semibold mb-2">Role-Based Access Check</h3>
              
              {!roleResults && (
                <div className="p-4 bg-gray-100 text-center rounded-md">
                  <p>Click "Check Role Access" to test role-based permissions</p>
                </div>
              )}
              
              {roleResults?.error && (
                <div className="p-3 bg-red-50 text-red-500 rounded-md mb-3">
                  {roleResults.error}
                </div>
              )}
              
              {roleResults && !roleResults.error && (
                <div>
                  <div className="mb-4 p-3 bg-blue-50 rounded-md">
                    <strong>Your Role:</strong> {roleResults.userRole || 'Unknown'}
                  </div>
                  
                  <div className="space-y-4">
                    {Object.entries(roleResults)
                      .filter(([key]) => key !== 'userRole' && key !== 'error')
                      .map(([endpoint, data]: [string, any]) => (
                        <div key={endpoint} className="border rounded-md overflow-hidden">
                          <div className={`p-3 ${data.ok ? 'bg-green-50' : 'bg-red-50'} border-b flex justify-between items-center`}>
                            <div>
                              <strong>{endpoint}</strong> ({data.url})
                            </div>
                            <div>
                              <span className={`px-2 py-1 rounded text-white ${data.ok ? 'bg-green-500' : 'bg-red-500'}`}>
                                {data.status} {data.statusText}
                              </span>
                            </div>
                          </div>
                          <div className="p-3">
                            <div className="grid grid-cols-2 gap-2 text-sm mb-2">
                              <div><strong>Should Have Access:</strong></div> 
                              <div>{String(data.shouldHaveAccess)}</div>
                              
                              <div><strong>Access Granted:</strong></div> 
                              <div>{String(data.accessGranted)}</div>
                            </div>
                            
                            {data.accessGranted && data.data && data.data !== 'N/A' && (
                              <div className="mt-2">
                                <div className="text-sm font-semibold mb-1">Response Data Sample:</div>
                                <div className="max-h-40 overflow-y-auto">
                                  <pre className="p-2 bg-gray-50 text-xs rounded">
                                    {typeof data.data === 'object' ? 
                                      JSON.stringify(data.data, null, 2).substring(0, 300) + (JSON.stringify(data.data).length > 300 ? '...' : '') : 
                                      String(data.data).substring(0, 300)}
                                  </pre>
                                </div>
                              </div>
                            )}
                          </div>
                        </div>
                      ))}
                  </div>
                </div>
              )}
            </div>
          )}
        </div>
      )}
      
      <div className="mt-4 text-sm text-gray-500">
        This tool helps identify server-side configuration issues causing 403 errors.
      </div>
    </div>
  );
};

export default ServerDebugger;
