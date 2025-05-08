import { useState, useEffect } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import DashboardLayout from '../../components/ui/layout/DashboardLayout';
import { adminApi, type Admin } from '../../lib/api/admin';
import { Button } from '../../components/ui/button';
import { Input } from '../../components/ui/input';
import { Label } from '../../components/ui/label';
import { Card, CardContent, CardHeader, CardTitle, CardDescription, CardFooter } from '../../components/ui/card';
import { Badge } from '../../components/ui/badge';
import { Alert, AlertDescription } from '../../components/ui/alert';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '../../components/ui/tabs';
import { 
  Plus, 
  Pencil, 
  Trash2, 
  ShieldCheck, 
  Shield, 
  AlertTriangle, 
  X, 
  ArrowUp, 
  ArrowDown 
} from 'lucide-react';
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '../../components/ui/table';
import { Checkbox } from '../../components/ui/checkbox';

const AdminManagement = () => {
  const navigate = useNavigate();
  const location = useLocation();
  
  const [activeTab, setActiveTab] = useState<'admins' | 'systemAdmins'>('admins');
  const [admins, setAdmins] = useState<Admin[]>([]);
  const [systemAdmins, setSystemAdmins] = useState<Admin[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  
  // Form state for adding/editing admins
  const [showForm, setShowForm] = useState(false);
  const [formType, setFormType] = useState<'add' | 'edit'>('add');
  const [selectedAdminId, setSelectedAdminId] = useState<number | null>(null);
  
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
      if (activeTab === 'admins') {
        const data = await adminApi.getAll();
        setAdmins(data);
      } else {
        const data = await adminApi.getAllSystemAdmins();
        setSystemAdmins(data);
      }
    } catch (error) {
      console.error(`Error fetching ${activeTab}:`, error);
      setError(`Failed to load ${activeTab}. Please try again later.`);
    } finally {
      setIsLoading(false);
    }
  };

  const handleTabChange = (tab: 'admins' | 'systemAdmins') => {
    setActiveTab(tab);
    resetForm();
  };

  // Check if we're on the correct route and redirect if needed
  useEffect(() => {
    if (location.pathname !== '/system-admin/admins') {
      navigate('/system-admin/admins');
    }
  }, [location.pathname, navigate]);

  const resetForm = () => {
    setShowForm(false);
    setFormType('add');
    setSelectedAdminId(null);
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
    // When adding from the system admins tab, set the systemAdmin flag
    setFormData(prev => ({
      ...prev,
      systemAdmin: activeTab === 'systemAdmins'
    }));
  };

  const handleEdit = (id: number) => {
    setShowForm(true);
    setFormType('edit');
    setSelectedAdminId(id);
    
    // Populate form data based on selected user
    const adminList = activeTab === 'admins' ? admins : systemAdmins;
    const admin = adminList.find(a => a.id === id);
    
    if (admin) {
      setFormData({
        firstName: admin.firstName,
        middleName: admin.middleName || '',
        lastName: admin.lastName,
        email: admin.email,
        password: '', // Don't populate password for security
        systemAdmin: admin.systemAdmin
      });
    }
  };

  const handleDelete = async (id: number) => {
    const adminType = activeTab === 'admins' ? 'admin' : 'system admin';
    if (!window.confirm(`Are you sure you want to delete this ${adminType}?`)) {
      return;
    }
    
    try {
      await adminApi.deleteAdmin(id);
      
      if (activeTab === 'admins') {
        setAdmins(admins.filter(a => a.id !== id));
      } else {
        setSystemAdmins(systemAdmins.filter(a => a.id !== id));
      }
      
      setError(null);
    } catch (error) {
      console.error(`Error deleting ${adminType}:`, error);
      setError(`Failed to delete ${adminType}. Please try again later.`);
    }
  };

  const handlePromote = async (id: number) => {
    if (!window.confirm("Are you sure you want to promote this admin to system admin?")) {
      return;
    }
    
    try {
      const promotedAdmin = await adminApi.promoteToSystemAdmin(id);
      
      // Update both lists
      setAdmins(admins.filter(a => a.id !== id));
      setSystemAdmins([...systemAdmins, promotedAdmin]);
      
      setError(null);
    } catch (error) {
      console.error(`Error promoting admin:`, error);
      setError(`Failed to promote admin. Please try again later.`);
    }
  };

  const handleDemote = async (id: number) => {
    const systemAdminCount = systemAdmins.length;
    
    if (systemAdminCount <= 1) {
      setError("Cannot demote the last system admin. At least one system admin must remain.");
      return;
    }
    
    if (!window.confirm("Are you sure you want to demote this system admin to regular admin?")) {
      return;
    }
    
    try {
      const demotedAdmin = await adminApi.demoteFromSystemAdmin(id);
      
      // Update both lists
      setSystemAdmins(systemAdmins.filter(a => a.id !== id));
      setAdmins([...admins, demotedAdmin]);
      
      setError(null);
    } catch (error) {
      console.error(`Error demoting system admin:`, error);
      setError(`Failed to demote system admin. Please try again later.`);
    }
  };

  // Password validation helper function
  const validatePassword = (password: string): { valid: boolean; message: string } => {
    if (formType === 'add' && !password) {
      return { valid: false, message: 'Password is required for new admin accounts' };
    }
    if (password && password.length < 8) {
      return { valid: false, message: 'Password must be at least 8 characters long' };
    }
    // Add more password requirements as needed
    return { valid: true, message: '' };
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    
    // Validate password for new admin creation
    if (formData.password) {
      const passwordValidation = validatePassword(formData.password);
      if (!passwordValidation.valid) {
        setError(passwordValidation.message);
        return;
      }
    } else if (formType === 'add') {
      setError('Password is required for new admin accounts');
      return;
    }
    
    try {
      if (formType === 'edit' && selectedAdminId) {
        // Update admin - we don't actually have an update endpoint in the API
        // so we'll just log for now
        console.log('Updating admin, would call API with:', {
          id: selectedAdminId,
          ...formData
        });
        
        // Mock response for now
        const updatedAdmin = {
          id: selectedAdminId,
          firstName: formData.firstName,
          middleName: formData.middleName || undefined,
          lastName: formData.lastName,
          email: formData.email,
          systemAdmin: formData.systemAdmin
        };
        
        // Update the right list
        if (formData.systemAdmin) {
          setSystemAdmins(systemAdmins.map(a => a.id === selectedAdminId ? updatedAdmin : a));
          setAdmins(admins.filter(a => a.id !== selectedAdminId));
        } else {
          setAdmins(admins.map(a => a.id === selectedAdminId ? updatedAdmin : a));
          setSystemAdmins(systemAdmins.filter(a => a.id !== selectedAdminId));
        }
      } else {
        // Create new admin
        const adminData = {
          firstName: formData.firstName,
          middleName: formData.middleName || undefined,
          lastName: formData.lastName,
          email: formData.email,
          password: formData.password,
          systemAdmin: formData.systemAdmin
        };
        
        // Call the appropriate API based on systemAdmin flag
        let newAdmin;
        if (formData.systemAdmin) {
          newAdmin = await adminApi.createSystemAdmin(adminData);
          setSystemAdmins([...systemAdmins, newAdmin]);
        } else {
          newAdmin = await adminApi.createAdmin(adminData);
          setAdmins([...admins, newAdmin]);
        }
      }
      
      resetForm();
      await fetchData();
      setError(null);
    } catch (error) {
      console.error(`Error ${formType === 'add' ? 'creating' : 'updating'} admin:`, error);
      setError(`Failed to ${formType === 'add' ? 'create' : 'update'} admin. Please try again later.`);
    }
  };

  const handleInputChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target;
    setFormData({ ...formData, [name]: value });
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
              <ShieldCheck className="h-6 w-6" />
              Admin Management
            </h2>
            <p className="text-sm text-gray-500 mt-1">Manage admin accounts and privileges</p>
          </div>
          
          <div className="mt-4 flex space-x-3 sm:mt-0">
            <Button 
              onClick={handleAddNew} 
              className="bg-[#215f47] hover:bg-[#215f47]/90 text-white gap-2 py-2"
            >
              <Plus className="h-4 w-4" />
              Add New {activeTab === 'admins' ? 'Admin' : 'System Admin'}
            </Button>
          </div>
        </div>
        
        {error && (
          <Alert variant="destructive" className="border-red-300 bg-red-50 my-4">
            <AlertTriangle className="h-5 w-5 text-red-600" />
            <AlertDescription className="text-red-700">{error}</AlertDescription>
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
                      required
                      className="border-[#215f47]/20 focus:border-[#215f47] focus:ring-2 focus:ring-[#215f47]/20"
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
                      required
                      className="border-[#215f47]/20 focus:border-[#215f47] focus:ring-2 focus:ring-[#215f47]/20"
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
                      required
                      className="border-[#215f47]/20 focus:border-[#215f47] focus:ring-2 focus:ring-[#215f47]/20"
                    />
                  </div>
                  
                  <div className="space-y-2">
                    <Label htmlFor="password" className="text-gray-700">
                      Password {formType === 'edit' && <span className="text-gray-500 text-sm">(Leave blank to keep current)</span>}
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
                      required={formType === 'add'}
                      className="border-[#215f47]/20 focus:border-[#215f47] focus:ring-2 focus:ring-[#215f47]/20"
                    />
                  </div>
                  
                  <div className="flex items-center space-x-2 pt-4">
                    <Checkbox 
                      id="systemAdmin" 
                      checked={formData.systemAdmin}
                      onCheckedChange={(checked: boolean | 'indeterminate') => {
                        // Handle all possible states from Radix UI checkbox
                        setFormData({ ...formData, systemAdmin: checked === true });
                      }}
                    />
                    <Label 
                      htmlFor="systemAdmin" 
                      className="text-gray-700 font-medium flex items-center gap-1"
                    >
                      <ShieldCheck className="h-4 w-4 text-[#215f47]" />
                      System Admin Privileges
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
        
        <Tabs defaultValue="admins" value={activeTab} onValueChange={(value: string) => handleTabChange(value as 'admins' | 'systemAdmins')} className="space-y-4">
          <TabsList className="bg-[#215f47]/10 p-1">
            <TabsTrigger 
              value="admins" 
              className="data-[state=active]:bg-[#215f47] data-[state=active]:text-white data-[state=active]:shadow-sm rounded-md"
            >
              <Shield className="mr-2 h-4 w-4" />
              Admins
            </TabsTrigger>
            <TabsTrigger 
              value="systemAdmins" 
              className="data-[state=active]:bg-[#215f47] data-[state=active]:text-white data-[state=active]:shadow-sm rounded-md"
            >
              <ShieldCheck className="mr-2 h-4 w-4" />
              System Admins
            </TabsTrigger>
          </TabsList>

          <TabsContent value="admins" className="space-y-4">
            <Card className="border-[#215f47]/20 shadow-sm">
              <CardHeader className="px-6 pb-2 pt-6">
                <CardTitle className="text-lg font-medium text-[#215f47] flex items-center gap-2">
                  <Shield className="h-5 w-5" />
                  Admin Users
                </CardTitle>
                <CardDescription className="text-gray-500">
                  Administrators with standard privileges
                </CardDescription>
              </CardHeader>
              <CardContent className="px-6 pb-6">
                <div className="rounded-md overflow-hidden border border-[#215f47]/20">
                  <Table>
                    <TableHeader className="bg-[#215f47]/5">
                      <TableRow>
                        <TableHead className="text-[#215f47] font-medium">Name</TableHead>
                        <TableHead className="text-[#215f47] font-medium">Email</TableHead>
                        <TableHead className="text-[#215f47] font-medium text-right w-[120px]">Actions</TableHead>
                      </TableRow>
                    </TableHeader>
                    <TableBody>
                      {admins.length > 0 ? (
                        admins.map((admin) => (
                          <TableRow key={admin.id} className="hover:bg-[#215f47]/5 transition-colors">
                            <TableCell>
                              {`${admin.firstName} ${admin.middleName ? admin.middleName + ' ' : ''}${admin.lastName}`}
                            </TableCell>
                            <TableCell>{admin.email}</TableCell>
                            <TableCell className="text-right">
                              <Button
                                variant="ghost"
                                size="sm"
                                onClick={() => handlePromote(admin.id)}
                                className="h-8 w-8 p-0 text-amber-600 hover:text-amber-700 hover:bg-amber-50"
                                title="Promote to System Admin"
                              >
                                <ArrowUp className="h-4 w-4" />
                              </Button>
                              <Button
                                variant="ghost"
                                size="sm"
                                onClick={() => handleEdit(admin.id)}
                                className="h-8 w-8 p-0 text-[#215f47] mx-1"
                                title="Edit Admin"
                              >
                                <Pencil className="h-4 w-4" />
                              </Button>
                              <Button
                                variant="ghost"
                                size="sm"
                                onClick={() => handleDelete(admin.id)}
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
                          <TableCell colSpan={3} className="h-24 text-center text-muted-foreground">
                            No admin users found. Add a new admin to get started.
                          </TableCell>
                        </TableRow>
                      )}
                    </TableBody>
                  </Table>
                </div>
              </CardContent>
            </Card>
          </TabsContent>

          <TabsContent value="systemAdmins" className="space-y-4">
            <Card className="border-[#215f47]/20 shadow-sm">
              <CardHeader className="px-6 pb-2 pt-6">
                <CardTitle className="text-lg font-medium text-[#215f47] flex items-center gap-2">
                  <ShieldCheck className="h-5 w-5" />
                  System Admins
                </CardTitle>
                <CardDescription className="text-gray-500">
                  Administrators with elevated system privileges
                </CardDescription>
              </CardHeader>
              <CardContent className="px-6 pb-6">
                <div className="rounded-md overflow-hidden border border-[#215f47]/20">
                  <Table>
                    <TableHeader className="bg-[#215f47]/5">
                      <TableRow>
                        <TableHead className="text-[#215f47] font-medium">Name</TableHead>
                        <TableHead className="text-[#215f47] font-medium">Email</TableHead>
                        <TableHead className="text-[#215f47] font-medium text-right w-[120px]">Actions</TableHead>
                      </TableRow>
                    </TableHeader>
                    <TableBody>
                      {systemAdmins.length > 0 ? (
                        systemAdmins.map((admin) => (
                          <TableRow key={admin.id} className="hover:bg-[#215f47]/5 transition-colors">
                            <TableCell>
                              {`${admin.firstName} ${admin.middleName ? admin.middleName + ' ' : ''}${admin.lastName}`}
                              {systemAdmins.length === 1 && (
                                <Badge variant="outline" className="ml-2 bg-amber-50 text-amber-700 border-amber-200">
                                  Last System Admin
                                </Badge>
                              )}
                            </TableCell>
                            <TableCell>{admin.email}</TableCell>
                            <TableCell className="text-right">
                              <Button
                                variant="ghost"
                                size="sm"
                                onClick={() => handleDemote(admin.id)}
                                className="h-8 w-8 p-0 text-amber-600 hover:text-amber-700 hover:bg-amber-50"
                                title="Demote to Regular Admin"
                                disabled={systemAdmins.length === 1}
                              >
                                <ArrowDown className="h-4 w-4" />
                              </Button>
                              <Button
                                variant="ghost"
                                size="sm"
                                onClick={() => handleEdit(admin.id)}
                                className="h-8 w-8 p-0 text-[#215f47] mx-1"
                                title="Edit System Admin"
                              >
                                <Pencil className="h-4 w-4" />
                              </Button>
                              <Button
                                variant="ghost"
                                size="sm"
                                onClick={() => handleDelete(admin.id)}
                                className="h-8 w-8 p-0 text-red-500 hover:text-red-700 hover:bg-red-50"
                                title="Delete System Admin"
                                disabled={systemAdmins.length === 1}
                              >
                                <Trash2 className="h-4 w-4" />
                              </Button>
                            </TableCell>
                          </TableRow>
                        ))
                      ) : (
                        <TableRow>
                          <TableCell colSpan={3} className="h-24 text-center text-muted-foreground">
                            No system admin users found. Promote an admin or add a new system admin.
                          </TableCell>
                        </TableRow>
                      )}
                    </TableBody>
                  </Table>
                </div>
              </CardContent>
              {systemAdmins.length === 1 && (
                <CardFooter className="px-6 pt-0 pb-6">
                  <Alert className="border-amber-300 bg-amber-50 w-full">
                    <AlertTriangle className="h-4 w-4 text-amber-700" />
                    <AlertDescription className="text-amber-800 text-sm">
                      You must always maintain at least one system admin user for security reasons.
                    </AlertDescription>
                  </Alert>
                </CardFooter>
              )}
            </Card>
          </TabsContent>
        </Tabs>
      </div>
    </DashboardLayout>
  );
};

export default AdminManagement;
