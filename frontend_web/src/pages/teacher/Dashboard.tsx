import { useState, useEffect } from 'react';
import DashboardLayout from '../../components/ui/layout/DashboardLayout';
import { useAuth } from '../../contexts/AuthContext';
import { sectionApi, type Section } from '../../lib/api/section';
import { attendanceApi } from '../../lib/api/attendance';
import { Calendar, Users, QrCode } from 'lucide-react';
import { Button } from '../../components/ui/button';

const TeacherDashboard = () => {
  const { user } = useAuth();
  const [sections, setSections] = useState<Section[]>([]);
  const [activeSection, setActiveSection] = useState<Section | null>(null);
  const [qrCodeData, setQrCodeData] = useState<string | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [isGeneratingQr, setIsGeneratingQr] = useState(false);
  const [error, setError] = useState<string | null>(null);

  // Fetch teacher's sections
  useEffect(() => {
    const fetchSections = async () => {
      if (!user?.id) return;
      
      try {
        setIsLoading(true);
        // Use the new getByTeacherId method that passes the teacherId as a parameter
        // This fixes the 500 error where the backend required the teacherId parameter
        const teacherSections = await sectionApi.getByTeacherId(user.id);
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
          <p className="text-lg">Loading dashboard data...</p>
        </div>
      </DashboardLayout>
    );
  }

  return (
    <DashboardLayout>
      <div className="space-y-6">
        <div className="flex flex-col justify-between sm:flex-row sm:items-center">
          <h2 className="text-xl font-semibold">Teacher Dashboard</h2>
          
          {sections.length > 0 && (
            <div className="mt-2 sm:mt-0">
              <select
                className="rounded-md border border-gray-300 bg-white px-3 py-2 text-sm"
                value={activeSection?.id || ''}
                onChange={(e) => handleSectionChange(Number(e.target.value))}
              >
                {sections.map((section) => (
                  <option key={section.id} value={section.id}>
                    Section ID: {section.id} ({section.sectionName})
                  </option>
                ))}
              </select>
            </div>
          )}
        </div>
        
        {error && (
          <div className="rounded-md bg-red-50 p-4">
            <div className="flex">
              <div className="ml-3">
                <h3 className="text-sm font-medium text-red-800">Error</h3>
                <div className="mt-2 text-sm text-red-700">
                  <p>{error}</p>
                </div>
              </div>
            </div>
          </div>
        )}
        
        {sections.length === 0 ? (
          <div className="rounded-lg bg-white p-6 shadow">
            <p className="text-center">You have not been assigned to any sections yet.</p>
          </div>
        ) : (
          <div className="grid grid-cols-1 gap-6 lg:grid-cols-2">
            {/* Section Information */}
            <div className="rounded-lg bg-white p-6 shadow">
              <h3 className="font-medium text-gray-900">Section Information</h3>
              
              {activeSection && (
                <div className="mt-4 space-y-4">
                  <div className="rounded-md bg-gray-50 p-4">
                    <div className="flex items-center">
                      <Calendar className="h-5 w-5 text-gray-400" />
                      <span className="ml-2 text-sm font-medium text-gray-700">Section ID:</span>
                      <span className="ml-2 text-sm text-gray-500">{activeSection.id}</span>
                    </div>
                  </div>
                  
                  <div className="rounded-md bg-gray-50 p-4">
                    <div className="flex items-center">
                      <Users className="h-5 w-5 text-gray-400" />
                      <span className="ml-2 text-sm font-medium text-gray-700">Section Name:</span>
                      <span className="ml-2 text-sm text-gray-500">
                        {activeSection.sectionName}
                      </span>
                    </div>
                  </div>
                  
                  <div className="rounded-md bg-gray-50 p-4">
                    <div className="flex items-center">
                      <QrCode className="h-5 w-5 text-gray-400" />
                      <span className="ml-2 text-sm font-medium text-gray-700">Enrollment Key:</span>
                      <span className="ml-2 text-sm text-gray-500">
                        {activeSection.enrollmentKey || 'No enrollment key generated'}
                      </span>
                    </div>
                  </div>
                  
                  <div className="flex space-x-2">
                    <Button 
                      className="flex-1" 
                      onClick={handleGenerateQrCode}
                      disabled={isGeneratingQr}
                    >
                      {isGeneratingQr ? 'Generating...' : 'Generate Attendance QR'}
                    </Button>
                  </div>
                </div>
              )}
            </div>
            
            {/* QR Code Display */}
            <div className="rounded-lg bg-white p-6 shadow">
              <h3 className="font-medium text-gray-900">Attendance QR Code</h3>
              
              <div className="mt-4 flex flex-col items-center justify-center">
                {qrCodeData ? (
                  <>
                    <div className="rounded-lg bg-white p-4 shadow">
                      <img 
                        src={`data:image/png;base64,${qrCodeData}`} 
                        alt="Attendance QR Code" 
                        className="h-64 w-64"
                      />
                    </div>
                    <p className="mt-4 text-sm text-gray-500">
                      Show this QR code to students to scan for attendance
                    </p>
                  </>
                ) : (
                  <div className="flex h-64 w-full items-center justify-center rounded-lg bg-gray-50">
                    <p className="text-center text-sm text-gray-500">
                      Generate a QR code to take attendance
                    </p>
                  </div>
                )}
              </div>
            </div>
          </div>
        )}
      </div>
    </DashboardLayout>
  );
};

export default TeacherDashboard;
