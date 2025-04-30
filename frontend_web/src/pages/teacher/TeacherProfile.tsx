import { useState, useEffect } from 'react';
import { useAuth } from '../../contexts/AuthContext';
import { teacherApi, type Teacher } from '../../lib/api/teacher';
import DashboardLayout from '../../components/ui/layout/DashboardLayout';
import { Button, Input, Label } from '../../components/ui';
import { AlertTriangle, Save, User, Mail, Check, X } from 'lucide-react';

const TeacherProfile = () => {
  const { user, refreshUserData } = useAuth();
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
  
  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    
    if (!teacher || !user?.id) {
      setError('Teacher data not available. Please refresh the page.');
      return;
    }
    
    try {
      setIsSaving(true);
      setError(null);
      setSuccess(null);
      
      // Check if passwords match when attempting to change password
      if (newPassword && newPassword !== confirmPassword) {
        setPasswordError("New passwords don't match");
        return;
      }
      
      // Check if current password is provided when changing password
      if (newPassword && !currentPassword) {
        setPasswordError("Please enter your current password");
        return;
      }
      
      setPasswordError(null);
      
      // No need to include ID as we're using the /me endpoint
      const updatedTeacher = {
        firstName,
        middleName: middleName || null,
        lastName,
        email,
        // Only include passwords if changing password
        ...(newPassword ? { 
          currentPassword,
          password: newPassword 
        } : {})
        // We don't need to include teacherPhysicalId as we're not updating it
      };
      
      const result = await teacherApi.updateCurrentTeacher(updatedTeacher);
      
      setTeacher(result);
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
        <div className="flex h-full items-center justify-center">
          <div className="animate-spin rounded-full h-12 w-12 border-t-2 border-b-2 border-blue-500"></div>
        </div>
      </DashboardLayout>
    );
  }
  
  return (
    <DashboardLayout>
      <div className="container mx-auto px-4 py-8">
        <h1 className="text-2xl font-bold mb-6">My Profile</h1>
        
        {error && (
          <div className="rounded-md bg-red-50 p-4 mb-6">
            <div className="flex">
              <AlertTriangle className="h-5 w-5 text-red-400" />
              <div className="ml-3">
                <h3 className="text-sm font-medium text-red-800">Error</h3>
                <div className="mt-2 text-sm text-red-700">
                  <p>{error}</p>
                </div>
              </div>
            </div>
          </div>
        )}
        
        {success && (
          <div className="rounded-md bg-green-50 p-4 mb-6">
            <div className="flex">
              <Check className="h-5 w-5 text-green-400" />
              <div className="ml-3">
                <h3 className="text-sm font-medium text-green-800">Success</h3>
                <div className="mt-2 text-sm text-green-700">
                  <p>{success}</p>
                </div>
              </div>
            </div>
          </div>
        )}
        
        <div className="bg-white rounded-lg shadow-md p-6">
          <form onSubmit={handleSubmit}>
            <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
              <div>
                <Label htmlFor="firstName" className="block text-sm font-medium text-gray-700 mb-1">
                  First Name
                </Label>
                <div className="relative">
                  <User className="h-5 w-5 text-gray-400 absolute left-3 top-1/2 transform -translate-y-1/2" />
                  <Input
                    id="firstName"
                    value={firstName}
                    onChange={(e: React.ChangeEvent<HTMLInputElement>) => setFirstName(e.target.value)}
                    className="pl-10 w-full"
                    required
                  />
                </div>
              </div>
              
              <div>
                <Label htmlFor="middleName" className="block text-sm font-medium text-gray-700 mb-1">
                  Middle Name (optional)
                </Label>
                <Input
                  id="middleName"
                  value={middleName}
                  onChange={(e: React.ChangeEvent<HTMLInputElement>) => setMiddleName(e.target.value)}
                  className="w-full"
                />
              </div>
              
              <div>
                <Label htmlFor="lastName" className="block text-sm font-medium text-gray-700 mb-1">
                  Last Name
                </Label>
                <div className="relative">
                  <User className="h-5 w-5 text-gray-400 absolute left-3 top-1/2 transform -translate-y-1/2" />
                  <Input
                    id="lastName"
                    value={lastName}
                    onChange={(e: React.ChangeEvent<HTMLInputElement>) => setLastName(e.target.value)}
                    className="pl-10 w-full"
                    required
                  />
                </div>
              </div>

              <div>
                <Label htmlFor="teacherId" className="block text-sm font-medium text-gray-700 mb-1">
                  Teacher ID
                </Label>
                <Input
                  id="teacherId"
                  value={teacher?.teacherPhysicalId || ''}
                  className="w-full bg-gray-100"
                  disabled
                />
                <p className="mt-1 text-xs text-gray-500">Teacher ID cannot be changed</p>
              </div>
              
              <div>
                <Label htmlFor="email" className="block text-sm font-medium text-gray-700 mb-1">
                  Email
                </Label>
                <div className="relative">
                  <Mail className="h-5 w-5 text-gray-400 absolute left-3 top-1/2 transform -translate-y-1/2" />
                  <Input
                    id="email"
                    type="email"
                    value={email}
                    onChange={(e: React.ChangeEvent<HTMLInputElement>) => setEmail(e.target.value)}
                    className="pl-10 w-full"
                    required
                  />
                </div>
              </div>
              
              <div className="col-span-2 border-t pt-4 mt-2">
                <h3 className="text-lg font-medium mb-3">Change Password</h3>
                {passwordError && (
                  <div className="mb-3 p-2 text-sm border border-red-300 bg-red-50 text-red-700 rounded">
                    {passwordError}
                  </div>
                )}
                <div className="space-y-3">
                  <div>
                    <Label htmlFor="currentPassword" className="block text-sm font-medium text-gray-700 mb-1">
                      Current Password
                    </Label>
                    <Input
                      id="currentPassword"
                      type="password"
                      value={currentPassword}
                      onChange={(e: React.ChangeEvent<HTMLInputElement>) => setCurrentPassword(e.target.value)}
                      placeholder="Enter current password"
                      className="w-full"
                    />
                  </div>
                  
                  <div>
                    <Label htmlFor="newPassword" className="block text-sm font-medium text-gray-700 mb-1">
                      New Password
                    </Label>
                    <Input
                      id="newPassword"
                      type="password"
                      value={newPassword}
                      onChange={(e: React.ChangeEvent<HTMLInputElement>) => setNewPassword(e.target.value)}
                      placeholder="Enter new password"
                      className="w-full"
                    />
                  </div>
                  
                  <div>
                    <Label htmlFor="confirmPassword" className="block text-sm font-medium text-gray-700 mb-1">
                      Confirm New Password
                    </Label>
                    <Input
                      id="confirmPassword"
                      type="password"
                      value={confirmPassword}
                      onChange={(e: React.ChangeEvent<HTMLInputElement>) => setConfirmPassword(e.target.value)}
                      placeholder="Re-enter new password"
                      className="w-full"
                    />
                  </div>
                  
                  <p className="text-xs text-gray-500 mt-1">
                    Passwords are stored securely using one-way encryption and cannot be displayed.
                    Leave these fields blank if you don't want to change your password.
                  </p>
                </div>              
              </div>
            </div>
            
            <div className="mt-6 flex flex-col md:flex-row gap-3">
              <Button
                type="submit"
                className="w-full md:w-auto"
                disabled={isSaving}
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
              
              <Button
                type="button"
                variant="outline"
                className="w-full md:w-auto"
                onClick={handleCancel}
                disabled={isSaving}
              >
                <X className="mr-2 h-4 w-4" />
                Cancel Changes
              </Button>
            </div>
          </form>
        </div>
      </div>
    </DashboardLayout>
  );
};

export default TeacherProfile;
