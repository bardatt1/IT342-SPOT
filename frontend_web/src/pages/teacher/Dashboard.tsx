import { useState, useEffect, useRef } from 'react';
import DashboardLayout from '../../components/ui/layout/DashboardLayout';
import { useAuth } from '../../contexts/AuthContext';
import { sectionApi, type Section } from '../../lib/api/section';
import { attendanceApi } from '../../lib/api/attendance';
import { scheduleApi, type Schedule, daysOfWeek } from '../../lib/api/schedule';
import { Calendar, Users, QrCode, AlertTriangle, RefreshCw, Clock, MapPin, ChevronLeft, ChevronRight, CalendarClock } from 'lucide-react';
import { Button } from '../../components/ui/button';
import FirstLoginModal from '../../components/ui/FirstLoginModal';
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from '../../components/ui/card';
import { Badge } from '../../components/ui/badge';
import { Alert, AlertDescription } from '../../components/ui/alert';
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogDescription } from '../../components/ui/dialog';

const TeacherDashboard = () => {
  const { user } = useAuth();
  const [sections, setSections] = useState<Section[]>([]);
  const [activeSection, setActiveSection] = useState<Section | null>(null);
  const [sectionSchedules, setSectionSchedules] = useState<{[sectionId: number]: Schedule[]}>({});
  const [activeSchedules, setActiveSchedules] = useState<Schedule[]>([]);
  const [isWithinSchedule, setIsWithinSchedule] = useState(false);
  const [showScheduleDialog, setShowScheduleDialog] = useState(false);
  const [qrCodeData, setQrCodeData] = useState<{
    imageBase64: string;
    url: string;
    expiresInSeconds: number;
  } | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [isLoadingSchedules, setIsLoadingSchedules] = useState(false);
  const [isGeneratingQr, setIsGeneratingQr] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [showFirstLoginModal, setShowFirstLoginModal] = useState(false);
  const timeCheckIntervalRef = useRef<number | null>(null);

  // Check for temporary password when user data is loaded
  useEffect(() => {
    // Check if this is a temporary account based on email pattern
    const isTemporaryAccount = 
      // Check if email ends with @edu-spot.me
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
  
  // Fetch schedules for all sections when sections are loaded
  useEffect(() => {
    const fetchAllSchedules = async () => {
      if (sections.length === 0) return;
      
      setIsLoadingSchedules(true);
      console.log(`Fetching schedules for all sections`);
      
      // Create a copy of the current section schedules object
      const newSectionSchedules = {...sectionSchedules};
      
      // Fetch schedules for each section that doesn't already have schedules fetched
      const fetchPromises = sections.map(async (section) => {
        // Skip if we already have schedules for this section
        if (newSectionSchedules[section.id] !== undefined) return;
        
        try {
          console.log(`Fetching schedules for section with ID: ${section.id}`);
          const schedules = await scheduleApi.getBySectionId(section.id);
          console.log(`Retrieved ${schedules.length} schedules for section ${section.id}:`, schedules);
          newSectionSchedules[section.id] = schedules;
        } catch (error) {
          console.error(`Error fetching schedules for section ${section.id}:`, error);
          newSectionSchedules[section.id] = [];
        }
      });
      
      // Wait for all fetch operations to complete
      await Promise.all(fetchPromises);
      
      // Update state with all fetched schedules
      setSectionSchedules(newSectionSchedules);
      setIsLoadingSchedules(false);
    };

    fetchAllSchedules();
  }, [sections]);
  
  // Update active schedules when active section changes
  useEffect(() => {
    if (!activeSection?.id) {
      setActiveSchedules([]);
      return;
    }
    
    // Use schedules from the sectionSchedules object for the active section
    const schedules = sectionSchedules[activeSection.id] || [];
    setActiveSchedules(schedules);
  }, [activeSection?.id, sectionSchedules]);
  
  // Set up interval to check if current time is within schedule
  useEffect(() => {
    // Check if current time is within any schedule
    const checkTimeWithinSchedule = () => {
      if (activeSchedules.length === 0) {
        setIsWithinSchedule(false);
        return;
      }
      
      const now = new Date();
      const currentDay = now.getDay() === 0 ? 7 : now.getDay(); // Convert Sunday from 0 to 7 to match backend
      
      const currentHours = now.getHours();
      const currentMinutes = now.getMinutes();
      const currentTime = `${currentHours.toString().padStart(2, '0')}:${currentMinutes.toString().padStart(2, '0')}`;
      
      // Check if current time falls within any schedule for today
      const isWithin = activeSchedules.some(schedule => {
        // Check if schedule is for today
        if (schedule.dayOfWeek !== currentDay) return false;
        
        // Compare current time with schedule start and end times
        return currentTime >= schedule.timeStart && currentTime <= schedule.timeEnd;
      });
      
      setIsWithinSchedule(isWithin);
    };
    
    // Check immediately
    checkTimeWithinSchedule();
    
    // Set up interval to check every minute
    timeCheckIntervalRef.current = window.setInterval(checkTimeWithinSchedule, 60000);
    
    return () => {
      if (timeCheckIntervalRef.current !== null) {
        clearInterval(timeCheckIntervalRef.current);
      }
    };
  }, [activeSchedules]);

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
  
  const handlePrevSection = () => {
    const currentIndex = sections.findIndex(section => section.id === activeSection?.id);
    const newIndex = currentIndex <= 0 ? sections.length - 1 : currentIndex - 1;
    handleSectionChange(sections[newIndex].id);
  };
  
  const handleNextSection = () => {
    const currentIndex = sections.findIndex(section => section.id === activeSection?.id);
    const newIndex = currentIndex >= sections.length - 1 ? 0 : currentIndex + 1;
    handleSectionChange(sections[newIndex].id);
  };
  
  // Format time for display (12-hour format with AM/PM)
  const formatTimeForDisplay = (time: string): string => {
    if (!time) return '';
    const [hours, minutes] = time.split(':');
    const hour = parseInt(hours, 10);
    const period = hour >= 12 ? 'PM' : 'AM';
    const displayHour = hour % 12 || 12;
    return `${displayHour}:${minutes} ${period}`;
  };
  
  // Get day name from dayOfWeek number
  const getDayName = (dayOfWeek: number): string => {
    const day = daysOfWeek.find(d => d.value === dayOfWeek);
    return day ? day.label : '';
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
          <>
            {/* Section Carousel */}
            <div className="mb-6">
              <h3 className="text-lg font-medium text-[#215f47] mb-3">Your Class Sections</h3>
              <div className="relative">
                <div className="flex overflow-hidden">
                  <div className="flex transition-transform duration-300 ease-in-out">
                    {/* Section cards displayed in a row */}
                    <div className="flex space-x-4 p-2">
                      {sections.map((section) => (
                        <Card 
                          key={section.id} 
                          className={`w-64 flex-shrink-0 cursor-pointer transition-all duration-200 ${
                            activeSection?.id === section.id 
                              ? 'border-[#215f47] shadow-md' 
                              : 'border-gray-200 opacity-70 hover:opacity-100'
                          }`}
                          onClick={() => handleSectionChange(section.id)}
                        >
                          <CardHeader className="pb-2">
                            <CardTitle className="text-md font-medium">{section.course?.courseCode}</CardTitle>
                            <CardDescription>{section.sectionName}</CardDescription>
                          </CardHeader>
                          <CardContent className="pb-3">
                            <div className="text-sm">
                              {sectionSchedules[section.id] ? (
                                sectionSchedules[section.id].length > 0 ? (
                                  <Badge variant="outline" className="bg-[#215f47]/10 text-[#215f47] border-none">
                                    {sectionSchedules[section.id].length} Schedule(s)
                                  </Badge>
                                ) : (
                                  <Badge variant="outline" className="bg-gray-100 text-gray-500 border-none">
                                    No Schedule
                                  </Badge>
                                )
                              ) : (
                                <Badge variant="outline" className="bg-gray-100 text-gray-500 border-none">
                                  Loading...
                                </Badge>
                              )}
                            </div>
                          </CardContent>
                        </Card>
                      ))}
                    </div>
                  </div>
                </div>
                {sections.length > 1 && (
                  <>
                    <Button 
                      variant="outline" 
                      size="icon" 
                      className="absolute left-0 top-1/2 -translate-y-1/2 -ml-3 bg-white border-[#215f47]/20 hover:bg-[#215f47]/10 z-10"
                      onClick={handlePrevSection}
                    >
                      <ChevronLeft className="h-4 w-4" />
                    </Button>
                    <Button 
                      variant="outline" 
                      size="icon" 
                      className="absolute right-0 top-1/2 -translate-y-1/2 -mr-3 bg-white border-[#215f47]/20 hover:bg-[#215f47]/10 z-10"
                      onClick={handleNextSection}
                    >
                      <ChevronRight className="h-4 w-4" />
                    </Button>
                  </>
                )}
              </div>
            </div>
          
            <div className="grid grid-cols-1 gap-6 lg:grid-cols-2">
            {/* Section Information */}
            <Card className="border-[#215f47]/20 shadow-sm hover:shadow transition-shadow duration-200">
              <CardHeader className="px-6 pb-2 pt-6">
                <div className="flex items-center justify-between">
                  <CardTitle className="text-lg font-medium text-[#215f47]">Section Information</CardTitle>
                  <Badge variant="outline" className="bg-[#215f47]/5 text-[#215f47] px-2 py-1">
                    {isWithinSchedule ? 'Currently Active' : 'Inactive'}
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
                        <CalendarClock className="h-5 w-5 text-[#215f47]" />
                      </div>
                      <div className="flex-1">
                        <span className="text-xs font-medium text-gray-500">Schedules</span>
                        {isLoadingSchedules ? (
                          <p className="text-sm text-gray-500">Loading schedules...</p>
                        ) : activeSchedules.length > 0 ? (
                          <div>
                            <div className="flex justify-between items-center">
                              <p className="text-sm font-medium text-[#215f47]">{activeSchedules.length} schedule(s)</p>
                              <Button 
                                variant="link" 
                                className="text-xs text-[#215f47] p-0 h-auto" 
                                onClick={() => setShowScheduleDialog(true)}
                              >
                                View All
                              </Button>
                            </div>
                            <div className="mt-1">
                              {activeSchedules.slice(0, 1).map(schedule => (
                                <div key={schedule.id} className="text-xs text-gray-600">
                                  {getDayName(schedule.dayOfWeek)}, {formatTimeForDisplay(schedule.timeStart)} - {formatTimeForDisplay(schedule.timeEnd)}
                                </div>
                              ))}
                              {activeSchedules.length > 1 && (
                                <div className="text-xs text-gray-500 mt-1">{activeSchedules.length - 1} more schedule(s)...</div>
                              )}
                            </div>
                          </div>
                        ) : (
                          <p className="text-sm text-gray-500">No schedules available</p>
                        )}
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
                    
                    {(!isWithinSchedule || activeSchedules.length === 0) && (
                      <Alert className="bg-amber-50 border-amber-200 mt-2">
                        <AlertTriangle className="h-4 w-4 text-amber-500" />
                        <AlertDescription className="text-amber-700 text-sm">
                          {activeSchedules.length === 0 
                            ? "QR generation is disabled because this section has no schedule set up."
                            : "QR generation is disabled because the current time is outside of class schedule."}
                        </AlertDescription>
                      </Alert>
                    )}
                    
                    <Button 
                      className="w-full mt-2 bg-[#215f47] hover:bg-[#215f47]/90 text-white gap-2 py-2" 
                      onClick={handleGenerateQrCode}
                      disabled={isGeneratingQr || !isWithinSchedule || activeSchedules.length === 0}
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
            
            {/* Schedule Dialog */}
            <Dialog open={showScheduleDialog} onOpenChange={setShowScheduleDialog}>
              <DialogContent className="sm:max-w-md">
                <DialogHeader>
                  <DialogTitle>Class Schedules</DialogTitle>
                  <DialogDescription>
                    {activeSection?.course?.courseCode} - {activeSection?.sectionName}
                  </DialogDescription>
                </DialogHeader>
                <div className="py-4">
                  {activeSchedules.length === 0 ? (
                    <p className="text-center text-gray-500 py-4">No schedules for this section</p>
                  ) : (
                    <div className="space-y-4">
                      {activeSchedules.map(schedule => (
                        <div key={schedule.id} className="flex items-start gap-3 p-3 rounded-md border border-[#215f47]/10 bg-[#215f47]/5">
                          <div className="flex-shrink-0 mt-1">
                            <div className="flex h-8 w-8 items-center justify-center rounded-full bg-[#215f47]/10">
                              <Clock className="h-4 w-4 text-[#215f47]" />
                            </div>
                          </div>
                          <div className="flex-1">
                            <p className="text-sm font-medium text-[#215f47]">{getDayName(schedule.dayOfWeek)}</p>
                            <p className="text-sm text-gray-600 mt-1">
                              {formatTimeForDisplay(schedule.timeStart)} - {formatTimeForDisplay(schedule.timeEnd)}
                            </p>
                          </div>
                          <div className="flex items-center gap-1">
                            <MapPin className="h-3 w-3 text-gray-400" />
                            <span className="text-xs text-gray-500">{schedule.room}</span>
                          </div>
                        </div>
                      ))}
                    </div>
                  )}
                </div>
              </DialogContent>
            </Dialog>
          </>
        )}
      </div>
    </DashboardLayout>
  );
};

export default TeacherDashboard;
