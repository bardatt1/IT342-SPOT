import { useState, useEffect } from 'react';
import { useAuth } from '../../contexts/AuthContext';
import { sectionApi } from '../../lib/api/section';
import { Section } from '../../lib/api/section';
import DashboardLayout from '../../components/ui/layout/DashboardLayout';
import { Button } from '../../components/ui/button';
import { Calendar, Users, QrCode, AlertTriangle, RefreshCw, Copy, Check } from 'lucide-react';

const TeacherSections = () => {
  const { user } = useAuth();
  const [sections, setSections] = useState<Section[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [generatingCode, setGeneratingCode] = useState<Record<number, boolean>>({});
  const [successMessage, setSuccessMessage] = useState<string | null>(null);
  const [copiedCodes, setCopiedCodes] = useState<Record<number, boolean>>({}); 

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
      <div className="container mx-auto px-4 py-8">
        <h1 className="text-2xl font-bold mb-6">My Sections</h1>
        
        {isLoading ? (
          <div className="flex justify-center items-center h-64">
            <div className="animate-spin rounded-full h-12 w-12 border-t-2 border-b-2 border-blue-500"></div>
          </div>
        ) : error ? (
          <div className="rounded-md bg-red-50 p-4">
            <div className="flex">
              <AlertTriangle className="h-5 w-5 text-red-400" />
              <div className="ml-3">
                <h3 className="text-sm font-medium text-red-800">{error}</h3>
              </div>
            </div>
          </div>
        ) : sections.length === 0 ? (
          <div className="bg-yellow-50 border-l-4 border-yellow-400 p-4">
            <div className="flex">
              <div className="flex-shrink-0">
                <AlertTriangle className="h-5 w-5 text-yellow-400" />
              </div>
              <div className="ml-3">
                <p className="text-sm text-yellow-700">
                  You are not assigned to any sections yet. Please contact an administrator if you believe this is an error.
                </p>
              </div>
            </div>
          </div>
        ) : (
          <>
            {successMessage && (
              <div className="bg-green-50 border-l-4 border-green-400 p-4 mb-4">
                <div className="flex">
                  <div className="flex-shrink-0">
                    <Check className="h-5 w-5 text-green-400" />
                  </div>
                  <div className="ml-3">
                    <p className="text-sm text-green-700">
                      {successMessage}
                    </p>
                  </div>
                </div>
              </div>
            )}
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
            {sections.map((section) => (
              <div key={section.id} className="bg-white shadow-md rounded-lg overflow-hidden">
                <div className="bg-blue-600 px-4 py-3">
                  <h3 className="text-lg font-medium text-white">
                    {section.sectionName}
                  </h3>
                </div>
                <div className="p-4">
                  <div className="flex items-center text-sm text-gray-600 mb-3">
                    <Calendar className="h-4 w-4 mr-2" />
                    <span>Section ID: {section.id}</span>
                  </div>
                  
                  <div className="flex items-center text-sm text-gray-600 mb-3">
                    <Users className="h-4 w-4 mr-2" />
                    <span>Students: {section.enrollmentCount}</span>
                  </div>
                  
                  <div className="flex items-center text-sm text-gray-600 mb-4">
                    <QrCode className="h-4 w-4 mr-2" />
                    <span className="flex-1">Enrollment Key: {section.enrollmentKey || 'Not set'}</span>
                    {section.enrollmentKey && (
                      <button 
                        onClick={() => copyToClipboard(section.enrollmentKey, section.id)}
                        className="p-1 ml-2 text-gray-500 hover:text-blue-600 rounded-md"
                        title="Copy to clipboard"
                      >
                        {copiedCodes[section.id] ? <Check className="h-4 w-4 text-green-500" /> : <Copy className="h-4 w-4" />}
                      </button>
                    )}
                  </div>
                  
                  <div className="flex space-x-2 mt-4">
                    <Button 
                      className="flex-1"
                      variant="outline"
                      onClick={() => window.location.href = `/teacher/attendance?sectionId=${section.id}`}
                    >
                      Attendance
                    </Button>
                    <Button 
                      className="flex-1"
                      variant="outline"
                      onClick={() => window.location.href = `/teacher/seats?sectionId=${section.id}`}
                    >
                      Seats
                    </Button>
                  </div>
                  
                  <div className="mt-2">
                    <Button 
                      className="w-full mt-2"
                      variant="secondary"
                      onClick={() => handleGenerateClassCode(section.id)}
                      disabled={generatingCode[section.id]}
                    >
                      {generatingCode[section.id] ? (
                        <>
                          <RefreshCw className="mr-2 h-4 w-4 animate-spin" />
                          Generating...
                        </>
                      ) : (
                        <>
                          <QrCode className="mr-2 h-4 w-4" />
                          Generate Class Code
                        </>
                      )}
                    </Button>
                  </div>
                </div>
              </div>
            ))}
            </div>
          </>
        )}
      </div>
    </DashboardLayout>
  );
};

export default TeacherSections;
