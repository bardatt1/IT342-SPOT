import React, { useState } from 'react';
import TokenDebugger from '@/components/TokenDebugger';
import ServerDebugger from '@/components/ServerDebugger';

/**
 * Debug Console Page - A centralized dashboard for all debugging tools
 * This page is meant for development and troubleshooting purposes only
 */
const DebugConsole: React.FC = () => {
  const [activeTab, setActiveTab] = useState<'server' | 'token'>('server');

  return (
    <div className="container mx-auto py-6">
      <div className="bg-white shadow-md rounded-lg p-6 mb-6">
        <div>
          <h1 className="text-2xl font-bold">SPOT System Debug Console</h1>
          <p className="text-gray-600">
            Tools for diagnosing authentication and API issues
          </p>
        </div>
      </div>

      <div className="w-full">
        <div className="w-full max-w-md mx-auto mb-6 flex border rounded overflow-hidden">
          <button
            className={`flex-1 py-2 px-4 ${activeTab === 'server' ? 'bg-blue-500 text-white' : 'bg-gray-100'}`}
            onClick={() => setActiveTab('server')}
          >
            Server Tests
          </button>
          <button
            className={`flex-1 py-2 px-4 ${activeTab === 'token' ? 'bg-blue-500 text-white' : 'bg-gray-100'}`}
            onClick={() => setActiveTab('token')}
          >
            Token Debug
          </button>
        </div>
        
        {activeTab === 'server' && <ServerDebugger />}
        {activeTab === 'token' && <TokenDebugger />}
      </div>
    </div>
  );
};

export default DebugConsole;
