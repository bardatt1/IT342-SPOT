import { useState, useEffect } from 'react';
import { useAuth } from '../../contexts/AuthContext';
import { sectionApi } from '../../lib/api/section';
import { Section } from '../../lib/api/section';
import DashboardLayout from '../../components/ui/layout/DashboardLayout';
import { Button } from '../../components/ui/button';
import { Card, CardContent, CardDescription, CardFooter, CardHeader, CardTitle } from '../../components/ui/card';
import { Alert, AlertDescription, AlertTitle } from '../../components/ui/alert';
import { Badge } from '../../components/ui/badge';
import { 
  Calendar, 
  Users, 
  QrCode, 
  AlertTriangle, 
  RefreshCw, 
  Copy, 
  Check, 
  BookOpen, 
  GraduationCap, 
  CalendarDays,
  MapPin
} from 'lucide-react';
import FirstLoginModal from '../../components/ui/FirstLoginModal';

const TeacherSections = () => {
  const { user } = useAuth();
  const [sections, setSections] = useState<Section[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [generatingCode, setGeneratingCode] = useState<Record<number, boolean>>({});
  const [successMessage, setSuccessMessage] = useState<string | null>(null);
  const [copiedCodes, setCopiedCodes] = useState<Record<number, boolean>>({});
  const [showFirstLoginModal, setShowFirstLoginModal] = useState(false);

  // Check for temporary password when user data is loaded
  useEffect(() => {
    // Check if this is a temporary account based on email pattern
    const isTemporaryAccount = 
      // Check if email ends with @temporary.com
      user?.email?.endsWith('@temporary.com') ||
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
        const teacherSections = await sectionApi.getByTeacherId(user.id);
        setSections(teacherSections);
      } catch (error) {
        console.error('Error fetching sections:', error);
        setError('Failed to load sections. Please try again later.');
      } finally {
        setIsLoading(false);
      }
    };

    fetchSections();
  }, [user?.id]);
  
  // Generate class code for a section
  const handleGenerateClassCode = async (sectionId: number) => {
    try {
      setGeneratingCode(prev => ({ ...prev, [sectionId]: true }));
      setError(null);
      setSuccessMessage(null);
      
      const updatedSection = await sectionApi.generateClassCode(sectionId);
      
      // Update the section in the local state
      setSections(prevSections => 
        prevSections.map(section => 
          section.id === sectionId ? { ...section, enrollmentKey: updatedSection.enrollmentKey } : section
        )
      );
      
      setSuccessMessage(`Class code generated successfully!`);
      
      // Clear success message after 3 seconds
      setTimeout(() => {
        setSuccessMessage(null);
      }, 3000);
    } catch (error) {
      console.error('Error generating class code:', error);
      setError('Failed to generate class code. Please try again.');
    } finally {
      setGeneratingCode(prev => ({ ...prev, [sectionId]: false }));
    }
  };
  
  // Copy enrollment key to clipboard
  const copyToClipboard = (text: string, sectionId: number) => {
    navigator.clipboard.writeText(text)
      .then(() => {
        setCopiedCodes(prev => ({ ...prev, [sectionId]: true }));
        setTimeout(() => {
          setCopiedCodes(prev => ({ ...prev, [sectionId]: false }));
        }, 2000);
      })
      .catch(err => {
        console.error('Error copying to clipboard:', err);
      });
  };

  return (
    <DashboardLayout>
      {showFirstLoginModal && (
        <FirstLoginModal onClose={() => setShowFirstLoginModal(false)} />
      )}
      <div className="space-y-6 p-6">
        <div className="flex flex-col sm:flex-row sm:items-center justify-between mb-6">
          <div>
            <h2 className="text-2xl font-bold text-[#215f47] flex items-center gap-2">
              <BookOpen className="h-6 w-6" />
              My Sections
            </h2>
            <p className="text-gray-500 mt-1">Manage your class sections and enrollment</p>
          </div>
        </div>
        
        {isLoading ? (
          <div className="flex h-64 items-center justify-center">
            <div className="flex flex-col items-center space-y-4 text-center">
              <div className="animate-spin text-[#215f47]">
                <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                  <path d="M21 12a9 9 0 1 1-6.219-8.56"/>
                </svg>
              </div>
              <p className="text-lg font-medium text-gray-700">Loading your sections...</p>
            </div>
          </div>
        ) : error ? (
          <Alert variant="destructive" className="border-red-500/20 mb-6">
            <AlertTriangle className="h-4 w-4" />
            <AlertTitle>Error</AlertTitle>
            <AlertDescription>{error}</AlertDescription>
          </Alert>
        ) : sections.length === 0 ? (
          <Card className="border-[#215f47]/20 shadow-sm">
            <CardContent className="py-12 flex flex-col items-center justify-center text-center">
              <AlertTriangle className="h-12 w-12 text-amber-500/70 mb-4" />
              <h3 className="text-lg font-medium text-gray-700 mb-2">No Sections Assigned</h3>
              <p className="text-gray-500 max-w-sm">
                You are not assigned to any sections yet. Please contact an administrator if you believe this is an error.
              </p>
            </CardContent>
          </Card>
        ) : (
          <>
            {successMessage && (
              <Alert className="bg-green-50 border-green-500/20 text-green-800 mb-6">
                <Check className="h-4 w-4 text-green-500" />
                <AlertTitle className="text-green-800">Success</AlertTitle>
                <AlertDescription className="text-green-700">{successMessage}</AlertDescription>
              </Alert>
            )}
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
              {sections.map((section) => (
                <Card key={section.id} className="border-[#215f47]/20 shadow-sm overflow-hidden">
                  <CardHeader className="bg-[#215f47]/90 text-white pb-3 pt-5">
                    <CardTitle className="text-white flex items-center gap-2">
                      <GraduationCap className="h-5 w-5" />
                      {section.sectionName}
                    </CardTitle>
                    <CardDescription className="text-white/80">
                      <Badge variant="outline" className="bg-white/10 text-white font-mono border-white/20">
                        Section #{section.id}
                      </Badge>
                    </CardDescription>
                  </CardHeader>
                  <CardContent className="p-5 space-y-4">
                    <div className="grid grid-cols-2 gap-4">
                      <div className="rounded-md bg-[#215f47]/5 p-3 border border-[#215f47]/10">
                        <div className="flex items-center text-[#215f47] font-medium text-sm mb-1">
                          <Users className="h-3.5 w-3.5 mr-1.5" />
                          Students
                        </div>
                        <p className="text-lg font-semibold">
                          {section.enrollmentCount || 0}
                        </p>
                      </div>
                      
                      <div className="rounded-md bg-[#215f47]/5 p-3 border border-[#215f47]/10">
                        <div className="flex items-center text-[#215f47] font-medium text-sm mb-1">
                          <CalendarDays className="h-3.5 w-3.5 mr-1.5" />
                          Course
                        </div>
                        <p className="text-sm">
                          {section.course?.courseCode || 'Not set'}
                        </p>
                      </div>
                    </div>
                    
                    <div className="bg-white p-3 rounded-md border border-[#215f47]/10 flex items-center">
                      <div className="flex-1">
                        <div className="flex items-center text-[#215f47] font-medium text-sm mb-1">
                          <QrCode className="h-3.5 w-3.5 mr-1.5" />
                          Enrollment Key
                        </div>
                        <p className="font-mono text-sm bg-gray-50 p-1.5 rounded-sm border border-gray-100">
                          {section.enrollmentKey || 'Not generated'}
                        </p>
                      </div>
                      {section.enrollmentKey && (
                        <button 
                          onClick={() => copyToClipboard(section.enrollmentKey, section.id)}
                          className="ml-2 flex items-center justify-center h-8 w-8 rounded-full bg-[#215f47]/10 text-[#215f47] hover:bg-[#215f47]/20 transition-colors"
                          title="Copy to clipboard"
                        >
                          {copiedCodes[section.id] ? <Check className="h-4 w-4" /> : <Copy className="h-4 w-4" />}
                        </button>
                      )}
                    </div>
                  </CardContent>
                  <CardFooter className="px-5 pb-5 pt-0 grid grid-cols-2 gap-3">
                    <Button 
                      variant="outline" 
                      className="border-[#215f47]/20 text-[#215f47] hover:bg-[#215f47]/5 flex items-center justify-center"
                      onClick={() => window.location.href = `/teacher/attendance?sectionId=${section.id}`}
                    >
                      <Calendar className="mr-2 h-4 w-4" />
                      Attendance
                    </Button>
                    <Button 
                      variant="outline"
                      className="border-[#215f47]/20 text-[#215f47] hover:bg-[#215f47]/5 flex items-center justify-center"
                      onClick={() => window.location.href = `/teacher/seats?sectionId=${section.id}`}
                    >
                      <MapPin className="mr-2 h-4 w-4" />
                      Seats
                    </Button>
                    <Button 
                      className="col-span-2 bg-[#215f47] hover:bg-[#215f47]/90 text-white flex items-center justify-center"
                      onClick={() => handleGenerateClassCode(section.id)}
                      disabled={generatingCode[section.id]}
                    >
                      {generatingCode[section.id] ? (
                        <>
                          <RefreshCw className="mr-2 h-4 w-4 animate-spin" />
                          Generating Code...
                        </>
                      ) : (
                        <>
                          <QrCode className="mr-2 h-4 w-4" />
                          Generate Class Code
                        </>
                      )}
                    </Button>
                  </CardFooter>
                </Card>
              ))}
            </div>
          </>
        )}
      </div>
    </DashboardLayout>
  );
};

export default TeacherSections;
