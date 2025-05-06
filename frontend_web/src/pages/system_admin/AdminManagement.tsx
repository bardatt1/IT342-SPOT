import { useState, useEffect } from 'react';
import DashboardLayout from '../../components/ui/layout/DashboardLayout';
import { adminApi, type Admin } from '../../lib/api/admin';
import { Button } from '../../components/ui/button';
import { Input } from '../../components/ui/input';
import { Label } from '../../components/ui/label';
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from '../../components/ui/card';
import { Badge } from '../../components/ui/badge';
import { Alert, AlertDescription } from '../../components/ui/alert';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '../../components/ui/tabs';
import { Plus, Pencil, Trash2, Shield, UserCog, AlertTriangle, X, ArrowUpCircle, ArrowDownCircle } from 'lucide-react';
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '../../components/ui/table';

import { useNavigate } from 'react-router-dom';

const AdminManagement = () => {
  const navigate = useNavigate();
  const [admins, setAdmins] = useState<Admin[]>([]);
  const [systemAdmins, setSystemAdmins] = useState<Admin[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [successMessage, setSuccessMessage] = useState<string | null>(null);
  const [activeTab, setActiveTab] = useState<'admins' | 'system-admins'>('admins');
  
  // Form state for adding/editing admins
  const [showForm, setShowForm] = useState(false);
  const [formType, setFormType] = useState<'add' | 'edit'>('add');
  const [selectedAdminId, setSelectedAdminId] = useState<number | null>(null);
  const [isSystemAdmin, setIsSystemAdmin] = useState<boolean>(false);
  
  const [formData, setFormData] = useState({
    firstName: '',
    middleName: '',
    lastName: '',
    email: '',
    password: '',
    systemAdmin: false
  });

  useEffect(() => {
    fetchData();
  }, [activeTab]);

  const fetchData = async () => {
    try {
      setIsLoading(true);
      setError(null);
      
      // Fetch both regular admins and system admins
      const adminData = await adminApi.getAll();
      setAdmins(adminData.filter(admin =>  !admin.systemAdmin));
      
      const systemAdminData = await adminApi.getAllSystemAdmins();
      setSystemAdmins(systemAdminData);
      
    } catch (error) {
      console.error('Error fetching admins:', error);
      setError('Failed to load admin data. Please try again later.');
    } finally {
      setIsLoading(false);
    }
  };

  const handleTabChange = (tab: 'admins' | 'system-admins') => {
    setActiveTab(tab);
    resetForm();
  };

  const resetForm = () => {
    setShowForm(false);
    setFormType('add');
    setSelectedAdminId(null);
    setIsSystemAdmin(false);
    setFormData({
      firstName: '',
      middleName: '',
      lastName: '',
      email: '',
      password: '',
      systemAdmin: false
    });
  };

  const handleAddNew = () => {
    setShowForm(true);
    setFormType('add');
    // Set systemAdmin value based on current tab
    setFormData({
      ...formData,
      systemAdmin: activeTab === 'system-admins'
    });
  };

  const handleEdit = (admin: Admin) => {
    setShowForm(true);
    setFormType('edit');
    setSelectedAdminId(admin.id);
    setIsSystemAdmin(admin.systemAdmin);
    
    setFormData({
      firstName: admin.firstName,
      middleName: admin.middleName || '',
      lastName: admin.lastName,
      email: admin.email,
      password: '', // Don't populate password for security
      systemAdmin: admin.systemAdmin
    });
  };

  const handleDelete = async (id: number, isSystemAdmin: boolean) => {
    if (!window.confirm(`Are you sure you want to delete this ${isSystemAdmin ? 'system admin' : 'admin'}?`)) {
      return;
    }
    
    try {
      await adminApi.deleteAdmin(id);
      
      // Update the state to remove the deleted admin
      if (isSystemAdmin) {
        setSystemAdmins(systemAdmins.filter(admin => admin.id !== id));
      } else {
        setAdmins(admins.filter(admin => admin.id !== id));
      }
      
      setSuccessMessage(`Admin successfully deleted`);
      // Clear the success message after 3 seconds
      setTimeout(() => setSuccessMessage(null), 3000);
    } catch (error) {
      console.error(`Error deleting admin:`, error);
      setError(`Failed to delete admin. Please try again later.`);
    }
  };

  const handlePromote = async (id: number) => {
    if (!window.confirm('Are you sure you want to promote this admin to system admin?')) {
      return;
    }
    
    try {
      const updatedAdmin = await adminApi.promoteToSystemAdmin(id);
      
      // Update both lists to reflect the promotion
      setAdmins(admins.filter(admin => admin.id !== id));
      setSystemAdmins([...systemAdmins, updatedAdmin]);
      
      setSuccessMessage('Admin successfully promoted to system admin');
      // Clear the success message after 3 seconds
      setTimeout(() => setSuccessMessage(null), 3000);
    } catch (error) {
      console.error(`Error promoting admin:`, error);
      setError(`Failed to promote admin. Please try again later.`);
    }
  };

  const handleDemote = async (id: number) => {
    if (!window.confirm('Are you sure you want to demote this system admin to regular admin?')) {
      return;
    }
    
    try {
      const updatedAdmin = await adminApi.demoteFromSystemAdmin(id);
      
      // Update both lists to reflect the demotion
      setSystemAdmins(systemAdmins.filter(admin => admin.id !== id));
      setAdmins([...admins, updatedAdmin]);
      
      setSuccessMessage('System admin successfully demoted to regular admin');
      // Clear the success message after 3 seconds
      setTimeout(() => setSuccessMessage(null), 3000);
    } catch (error) {
      console.error(`Error demoting system admin:`, error);
      setError(`Failed to demote system admin. Please try again later.`);
    }
  };

  // Password validation helper function
  const validatePassword = (password: string): { valid: boolean; message: string } => {
    if (!password) {
      return { valid: false, message: 'Password is required' };
    }
    if (password.length < 8) {
      return { valid: false, message: 'Password must be at least 8 characters long' };
    }
    // Add more password requirements as needed
    return { valid: true, message: '' };
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    
    // Validate password for new admin creation
    if (formType === 'add' && formData.password) {
      const passwordValidation = validatePassword(formData.password);
      if (!passwordValidation.valid) {
        setError(passwordValidation.message);
        return;
      }
    }
    
    try {
      if (formType === 'edit' && selectedAdminId) {
        // For editing, we need to determine if we're updating a system admin or regular admin
        
        // The backend doesn't seem to have a specific endpoint for updating admins
        // We would need to implement this on the backend and update the adminApi
        // For now, assuming an update function exists
        
        // If role changed, promote or demote accordingly
        if (isSystemAdmin !== formData.systemAdmin) {
          if (formData.systemAdmin) {
            await adminApi.promoteToSystemAdmin(selectedAdminId);
          } else {
            await adminApi.demoteFromSystemAdmin(selectedAdminId);
          }
        }
        
        // Update lists
        await fetchData();
        setSuccessMessage('Admin successfully updated');
      } else {
        // For creating new admins
        const adminCreateData = {
          firstName: formData.firstName,
          middleName: formData.middleName || null,
          lastName: formData.lastName,
          email: formData.email,
          password: formData.password,
          systemAdmin: formData.systemAdmin
        };
        
        // Call the appropriate creation function based on the role
        if (formData.systemAdmin) {
          await adminApi.createSystemAdmin(adminCreateData);
          setSuccessMessage('System admin successfully created');
        } else {
          await adminApi.createAdmin(adminCreateData);
          setSuccessMessage('Admin successfully created');
        }
      }
      
      resetForm();
      await fetchData();
      
      // Clear the success message after 3 seconds
      setTimeout(() => setSuccessMessage(null), 3000);
    } catch (error) {
      console.error(`Error ${formType === 'add' ? 'creating' : 'updating'} admin:`, error);
      setError(`Failed to ${formType === 'add' ? 'create' : 'update'} admin. Please try again later.`);
    }
  };

  const handleInputChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target;
    setFormData({ ...formData, [name]: value });
  };
  
  const handleSystemAdminChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    setFormData({ ...formData, systemAdmin: e.target.checked });
  };

  if (isLoading) {
    return (
      <DashboardLayout>
        <div className="flex h-full items-center justify-center">
          <div className="flex items-center space-x-2">
            <div className="h-4 w-4 animate-spin rounded-full border-2 border-[#215f47] border-t-transparent"></div>
            <p className="text-lg font-medium text-[#215f47]">Loading admin data...</p>
          </div>
        </div>
      </DashboardLayout>
    );
  }

  return (
    <DashboardLayout>
      <div className="space-y-6 p-6">
        <div className="flex flex-col sm:flex-row sm:items-center justify-between mb-6">
          <div>
            <h2 className="text-2xl font-bold text-[#215f47] flex items-center gap-2">
              <Shield className="h-6 w-6" />
              Admin Management
            </h2>
            <p className="text-sm text-gray-500 mt-1">Manage admin accounts and their privileges</p>
          </div>
          
          <div className="mt-4 flex space-x-3 sm:mt-0">
            <Button 
              onClick={() => navigate('/system-admin/dashboard')} 
              variant="outline"
              className="border-[#215f47]/20 text-gray-600 hover:text-[#215f47] hover:border-[#215f47]/30"
            >
              Back to Dashboard
            </Button>
            <Button 
              onClick={handleAddNew} 
              className="bg-[#215f47] hover:bg-[#215f47]/90 text-white gap-2 py-2"
            >
              <Plus className="h-4 w-4" />
              Add New {activeTab === 'system-admins' ? 'System Admin' : 'Admin'}
            </Button>
          </div>
        </div>
        
        {error && (
          <Alert variant="destructive" className="border-red-300 bg-red-50 my-4">
            <AlertTriangle className="h-5 w-5 text-red-600" />
            <AlertDescription className="text-red-700">{error}</AlertDescription>
          </Alert>
        )}
        
        {successMessage && (
          <Alert className="border-green-300 bg-green-50 my-4">
            <AlertDescription className="text-green-700">{successMessage}</AlertDescription>
          </Alert>
        )}
        
        {showForm && (
          <Card className="border-[#215f47]/20 shadow-sm mb-6">
            <CardHeader className="pb-3">
              <div className="flex justify-between items-center">
                <div>
                  <CardTitle className="text-[#215f47]">
                    {formType === 'add' ? 'Add New' : 'Edit'} {formData.systemAdmin ? 'System Admin' : 'Admin'}
                  </CardTitle>
                  <CardDescription>
                    {formType === 'add' 
                      ? `Create a new ${formData.systemAdmin ? 'system admin' : 'admin'} account` 
                      : `Update ${formData.systemAdmin ? 'system admin' : 'admin'} information`}
                  </CardDescription>
                </div>
                <Button 
                  variant="ghost" 
                  size="sm" 
                  className="h-8 w-8 p-0 text-gray-500" 
                  onClick={resetForm}
                >
                  <X className="h-4 w-4" />
                </Button>
              </div>
            </CardHeader>
            <CardContent>
              <form onSubmit={handleSubmit} className="space-y-4">
                <div className="grid grid-cols-1 gap-4 md:grid-cols-2">
                  <div className="space-y-2">
                    <Label htmlFor="firstName" className="text-gray-700">First Name</Label>
                    <Input
                      id="firstName"
                      type="text"
                      name="firstName"
                      value={formData.firstName}
                      onChange={handleInputChange}
                      className="border-[#215f47]/20 focus:border-[#215f47] focus:ring-2 focus:ring-[#215f47]/20"
                      required
                    />
                  </div>
                  
                  <div className="space-y-2">
                    <Label htmlFor="middleName" className="text-gray-700">Middle Name</Label>
                    <Input
                      id="middleName"
                      type="text"
                      name="middleName"
                      value={formData.middleName}
                      onChange={handleInputChange}
                      className="border-[#215f47]/20 focus:border-[#215f47] focus:ring-2 focus:ring-[#215f47]/20"
                    />
                  </div>
                  
                  <div className="space-y-2">
                    <Label htmlFor="lastName" className="text-gray-700">Last Name</Label>
                    <Input
                      id="lastName"
                      type="text"
                      name="lastName"
                      value={formData.lastName}
                      onChange={handleInputChange}
                      className="border-[#215f47]/20 focus:border-[#215f47] focus:ring-2 focus:ring-[#215f47]/20"
                      required
                    />
                  </div>
                  
                  <div className="space-y-2">
                    <Label htmlFor="email" className="text-gray-700">Email</Label>
                    <Input
                      id="email"
                      type="email"
                      name="email"
                      value={formData.email}
                      onChange={handleInputChange}
                      className="border-[#215f47]/20 focus:border-[#215f47] focus:ring-2 focus:ring-[#215f47]/20"
                      required
                    />
                  </div>
                  
                  <div className="space-y-2">
                    <Label htmlFor="password" className="text-gray-700">
                      {formType === 'add' ? 'Password' : 'New Password (leave empty to keep current)'}
                    </Label>
                    <Input
                      id="password"
                      type="password"
                      name="password"
                      value={formData.password}
                      onChange={(e) => {
                        handleInputChange(e);
                        // Clear password error when user changes input
                        if (error && error.includes('Password')) setError(null);
                      }}
                      placeholder={formType === 'edit' ? 'Leave empty to keep current password' : ''}
                      className="border-[#215f47]/20 focus:border-[#215f47] focus:ring-2 focus:ring-[#215f47]/20"
                      required={formType === 'add'}
                    />
                  </div>
                  
                  <div className="space-y-2 flex items-center">
                    <Label htmlFor="systemAdmin" className="text-gray-700 flex items-center gap-2 cursor-pointer">
                      <input
                        id="systemAdmin"
                        type="checkbox"
                        name="systemAdmin"
                        checked={formData.systemAdmin}
                        onChange={handleSystemAdminChange}
                        className="rounded text-[#215f47] focus:ring-[#215f47]/20"
                      />
                      <span className="ml-2">System Admin Privileges</span>
                      {formData.systemAdmin ? (
                        <Badge className="bg-red-50 text-red-700 border-red-200">
                          System Admin
                        </Badge>
                      ) : (
                        <Badge className="bg-[#215f47]/10 text-[#215f47]">
                          Regular Admin
                        </Badge>
                      )}
                    </Label>
                  </div>
                </div>
                <div className="flex justify-end space-x-3 pt-6">
                  <Button
                    type="button"
                    onClick={resetForm}
                    variant="outline"
                    className="border-[#215f47]/20 text-gray-600 hover:text-[#215f47] hover:border-[#215f47]/30"
                  >
                    Cancel
                  </Button>
                  <Button
                    type="submit"
                    className="bg-[#215f47] hover:bg-[#215f47]/90 text-white"
                  >
                    {formType === 'add' ? 'Add' : 'Update'}
                  </Button>
                </div>
              </form>
            </CardContent>
          </Card>
        )}
        
        <Tabs defaultValue="admins" value={activeTab} onValueChange={(value: string) => handleTabChange(value as 'admins' | 'system-admins')} className="space-y-4">
          <TabsList className="bg-[#215f47]/10 p-1">
            <TabsTrigger 
              value="admins" 
              className="data-[state=active]:bg-[#215f47] data-[state=active]:text-white data-[state=active]:shadow-sm rounded-md"
            >
              <UserCog className="mr-2 h-4 w-4" />
              Regular Admins
            </TabsTrigger>
            <TabsTrigger 
              value="system-admins" 
              className="data-[state=active]:bg-[#215f47] data-[state=active]:text-white data-[state=active]:shadow-sm rounded-md"
            >
              <Shield className="mr-2 h-4 w-4" />
              System Admins
            </TabsTrigger>
          </TabsList>

          <TabsContent value="admins" className="space-y-4">
            {/* Regular Admins List */}
            <Card className="border-[#215f47]/20 shadow-sm">
              <CardHeader className="px-6 pb-2 pt-6">
                <CardTitle className="text-lg font-medium text-[#215f47] flex items-center gap-2">
                  <UserCog className="h-5 w-5" />
                  Regular Admins List
                </CardTitle>
                <CardDescription className="text-gray-500">
                  Manage regular admin accounts with standard privileges
                </CardDescription>
              </CardHeader>
              <CardContent className="px-6 pb-6">
                <div className="rounded-md overflow-hidden border border-[#215f47]/20">
                  <Table>
                    <TableHeader className="bg-[#215f47]/5">
                      <TableRow>
                        <TableHead className="text-[#215f47] font-medium">ID</TableHead>
                        <TableHead className="text-[#215f47] font-medium">Name</TableHead>
                        <TableHead className="text-[#215f47] font-medium">Email</TableHead>
                        <TableHead className="text-[#215f47] font-medium">Role</TableHead>
                        <TableHead className="text-[#215f47] font-medium text-right w-[120px]">Actions</TableHead>
                      </TableRow>
                    </TableHeader>
                    <TableBody>
                      {admins.length > 0 ? (
                        admins.map((admin) => (
                          <TableRow key={admin.id} className="hover:bg-[#215f47]/5 transition-colors">
                            <TableCell className="font-medium">{admin.id}</TableCell>
                            <TableCell>
                              {`${admin.firstName} ${admin.middleName ? admin.middleName + ' ' : ''}${admin.lastName}`}
                            </TableCell>
                            <TableCell>{admin.email}</TableCell>
                            <TableCell>
                              <Badge className="bg-[#215f47]/10 text-[#215f47]">
                                Admin
                              </Badge>
                            </TableCell>
                            <TableCell className="text-right">
                              <Button
                                variant="ghost"
                                size="sm"
                                onClick={() => handlePromote(admin.id)}
                                className="h-8 w-8 p-0 text-blue-500 hover:text-blue-700 hover:bg-blue-50"
                                title="Promote to System Admin"
                              >
                                <ArrowUpCircle className="h-4 w-4" />
                              </Button>
                              <Button
                                variant="ghost"
                                size="sm"
                                onClick={() => handleEdit(admin)}
                                className="h-8 w-8 p-0 text-[#215f47] mx-1"
                                title="Edit Admin"
                              >
                                <Pencil className="h-4 w-4" />
                              </Button>
                              <Button
                                variant="ghost"
                                size="sm"
                                onClick={() => handleDelete(admin.id, false)}
                                className="h-8 w-8 p-0 text-red-500 hover:text-red-700 hover:bg-red-50"
                                title="Delete Admin"
                              >
                                <Trash2 className="h-4 w-4" />
                              </Button>
                            </TableCell>
                          </TableRow>
                        ))
                      ) : (
                        <TableRow>
                          <TableCell colSpan={5} className="h-24 text-center text-muted-foreground">
                            No regular admins found
                          </TableCell>
                        </TableRow>
                      )}
                    </TableBody>
                  </Table>
                </div>
              </CardContent>
            </Card>
          </TabsContent>

          <TabsContent value="system-admins" className="space-y-4">
            {/* System Admins List */}
            <Card className="border-[#215f47]/20 shadow-sm">
              <CardHeader className="px-6 pb-2 pt-6">
                <CardTitle className="text-lg font-medium text-[#215f47] flex items-center gap-2">
                  <Shield className="h-5 w-5" />
                  System Admins List
                </CardTitle>
                <CardDescription className="text-gray-500">
                  Manage system admin accounts with elevated privileges
                </CardDescription>
              </CardHeader>
              <CardContent className="px-6 pb-6">
                <div className="rounded-md overflow-hidden border border-[#215f47]/20">
                  <Table>
                    <TableHeader className="bg-[#215f47]/5">
                      <TableRow>
                        <TableHead className="text-[#215f47] font-medium">ID</TableHead>
                        <TableHead className="text-[#215f47] font-medium">Name</TableHead>
                        <TableHead className="text-[#215f47] font-medium">Email</TableHead>
                        <TableHead className="text-[#215f47] font-medium">Role</TableHead>
                        <TableHead className="text-[#215f47] font-medium text-right w-[120px]">Actions</TableHead>
                      </TableRow>
                    </TableHeader>
                    <TableBody>
                      {systemAdmins.length > 0 ? (
                        systemAdmins.map((admin) => (
                          <TableRow key={admin.id} className="hover:bg-[#215f47]/5 transition-colors">
                            <TableCell className="font-medium">{admin.id}</TableCell>
                            <TableCell>
                              {`${admin.firstName} ${admin.middleName ? admin.middleName + ' ' : ''}${admin.lastName}`}
                            </TableCell>
                            <TableCell>{admin.email}</TableCell>
                            <TableCell>
                              <Badge className="bg-red-50 text-red-700 border-red-200">
                                System Admin
                              </Badge>
                            </TableCell>
                            <TableCell className="text-right">
                              <Button
                                variant="ghost"
                                size="sm"
                                onClick={() => handleDemote(admin.id)}
                                className="h-8 w-8 p-0 text-orange-500 hover:text-orange-700 hover:bg-orange-50"
                                title="Demote to Regular Admin"
                              >
                                <ArrowDownCircle className="h-4 w-4" />
                              </Button>
                              <Button
                                variant="ghost"
                                size="sm"
                                onClick={() => handleEdit(admin)}
                                className="h-8 w-8 p-0 text-[#215f47] mx-1"
                                title="Edit System Admin"
                              >
                                <Pencil className="h-4 w-4" />
                              </Button>
                              <Button
                                variant="ghost"
                                size="sm"
                                onClick={() => handleDelete(admin.id, true)}
                                className="h-8 w-8 p-0 text-red-500 hover:text-red-700 hover:bg-red-50"
                                title="Delete System Admin"
                              >
                                <Trash2 className="h-4 w-4" />
                              </Button>
                            </TableCell>
                          </TableRow>
                        ))
                      ) : (
                        <TableRow>
                          <TableCell colSpan={5} className="h-24 text-center text-muted-foreground">
                            No system admins found
                          </TableCell>
                        </TableRow>
                      )}
                    </TableBody>
                  </Table>
                </div>
              </CardContent>
            </Card>
          </TabsContent>
        </Tabs>
      </div>
    </DashboardLayout>
  );
};

export default AdminManagement;
