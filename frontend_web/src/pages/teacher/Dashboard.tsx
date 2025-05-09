import { useState, useEffect } from 'react';
import DashboardLayout from '../../components/ui/layout/DashboardLayout';
import { useAuth } from '../../contexts/AuthContext';
import { sectionApi, type Section } from '../../lib/api/section';
import { attendanceApi } from '../../lib/api/attendance';
import { Calendar, Users, QrCode, AlertTriangle, RefreshCw } from 'lucide-react';
import { Button } from '../../components/ui/button';
import FirstLoginModal from '../../components/ui/FirstLoginModal';
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from '../../components/ui/card';
import { Badge } from '../../components/ui/badge';
import { Alert, AlertDescription } from '../../components/ui/alert';

const TeacherDashboard = () => {
  const { user } = useAuth();
  const [sections, setSections] = useState<Section[]>([]);
  const [activeSection, setActiveSection] = useState<Section | null>(null);
  const [qrCodeData, setQrCodeData] = useState<{
    imageBase64: string;
    url: string;
    expiresInSeconds: number;
  } | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [isGeneratingQr, setIsGeneratingQr] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [showFirstLoginModal, setShowFirstLoginModal] = useState(false);

  // Check for temporary password when user data is loaded
  useEffect(() => {
    // Check if this is a temporary account based on email pattern
    const isTemporaryAccount = 
      // Check if email starts with the teacher's physical ID
      user?.email?.endsWith('@spot-edu.me') ||
      // Or if the backend explicitly flags it
      user?.hasTemporaryPassword;
    
    if (isTemporaryAccount) {
      setShowFirstLoginModal(true);
      
      // Store that we've shown the modal so it won't show again after closing
      // This is just for local testing until the backend sets the proper flag
      localStorage.setItem('firstTimeLogin', 'false');
    }
  }, [user]);

  // Fetch teacher's sections
  useEffect(() => {
    const fetchSections = async () => {
      if (!user?.id) return;
      
      try {
        setIsLoading(true);
        console.log(`Fetching sections for teacher with ID: ${user.id}`);
        
        // Use the getByTeacherId method that fetches sections and filters by teacher ID
        const teacherSections = await sectionApi.getByTeacherId(user.id);
        console.log(`Retrieved ${teacherSections.length} sections for teacher ${user.id}:`, teacherSections);
        setSections(teacherSections);
        
        // If teacher has sections, set the first one as active by default
        if (teacherSections.length > 0) {
          setActiveSection(teacherSections[0]);
        }
      } catch (error) {
        console.error('Error fetching sections:', error);
        setError('Failed to load sections. Please try again later.');
      } finally {
        setIsLoading(false);
      }
    };

    fetchSections();
  }, [user?.id]);

  const handleGenerateQrCode = async () => {
    if (!activeSection) return;
    
    try {
      setIsGeneratingQr(true);
      const qrData = await attendanceApi.generateQrCode(activeSection.id);
      setQrCodeData(qrData);
    } catch (error) {
      console.error('Error generating QR code:', error);
      setError('Failed to generate QR code. Please try again.');
    } finally {
      setIsGeneratingQr(false);
    }
  };

  const handleSectionChange = (sectionId: number) => {
    const section = sections.find(s => s.id === sectionId);
    if (section) {
      setActiveSection(section);
      setQrCodeData(null); // Reset QR code when changing sections
    }
  };

  if (isLoading) {
    return (
      <DashboardLayout>
        <div className="flex h-full items-center justify-center">
          <div className="flex items-center space-x-2">
            <div className="h-4 w-4 animate-spin rounded-full border-2 border-[#215f47] border-t-transparent"></div>
            <p className="text-lg font-medium text-[#215f47]">Loading dashboard data...</p>
          </div>
        </div>
      </DashboardLayout>
    );
  }

  return (
    <DashboardLayout>
      {showFirstLoginModal && (
        <FirstLoginModal onClose={() => setShowFirstLoginModal(false)} />
      )}
      <div className="space-y-6 p-6">
        <div className="flex flex-col justify-between sm:flex-row sm:items-center mb-4">
          <div>
            <h2 className="text-2xl font-bold text-[#215f47] flex items-center gap-2">
              <Users className="h-6 w-6" />
              Teacher Dashboard
            </h2>
            <p className="text-sm text-gray-500 mt-1">Manage your sections and attendance</p>
          </div>
          
          {sections.length > 0 && (
            <div className="mt-4 sm:mt-0 flex items-center">
              <div className="relative inline-block w-full min-w-[240px]">
                <select
                  className="appearance-none w-full rounded-md border border-[#215f47]/20 bg-white py-2 pl-3 pr-8 text-sm focus:border-[#215f47] focus:outline-none focus:ring-2 focus:ring-[#215f47]/20"
                  value={activeSection?.id || ''}
                  onChange={(e) => handleSectionChange(Number(e.target.value))}
                >
                  {sections.map((section) => (
                    <option key={section.id} value={section.id}>
                      {section.course?.courseCode} - {section.sectionName}
                    </option>
                  ))}
                </select>
                <div className="pointer-events-none absolute inset-y-0 right-0 flex items-center px-2 text-[#215f47]">
                  <svg className="h-4 w-4 fill-current" viewBox="0 0 20 20">
                    <path d="M5.293 7.293a1 1 0 011.414 0L10 10.586l3.293-3.293a1 1 0 111.414 1.414l-4 4a1 1 0 01-1.414 0l-4-4a1 1 0 010-1.414z" clipRule="evenodd" fillRule="evenodd"></path>
                  </svg>
                </div>
              </div>
            </div>
          )}
        </div>
        
        {error && (
          <Alert variant="destructive" className="border-red-300 bg-red-50 mb-4">
            <AlertTriangle className="h-5 w-5 text-red-600" />
            <AlertDescription className="text-red-700">{error}</AlertDescription>
          </Alert>
        )}
        
        {sections.length === 0 ? (
          <Card className="border-[#215f47]/20 shadow-sm">
            <CardContent className="p-6 flex flex-col items-center justify-center h-40">
              <div className="flex items-center gap-2 mb-2">
                <AlertTriangle className="h-5 w-5 text-amber-500" />
                <h3 className="font-medium text-[#215f47]">No Assigned Sections</h3>
              </div>
              <p className="text-gray-600 text-center">You have not been assigned to any sections yet. Please contact an administrator.</p>
            </CardContent>
          </Card>
        ) : (
          <div className="grid grid-cols-1 gap-6 lg:grid-cols-2">
            {/* Section Information */}
            <Card className="border-[#215f47]/20 shadow-sm hover:shadow transition-shadow duration-200">
              <CardHeader className="px-6 pb-2 pt-6">
                <div className="flex items-center justify-between">
                  <CardTitle className="text-lg font-medium text-[#215f47]">Section Information</CardTitle>
                  <Badge variant="outline" className="bg-[#215f47]/5 text-[#215f47] px-2 py-1">
                    Active
                  </Badge>
                </div>
                <CardDescription className="text-gray-500">
                  Details about your current section
                </CardDescription>
              </CardHeader>
              
              <CardContent className="px-6 pb-6">
                {activeSection && (
                  <div className="space-y-4">
                    <div className="flex items-center gap-3 p-3 rounded-md border border-[#215f47]/10 bg-[#215f47]/5">
                      <div className="flex h-10 w-10 items-center justify-center rounded-full bg-[#215f47]/10">
                        <Calendar className="h-5 w-5 text-[#215f47]" />
                      </div>
                      <div>
                        <span className="text-xs font-medium text-gray-500">Section Code</span>
                        <p className="text-sm font-medium text-[#215f47]">{activeSection.course?.courseCode}</p>
                      </div>
                    </div>
                    
                    <div className="flex items-center gap-3 p-3 rounded-md border border-[#215f47]/10 bg-[#215f47]/5">
                      <div className="flex h-10 w-10 items-center justify-center rounded-full bg-[#215f47]/10">
                        <Users className="h-5 w-5 text-[#215f47]" />
                      </div>
                      <div>
                        <span className="text-xs font-medium text-gray-500">Section Name</span>
                        <p className="text-sm font-medium text-[#215f47]">{activeSection.sectionName}</p>
                      </div>
                    </div>
                    
                    <div className="flex items-center gap-3 p-3 rounded-md border border-[#215f47]/10 bg-[#215f47]/5">
                      <div className="flex h-10 w-10 items-center justify-center rounded-full bg-[#215f47]/10">
                        <QrCode className="h-5 w-5 text-[#215f47]" />
                      </div>
                      <div>
                        <span className="text-xs font-medium text-gray-500">Enrollment Key</span>
                        <p className="text-sm font-medium text-[#215f47]">
                          {activeSection.enrollmentKey || 'No enrollment key generated'}
                        </p>
                      </div>
                    </div>
                    
                    <Button 
                      className="w-full mt-2 bg-[#215f47] hover:bg-[#215f47]/90 text-white gap-2 py-2" 
                      onClick={handleGenerateQrCode}
                      disabled={isGeneratingQr}
                    >
                      {isGeneratingQr ? (
                        <>
                          <RefreshCw className="h-4 w-4 animate-spin" />
                          Generating...
                        </>
                      ) : (
                        <>
                          <QrCode className="h-4 w-4" />
                          Generate Attendance QR
                        </>
                      )}
                    </Button>
                  </div>
                )}
              </CardContent>
            </Card>
            
            {/* QR Code Display */}
            <Card className="border-[#215f47]/20 shadow-sm hover:shadow transition-shadow duration-200">
              <CardHeader className="px-6 pb-2 pt-6">
                <CardTitle className="text-lg font-medium text-[#215f47]">Attendance QR Code</CardTitle>
                <CardDescription className="text-gray-500">
                  For students to scan and mark attendance
                </CardDescription>
              </CardHeader>
              
              <CardContent className="p-6 flex flex-col items-center justify-center min-h-[15rem]">
                {qrCodeData ? (
                  <>
                    <div className="rounded-lg bg-white p-4 shadow">
                      <img 
                        src={`data:image/png;base64,${qrCodeData.imageBase64}`} 
                        alt="Attendance QR Code" 
                        className="h-64 w-64"
                      />
                    </div>
                    <div className="mt-4 space-y-2 text-center">
                      <p className="text-sm text-gray-500">
                        Show this QR code to students to scan for attendance
                      </p>
                      {/* <p className="text-xs text-blue-600">
                        Link: <a href={qrCodeData.url} target="_blank" rel="noopener noreferrer">{qrCodeData.url}</a>
                      </p> */}
                      <p className="text-xs text-gray-400">
                        Expires in {Math.floor(qrCodeData.expiresInSeconds / 60)} minutes and {qrCodeData.expiresInSeconds % 60} seconds
                      </p>
                    </div>
                  </>
                ) : (
                  <div className="flex h-64 w-full items-center justify-center rounded-lg bg-gray-50">
                    <p className="text-center text-sm text-gray-500">
                      Generate a QR code to take attendance
                    </p>
                  </div>
                )}
              </CardContent>
            </Card>
          </div>
        )}
      </div>
    </DashboardLayout>
  );
};

export default TeacherDashboard;
