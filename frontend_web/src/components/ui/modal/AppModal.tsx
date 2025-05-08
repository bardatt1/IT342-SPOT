import { X } from 'lucide-react';
import * as React from 'react';
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogDescription } from '../dialog';

interface AppModalProps {
  isOpen: boolean;
  onClose: () => void;
  title: string;
  description?: string;
  children: React.ReactNode;
  size?: 'sm' | 'md' | 'lg' | 'xl' | 'full';
}

const AppModal: React.FC<AppModalProps> = ({
  isOpen,
  onClose,
  title,
  description,
  children,
  size = 'md'
}) => {
  // Size classes based on the size prop
  const sizeClasses = {
    sm: 'sm:max-w-md',
    md: 'sm:max-w-lg',
    lg: 'sm:max-w-2xl',
    xl: 'sm:max-w-4xl',
    full: 'sm:max-w-[90vw]'
  };

  return (
    <Dialog open={isOpen} onOpenChange={(open) => !open && onClose()}>
      <DialogContent 
        className={`${sizeClasses[size]} max-h-[90vh] overflow-y-auto p-0 bg-white`}
      >
        <div className="sticky top-0 z-10 bg-white border-b border-gray-100">
          <DialogHeader className="px-6 py-4">
            <div className="flex items-center justify-between">
              <div>
                <DialogTitle className="text-xl font-semibold text-[#215f47]">{title}</DialogTitle>
                {description && (
                  <DialogDescription className="text-gray-500 mt-1">
                    {description}
                  </DialogDescription>
                )}
              </div>
              <button
                onClick={onClose}
                className="rounded-full p-1.5 text-gray-400 hover:text-gray-500 hover:bg-gray-100 focus:outline-none"
              >
                <X className="h-5 w-5" />
              </button>
            </div>
          </DialogHeader>
        </div>
        <div className="p-6">
          {children}
        </div>
      </DialogContent>
    </Dialog>
  );
};

export default AppModal;
