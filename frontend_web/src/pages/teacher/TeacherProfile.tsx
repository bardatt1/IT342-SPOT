import { useState, useEffect } from 'react';
import { useAuth } from '../../contexts/AuthContext';
import { teacherApi, type Teacher } from '../../lib/api/teacher';
import DashboardLayout from '../../components/ui/layout/DashboardLayout';
import { Button } from '../../components/ui/button';
import { Input } from '../../components/ui/input';
import { Label } from '../../components/ui/label';
import { Card, CardContent, CardDescription, CardFooter, CardHeader, CardTitle } from '../../components/ui/card';
import { Alert, AlertDescription, AlertTitle } from '../../components/ui/alert';
import { Separator } from '../../components/ui/separator';
import { Dialog, DialogContent, DialogDescription, DialogFooter, DialogHeader, DialogTitle } from '../../components/ui/dialog';
import { AlertTriangle, Save, User, Mail, Check, X, Eye, EyeOff, Shield, UserCog } from 'lucide-react';

const TeacherProfile = () => {
  const { user, refreshUserData, logout } = useAuth();
  const [teacher, setTeacher] = useState<Teacher | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [isSaving, setIsSaving] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState<string | null>(null);
  
  // Form fields
  const [firstName, setFirstName] = useState('');
  const [middleName, setMiddleName] = useState('');
  const [lastName, setLastName] = useState('');
  const [email, setEmail] = useState('');
  const [currentPassword, setCurrentPassword] = useState('');
  const [newPassword, setNewPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [passwordError, setPasswordError] = useState<string | null>(null);
  const [showCurrentPassword, setShowCurrentPassword] = useState(false);
  const [showNewPassword, setShowNewPassword] = useState(false);
  const [showConfirmPassword, setShowConfirmPassword] = useState(false);
  
  // Email change confirmation dialog
  const [showEmailChangeDialog, setShowEmailChangeDialog] = useState(false);
  
  useEffect(() => {
    fetchTeacherDetails();
  }, [user?.id]);
  
  const fetchTeacherDetails = async () => {
    if (!user?.id) return;
    
    try {
      setIsLoading(true);
      // Use getCurrentTeacher instead of getById to avoid the admin-only endpoint
      const teacherData = await teacherApi.getCurrentTeacher();
      setTeacher(teacherData);
      
      // Initialize form fields
      setFirstName(teacherData.firstName || '');
      setMiddleName(teacherData.middleName || '');
      setLastName(teacherData.lastName || '');
      setEmail(teacherData.email || '');
      
      setError(null);
    } catch (error: any) {
      console.error('Error fetching teacher details:', error);
      const errorMessage = error.response?.data?.message || error.message || 'Unknown error';
      const errorStatus = error.response?.status || 'No status';
      console.log(`API Error Status: ${errorStatus}, Message: ${errorMessage}`);
      setError(`Failed to load your profile information (${errorStatus}): ${errorMessage}`);
    } finally {
      setIsLoading(false);
    }
  };
  
  const handleCancel = () => {
    // Reset form fields to original values
    if (teacher) {
      setFirstName(teacher.firstName || '');
      setMiddleName(teacher.middleName || '');
      setLastName(teacher.lastName || '');
      setEmail(teacher.email || '');
      
      // Reset password fields
      setCurrentPassword('');
      setNewPassword('');
      setConfirmPassword('');
      setPasswordError(null);
      
      // Clear any error or success messages
      setError(null);
      setSuccess('Changes cancelled, form reset to original values.');
      
      // Clear success message after 3 seconds
      setTimeout(() => {
        setSuccess(null);
      }, 3000);
    }
  };
  
  // Handle form submission - checks if email is changed and prompts for confirmation
  const handleSubmit = (e: React.MouseEvent<HTMLButtonElement>) => {
    e.preventDefault();
    
    // Reset states
    setError(null);
    setSuccess(null);
    setIsSaving(true);
    
    // Validate passwords
    if (newPassword) {
      // Check if passwords match
      if (newPassword !== confirmPassword) {
        setPasswordError("New passwords don't match");
        setIsSaving(false);
        return;
      }
      
      // Check password length
      if (newPassword.length < 8) {
        setPasswordError("New password must be at least 8 characters long");
        setIsSaving(false);
        return;
      }
      
      // Check current password
      if (!currentPassword) {
        setPasswordError("Please enter your current password");
        setIsSaving(false);
        return;
      }
    }
    
    // Check if email was changed
    if (user && email !== user.email) {
      // Show confirmation dialog instead of proceeding directly
      setShowEmailChangeDialog(true);
      setIsSaving(false);
    } else {
      // No email change, proceed directly
      processProfileUpdate();
    }
  };
  
  // Process the actual profile update after all checks
  const processProfileUpdate = async (forceLogout: boolean = false) => {
    if (!teacher || !user?.id) {
      setError('Teacher data not available. Please refresh the page.');
      return;
    }
    
    try {
      setIsSaving(true);
      setError(null);
      setSuccess(null);
      
      // Validate passwords when attempting to change password
      if (newPassword) {
        // Check if passwords match
        if (newPassword !== confirmPassword) {
          setPasswordError("New passwords don't match");
          setIsSaving(false);
          return;
        }
        
        // Check if new password meets minimum length requirement
        if (newPassword.length < 8) {
          setPasswordError("New password must be at least 8 characters long");
          setIsSaving(false);
          return;
        }
        
        // Check if current password is provided
        if (!currentPassword || currentPassword.trim() === '') {
          setPasswordError("Please enter your current password");
          setIsSaving(false);
          return;
        }
      }
      
      setPasswordError(null);
      
      // No need to include ID as we're using the /me endpoint
      const updatedTeacher: Record<string, any> = {
        firstName,
        middleName: middleName || null,
        lastName,
        email,
      };
      
      // Only include passwords if changing password and they meet requirements
      if (newPassword && newPassword.length >= 8 && currentPassword) {
        updatedTeacher.currentPassword = currentPassword;
        updatedTeacher.password = newPassword;
      }
      // We don't need to include teacherPhysicalId as we're not updating it
      
      const result = await teacherApi.updateCurrentTeacher(updatedTeacher);
      
      // Update the local teacher state with the updated data
      setTeacher(result);
      
      // Update all form fields with the new data
      setFirstName(result.firstName || '');
      setMiddleName(result.middleName || '');
      setLastName(result.lastName || '');
      setEmail(result.email || '');
      
      // Handle email change - always force logout if email was changed or forceLogout is true
      if (email !== user?.email || forceLogout) {
        console.log('Email was changed, initiating logout...');
        
        // Set success message for email change
        setSuccess('Profile updated successfully. You will be redirected to login with your new email in a moment.');
        
        // Log the user out after a brief delay to show the message
        setTimeout(() => {
          // Clear all authentication data and redirect to login page
          console.log('Logging out user after email change...');
          logout();
          
          // Forcibly reload the page to ensure all auth state is cleared
          setTimeout(() => {
            window.location.href = '/login';
          }, 500);
        }, 2000);
        return;
      }
      
      // Clear all password fields after successful update
      setCurrentPassword('');
      setNewPassword('');
      setConfirmPassword('');
      setPasswordError(null);
      
      // Show success message
      setSuccess('Profile updated successfully!');
      
      // Update auth context if email changed
      if (email !== user.email) {
        await refreshUserData();
      }
      
      // Clear success message after 3 seconds
      setTimeout(() => {
        setSuccess(null);
      }, 3000);
    } catch (error: any) {
      console.error('Error updating teacher profile:', error);
      const errorMessage = error.response?.data?.message || error.message || 'Unknown error';
      const errorStatus = error.response?.status || 'No status';
      console.log(`API Error Status: ${errorStatus}, Message: ${errorMessage}`);
      setError(`Failed to update profile (${errorStatus}): ${errorMessage}`);
    } finally {
      setIsSaving(false);
    }
  };
  
  if (isLoading) {
    return (
      <DashboardLayout>
        <div className="flex h-full items-center justify-center p-6">
          <div className="flex flex-col items-center space-y-4 text-center">
            <div className="animate-spin text-[#215f47]">
              <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                <path d="M21 12a9 9 0 1 1-6.219-8.56"/>
              </svg>
            </div>
            <p className="text-lg font-medium text-gray-700">Loading profile...</p>
          </div>
        </div>
      </DashboardLayout>
    );
  }
  
  return (
    <DashboardLayout>
      <div className="space-y-6 p-6">
        <div className="flex items-center">
          <div>
            <h1 className="text-2xl font-bold text-[#215f47] flex items-center gap-2">
              <UserCog className="h-6 w-6" />
              Teacher Profile
            </h1>
            <p className="text-gray-500 mt-1">Update your personal information and change password</p>
          </div>
        </div>
        
        {error && (
          <Alert variant="destructive" className="border-red-500/20 mb-6">
            <AlertTriangle className="h-4 w-4" />
            <AlertTitle>Error</AlertTitle>
            <AlertDescription>{error}</AlertDescription>
          </Alert>
        )}
        
        {success && (
          <Alert className="bg-green-50 border-green-500/20 text-green-800 mb-6">
            <Check className="h-4 w-4 text-green-500" />
            <AlertTitle className="text-green-800">Success</AlertTitle>
            <AlertDescription className="text-green-700">{success}</AlertDescription>
          </Alert>
        )}
        
        <Card className="border-[#215f47]/20 shadow-sm">
          <CardHeader className="pb-4">
            <CardTitle className="text-lg font-medium text-[#215f47] flex items-center gap-2">
              <User className="h-5 w-5" />
              Personal Information
            </CardTitle>
            <CardDescription>
              Manage your account details and contact information
            </CardDescription>
          </CardHeader>
          <CardContent>
            <form onSubmit={(e) => e.preventDefault()}>
              <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                <div className="space-y-2">
                  <Label htmlFor="firstName" className="text-gray-700">
                    First Name
                  </Label>
                  <div className="relative">
                    <User className="h-4 w-4 text-[#215f47]/60 absolute left-3 top-1/2 transform -translate-y-1/2" />
                    <Input
                      id="firstName"
                      value={firstName}
                      onChange={(e: React.ChangeEvent<HTMLInputElement>) => setFirstName(e.target.value)}
                      className="pl-9 border-[#215f47]/20 focus:ring-[#215f47]/20 focus:border-[#215f47]"
                      required
                    />
                  </div>
                </div>
                
                <div className="space-y-2">
                  <Label htmlFor="middleName" className="text-gray-700">
                    Middle Name (optional)
                  </Label>
                  <Input
                    id="middleName"
                    value={middleName}
                    onChange={(e: React.ChangeEvent<HTMLInputElement>) => setMiddleName(e.target.value)}
                    className="border-[#215f47]/20 focus:ring-[#215f47]/20 focus:border-[#215f47]"
                  />
                </div>
                
                <div className="space-y-2">
                  <Label htmlFor="lastName" className="text-gray-700">
                    Last Name
                  </Label>
                  <div className="relative">
                    <User className="h-4 w-4 text-[#215f47]/60 absolute left-3 top-1/2 transform -translate-y-1/2" />
                    <Input
                      id="lastName"
                      value={lastName}
                      onChange={(e: React.ChangeEvent<HTMLInputElement>) => setLastName(e.target.value)}
                      className="pl-9 border-[#215f47]/20 focus:ring-[#215f47]/20 focus:border-[#215f47]"
                      required
                    />
                  </div>
                </div>

                <div className="space-y-2">
                  <Label htmlFor="teacherId" className="text-gray-700">
                    Teacher ID
                  </Label>
                  <div className="relative">
                    <Shield className="h-4 w-4 text-[#215f47]/60 absolute left-3 top-1/2 transform -translate-y-1/2" />
                    <Input
                      id="teacherId"
                      value={teacher?.teacherPhysicalId || ''}
                      className="pl-9 border-[#215f47]/20 bg-[#215f47]/5 text-gray-500"
                      disabled
                    />
                  </div>
                  <p className="text-xs text-gray-500">Teacher ID cannot be changed</p>
                </div>
                
                <div className="space-y-2 md:col-span-2">
                  <Label htmlFor="email" className="text-gray-700">
                    Email Address
                  </Label>
                  <div className="relative">
                    <Mail className="h-4 w-4 text-[#215f47]/60 absolute left-3 top-1/2 transform -translate-y-1/2" />
                    <Input
                      id="email"
                      type="email"
                      value={email}
                      onChange={(e: React.ChangeEvent<HTMLInputElement>) => setEmail(e.target.value)}
                      className="pl-9 border-[#215f47]/20 focus:ring-[#215f47]/20 focus:border-[#215f47]"
                      required
                    />
                  </div>
                </div>
                
                <div className="col-span-2">
                  <Separator className="my-6 bg-[#215f47]/10" />
                  
                  <div className="mb-4">
                    <h3 className="text-lg font-medium text-[#215f47] flex items-center gap-2">
                      <Shield className="h-5 w-5" />
                      Security Settings
                    </h3>
                    <p className="text-sm text-gray-500">Update your password or security preferences</p>
                  </div>
                  
                  {passwordError && (
                    <Alert variant="destructive" className="mb-4 bg-red-50/50 border-red-200 text-red-800">
                      <AlertTriangle className="h-4 w-4" />
                      <AlertTitle>Password Error</AlertTitle>
                      <AlertDescription>{passwordError}</AlertDescription>
                    </Alert>
                  )}
                  
                  <div className="space-y-4">
                    <div className="space-y-2">
                      <Label htmlFor="currentPassword" className="text-gray-700">
                        Current Password
                      </Label>
                      <div className="relative">
                        <Input
                          id="currentPassword"
                          type={showCurrentPassword ? "text" : "password"}
                          value={currentPassword}
                          onChange={(e: React.ChangeEvent<HTMLInputElement>) => setCurrentPassword(e.target.value)}
                          placeholder="Enter current password"
                          className="border-[#215f47]/20 focus:ring-[#215f47]/20 focus:border-[#215f47] pr-10"
                        />
                        <button
                          type="button"
                          className="absolute right-3 top-1/2 transform -translate-y-1/2 text-gray-500 hover:text-[#215f47]"
                          onClick={() => setShowCurrentPassword(!showCurrentPassword)}
                          tabIndex={-1}
                        >
                          {showCurrentPassword ? (
                            <EyeOff className="h-4 w-4" aria-hidden="true" />
                          ) : (
                            <Eye className="h-4 w-4" aria-hidden="true" />
                          )}
                        </button>
                      </div>
                    </div>
                    
                    <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                      <div className="space-y-2">
                        <Label htmlFor="newPassword" className="text-gray-700">
                          New Password
                        </Label>
                        <div className="relative">
                          <Input
                            id="newPassword"
                            type={showNewPassword ? "text" : "password"}
                            value={newPassword}
                            onChange={(e: React.ChangeEvent<HTMLInputElement>) => setNewPassword(e.target.value)}
                            placeholder="Enter new password"
                            className="border-[#215f47]/20 focus:ring-[#215f47]/20 focus:border-[#215f47] pr-10"
                          />
                          <button
                            type="button"
                            className="absolute right-3 top-1/2 transform -translate-y-1/2 text-gray-500 hover:text-[#215f47]"
                            onClick={() => setShowNewPassword(!showNewPassword)}
                            tabIndex={-1}
                          >
                            {showNewPassword ? (
                              <EyeOff className="h-4 w-4" aria-hidden="true" />
                            ) : (
                              <Eye className="h-4 w-4" aria-hidden="true" />
                            )}
                          </button>
                        </div>
                      </div>
                      
                      <div className="space-y-2">
                        <Label htmlFor="confirmPassword" className="text-gray-700">
                          Confirm New Password
                        </Label>
                        <div className="relative">
                          <Input
                            id="confirmPassword"
                            type={showConfirmPassword ? "text" : "password"}
                            value={confirmPassword}
                            onChange={(e: React.ChangeEvent<HTMLInputElement>) => setConfirmPassword(e.target.value)}
                            placeholder="Re-enter new password"
                            className="border-[#215f47]/20 focus:ring-[#215f47]/20 focus:border-[#215f47] pr-10"
                          />
                          <button
                            type="button"
                            className="absolute right-3 top-1/2 transform -translate-y-1/2 text-gray-500 hover:text-[#215f47]"
                            onClick={() => setShowConfirmPassword(!showConfirmPassword)}
                            tabIndex={-1}
                          >
                            {showConfirmPassword ? (
                              <EyeOff className="h-4 w-4" aria-hidden="true" />
                            ) : (
                              <Eye className="h-4 w-4" aria-hidden="true" />
                            )}
                          </button>
                        </div>
                      </div>
                    </div>
                    
                    <p className="text-xs text-gray-500 mt-1 bg-[#215f47]/5 p-2 rounded-md border border-[#215f47]/10">
                      <span className="font-medium">Note:</span> Passwords are stored securely using one-way encryption and cannot be displayed.
                      Leave these fields blank if you don't want to change your password.
                    </p>
                  </div>              
                </div>
              </div>
            </form>
          </CardContent>
          <CardFooter className="flex flex-col sm:flex-row justify-end gap-3 pt-2 border-t border-[#215f47]/10">
            <Button
              type="button"
              variant="outline"
              onClick={handleCancel}
              disabled={isSaving}
              className="w-full sm:w-auto border-[#215f47]/20 text-[#215f47] hover:bg-[#215f47]/5 order-2 sm:order-1"
            >
              <X className="mr-2 h-4 w-4" />
              Cancel Changes
            </Button>
            
            <Button 
              type="button"
              onClick={handleSubmit}
              disabled={isSaving}
              className="w-full sm:w-auto bg-[#215f47] hover:bg-[#215f47]/90 text-white order-1 sm:order-2"
            >
              {isSaving ? (
                <>
                  <div className="animate-spin mr-2 h-4 w-4 border-t-2 border-b-2 border-white rounded-full"></div>
                  Saving...
                </>
              ) : (
                <>
                  <Save className="mr-2 h-4 w-4" />
                  Save Changes
                </>
              )}
            </Button>
          </CardFooter>
        </Card>
      </div>
      
      {/* Email Change Confirmation Dialog */}
      <Dialog open={showEmailChangeDialog} onOpenChange={(open) => !open && setShowEmailChangeDialog(false)}>
        <DialogContent className="bg-white">
          <DialogHeader>
            <DialogTitle className="text-[#215f47] flex items-center gap-2">
              <AlertTriangle className="h-5 w-5 text-amber-500" />
              Email Change Confirmation
            </DialogTitle>
            <DialogDescription>
              Changing your email will log you out. You'll need to log in again with your new email.
            </DialogDescription>
          </DialogHeader>
          
          <div className="py-4">
            <p className="text-sm text-gray-500">Current email: <span className="font-medium text-gray-900">{user?.email}</span></p>
            <p className="text-sm text-gray-500 mt-1">New email: <span className="font-medium text-gray-900">{email}</span></p>
            
            <Alert className="mt-4 border-amber-200 bg-amber-50">
              <AlertTriangle className="h-4 w-4 text-amber-500" />
              <AlertDescription className="text-amber-800 text-sm">
                You will be logged out immediately after confirming this change.
              </AlertDescription>
            </Alert>
          </div>
          
          <DialogFooter>
            <Button
              variant="outline"
              onClick={() => {
                setShowEmailChangeDialog(false);
                setIsSaving(false);
              }}
              className="border-[#215f47]/20 text-[#215f47] hover:bg-[#215f47]/5"
            >
              Cancel
            </Button>
            <Button
              onClick={() => {
                setShowEmailChangeDialog(false);
                // Process profile update and then force logout
                processProfileUpdate(true);
              }}
              className="bg-[#215f47] hover:bg-[#215f47]/90 text-white"
            >
              Confirm Change
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </DashboardLayout>
  );
};

export default TeacherProfile;
