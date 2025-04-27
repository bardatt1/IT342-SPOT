import React, { ComponentType, forwardRef } from 'react';
import ErrorBoundary from './ErrorBoundary';
import ErrorFallback from './ErrorFallback';

interface WithErrorBoundaryOptions {
  fallbackComponent?: React.ComponentType<any>;
  message?: string;
  showDetails?: boolean;
  onReset?: () => void;
}

/**
 * Higher-order component that wraps a component with an ErrorBoundary
 * @param Component The component to wrap
 * @param options Configuration options for the error boundary
 * @returns A wrapped component with error boundary protection
 */
export function withErrorBoundary<P extends object>(
  Component: ComponentType<P>,
  options: WithErrorBoundaryOptions = {}
) {
  const {
    fallbackComponent: FallbackComponent = ErrorFallback,
    message = 'Something went wrong',
    showDetails = import.meta.env.DEV === true,
    onReset
  } = options;

  // Create a wrapped component that preserves ref forwarding
  // Use forwardRef to maintain ref forwarding capability
  const WrappedComponent = forwardRef<unknown, P>((props, ref) => {
    const resetHandler = () => {
      if (onReset) {
        onReset();
      }
      
      // If we're using the default fallback, pass its resetErrorBoundary prop
      if (FallbackComponent === ErrorFallback) {
        // This would be called by the button in ErrorFallback
      }
    };

    const fallback = (
      <FallbackComponent
        message={message}
        showDetails={showDetails}
        resetErrorBoundary={resetHandler}
      />
    );

    return (
      <ErrorBoundary fallback={fallback}>
        <Component {...(props as any)} ref={ref} />
      </ErrorBoundary>
    );
  });

  // Set display name for better debugging
  const displayName = Component.displayName || Component.name || 'Component';
  WrappedComponent.displayName = `withErrorBoundary(${displayName})`;

  return WrappedComponent;
}

export default withErrorBoundary;
