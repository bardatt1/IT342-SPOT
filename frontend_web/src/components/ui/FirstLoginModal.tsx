import React from 'react';
import { useNavigate } from 'react-router-dom';
import { AlertTriangle, ArrowRight } from 'lucide-react';
import { Button } from './button';
import { Dialog, DialogContent, DialogDescription, DialogFooter, DialogHeader, DialogTitle } from './dialog';
import { Card, CardContent } from './card';
import { Badge } from './badge';

interface FirstLoginModalProps {
  onClose: () => void;
}

const FirstLoginModal: React.FC<FirstLoginModalProps> = ({ onClose }) => {
  const navigate = useNavigate();

  const handleGoToProfile = () => {
    navigate('/teacher/profile');
    onClose();
  };

  return (
    <Dialog open={true} onOpenChange={() => onClose()}>
      <DialogContent className="sm:max-w-md border-[#215f47]/20">
        <DialogHeader>
          <DialogTitle className="text-[#215f47] flex items-center gap-2">
            <Badge variant="outline" className="bg-[#215f47]/5 text-[#215f47] px-2 py-1 text-xs">
              New User
            </Badge>
            <span>Welcome to SPOT!</span>
          </DialogTitle>
          <DialogDescription className="pt-2">
            We're excited to have you on board. Let's get you set up properly.
          </DialogDescription>
        </DialogHeader>
        
        <div className="py-2">
          <Card className="border-[#215f47]/20 bg-[#215f47]/5 mb-4">
            <CardContent className="p-4">
              <div className="space-y-2">
                <div className="flex items-start gap-2">
                  <AlertTriangle className="h-5 w-5 text-yellow-600 mt-0.5 flex-shrink-0" />
                  <div>
                    <h4 className="font-medium text-[#215f47]">Temporary Password Detected</h4>
                    <p className="text-sm text-gray-600">
                      Your account is currently using a default password. For security reasons, 
                      please update it and complete your profile information.                      
                    </p>
                  </div>
                </div>
              </div>
            </CardContent>
          </Card>
          
          <div className="rounded-md bg-gradient-to-r from-[#215f47]/10 to-[#215f47]/5 p-4 mb-2">
            <p className="text-sm text-gray-700">
              Completing your profile will help students identify you more easily and
              ensure you receive important notifications.
            </p>
          </div>
        </div>
        
        <DialogFooter className="sm:justify-between">
          <Button 
            variant="outline" 
            onClick={onClose}
            className="border-[#215f47]/20 text-gray-600 hover:text-[#215f47] hover:border-[#215f47]/30"
          >
            Remind me later
          </Button>
          <Button 
            onClick={handleGoToProfile}
            className="bg-[#215f47] hover:bg-[#215f47]/90 text-white gap-1"
          >
            Update my profile
            <ArrowRight className="h-4 w-4" />
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );  
};

export default FirstLoginModal;
