import React from 'react';
import { AlertTriangle, Trash2, X, ArrowUp } from 'lucide-react';
import { Button } from '../button';
import AppModal from './AppModal';

interface DeleteConfirmationModalProps {
  isOpen: boolean;
  onClose: () => void;
  onConfirm: () => void;
  title: string;
  description?: string;
  itemName?: string;
  itemType: string;
  warningText?: string;
  itemDetails?: React.ReactNode;
  isLoading?: boolean;
  confirmButtonText?: string;
  confirmButtonVariant?: 'destructive' | 'default' | 'outline' | 'secondary' | 'ghost' | 'link';
  iconType?: 'delete' | 'promote' | 'none';
}

/**
 * A standardized delete confirmation modal component for use throughout the application
 */
const DeleteConfirmationModal: React.FC<DeleteConfirmationModalProps> = ({
  isOpen,
  onClose,
  onConfirm,
  title,
  description,
  itemName,
  itemType,
  warningText,
  itemDetails,
  isLoading = false,
  confirmButtonText,
  confirmButtonVariant = 'destructive',
  iconType = 'delete',
}) => {
  return (
    <AppModal
      isOpen={isOpen}
      onClose={onClose}
      title={title}
      description={description || `Are you sure you want to delete this ${itemType}? This action cannot be undone.`}
      size="sm"
    >
      <div className="space-y-4">
        <div className={`rounded-md p-4 border ${iconType === 'delete' ? 'bg-red-50 border-red-200' : 'bg-amber-50 border-amber-200'}`}>
          <div className="flex items-center">
            <AlertTriangle className={`h-5 w-5 mr-3 ${iconType === 'delete' ? 'text-red-600' : 'text-amber-600'}`} />
            <div>
              <h3 className={`text-sm font-medium ${iconType === 'delete' ? 'text-red-800' : 'text-amber-800'}`}>
                {iconType === 'delete' ? 'Warning: Permanent Deletion' : 'Important: System Change'}
              </h3>
              <div className={`mt-2 text-sm ${iconType === 'delete' ? 'text-red-700' : 'text-amber-700'}`}>
                {iconType === 'delete' ? (
                  <p>You are about to delete{itemName ? `: ${itemName}` : ' this ' + itemType}.</p>
                ) : iconType === 'promote' ? (
                  <p>You are about to promote{itemName ? `: ${itemName}` : ' this ' + itemType}.</p>
                ) : (
                  <p>You are about to confirm this action for{itemName ? `: ${itemName}` : ' this ' + itemType}.</p>
                )}
                {warningText && <p className="mt-2 text-xs">{warningText}</p>}
              </div>
            </div>
          </div>
        </div>
        
        {itemDetails && (
          <div className="mt-2 bg-gray-50 p-3 rounded-md text-sm">
            {itemDetails}
          </div>
        )}
        
        <div className="flex justify-end space-x-3 pt-2">
          <Button 
            variant="outline" 
            type="button" 
            onClick={onClose}
            className="border-[#215f47]/20 text-[#215f47] hover:bg-[#215f47]/5 hover:text-[#215f47] flex items-center"
            disabled={isLoading}
          >
            <X className="mr-2 h-4 w-4" />
            Cancel
          </Button>
          <Button 
            variant={confirmButtonVariant}
            onClick={onConfirm}
            className={`flex items-center ${confirmButtonVariant === 'destructive' ? 'bg-red-600 hover:bg-red-700' : ''}`}
            disabled={isLoading}
          >
            {iconType === 'delete' && <Trash2 className="mr-2 h-4 w-4" />}
            {iconType === 'promote' && <ArrowUp className="mr-2 h-4 w-4" />}
            {isLoading ? 
              (iconType === 'delete' ? 'Deleting...' : 'Processing...') : 
              (confirmButtonText || (iconType === 'delete' ? 'Delete Permanently' : 'Confirm'))
            }
          </Button>
        </div>
      </div>
    </AppModal>
  );
};

export default DeleteConfirmationModal;
