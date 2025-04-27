import { ReactNode } from 'react';
import { AlertTriangle, RefreshCcw } from 'lucide-react';
import { Button } from '../ui/button';

interface ErrorFallbackProps {
  error?: Error | null;
  resetErrorBoundary?: () => void;
  message?: string;
  showDetails?: boolean;
  children?: ReactNode;
}

const ErrorFallback = ({
  error,
  resetErrorBoundary,
  message = 'Something went wrong',
  showDetails = false,
  children
}: ErrorFallbackProps) => {
  return (
    <div className="mx-auto max-w-2xl rounded-md border border-red-200 bg-red-50 p-6 shadow-sm">
      <div className="flex items-start">
        <div className="flex-shrink-0">
          <AlertTriangle className="h-6 w-6 text-red-500" aria-hidden="true" />
        </div>
        <div className="ml-4 flex-1">
          <h3 className="text-base font-medium text-red-800">{message}</h3>
          <div className="mt-2 text-sm text-red-700">
            {children || <p>We encountered an error while processing your request. Please try again.</p>}
          </div>
          
          {showDetails && error && (
            <details className="mt-4 rounded-md bg-red-100 p-3">
              <summary className="cursor-pointer font-medium">Error details</summary>
              <div className="mt-2 overflow-auto">
                <p className="whitespace-pre-wrap font-mono text-xs">{error.message}</p>
                {error.stack && (
                  <p className="mt-2 whitespace-pre-wrap font-mono text-xs">{error.stack}</p>
                )}
              </div>
            </details>
          )}
          
          {resetErrorBoundary && (
            <div className="mt-4">
              <Button 
                onClick={resetErrorBoundary}
                className="flex items-center"
                variant="outline"
              >
                <RefreshCcw className="mr-2 h-4 w-4" />
                Try again
              </Button>
            </div>
          )}
        </div>
      </div>
    </div>
  );
};

export default ErrorFallback;
