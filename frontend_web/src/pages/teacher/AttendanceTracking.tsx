import { useState, useEffect, useMemo } from 'react';
import { useSearchParams } from 'react-router-dom';
import DashboardLayout from '../../components/ui/layout/DashboardLayout';
import { useAuth } from '../../contexts/AuthContext';
import { sectionApi, type Section } from '../../lib/api/section';
import { attendanceApi, type Attendance, type AttendanceAnalytics } from '../../lib/api/attendance';
import { studentApi, type Student } from '../../lib/api/student';
import { scheduleApi, type Schedule } from '../../lib/api/schedule';

// UI Components
import { Button } from '../../components/ui/button';
import { Input } from '../../components/ui/input';
import { Label } from '../../components/ui/label';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '../../components/ui/card';
import { Alert, AlertDescription, AlertTitle } from '../../components/ui/alert';
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '../../components/ui/table';
import { Tabs, TabsList, TabsTrigger } from '../../components/ui/tabs';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '../../components/ui/select';
import { Badge } from '../../components/ui/badge';

// Icons
import { 
  User, 
  Users,
  Calendar, 
  Clock, 
  BarChart, 
  FileSpreadsheet,
  CalendarDays, 
  CircleAlert, 
  ClipboardCheck, 
  ListFilter, 
  AlertTriangle, 
  FilterX,
  RefreshCw
} from 'lucide-react';
import FirstLoginModal from '../../components/ui/FirstLoginModal';

const AttendanceTracking = () => {
  const { user } = useAuth();
  const [searchParams] = useSearchParams();
  const [sections, setSections] = useState<Section[]>([]);
  const [selectedSectionId, setSelectedSectionId] = useState<number | null>(null);
  const [attendanceRecords, setAttendanceRecords] = useState<Attendance[]>([]);
  const [students, setStudents] = useState<Student[]>([]);
  const [schedules, setSchedules] = useState<Schedule[]>([]);
  const [analytics, setAnalytics] = useState<AttendanceAnalytics | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [isExporting, setIsExporting] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [dateFilter, setDateFilter] = useState<string>('');
  const [showFirstLoginModal, setShowFirstLoginModal] = useState(false);

  // Analytics view state
  const [showAnalytics, setShowAnalytics] = useState<boolean>(false);

  useEffect(() => {
    fetchTeacherSections();
    
    // Check if this is a temporary account based on email pattern
    const isTemporaryAccount = 
      // Check if email ends with @edu-spot.me
      user?.email?.endsWith('@edu-spot.me') ||
      // Or if the backend explicitly flags it
      user?.hasTemporaryPassword;
    
    if (isTemporaryAccount) {
      setShowFirstLoginModal(true);
      
      // Store that we've shown the modal so it won't show again after closing
      // This is just for local testing until the backend sets the proper flag
      localStorage.setItem('firstTimeLogin', 'false');
    }
  }, [user]);
  
  // Get section ID from URL if present
  useEffect(() => {
    const sectionIdParam = searchParams.get('sectionId');
    if (sectionIdParam && sections.length > 0) {
      const parsedId = parseInt(sectionIdParam, 10);
      // Check if the section exists and belongs to this teacher
      const sectionExists = sections.some(s => s.id === parsedId);
      if (sectionExists) {
        setSelectedSectionId(parsedId);
      }
    }
  }, [sections, searchParams]);

  useEffect(() => {
    fetchTeacherSections();
  }, [user?.id]);

  useEffect(() => {
    if (selectedSectionId) {
      const fetchSectionData = async () => {
        try {
          setIsLoading(true);
          setError(null);

          // Fetch data in parallel for better performance
          const dataPromises = [
            fetchAttendanceData(selectedSectionId),
            fetchStudentData(selectedSectionId),
            fetchScheduleData(selectedSectionId)
          ];

          await Promise.all(dataPromises);

          setIsLoading(false);
        } catch (error) {
          console.error(`Error fetching data for section ${selectedSectionId}:`, error);
          setError('Failed to load section data. Please try again later.');
          setIsLoading(false);
        }
      };

      fetchSectionData();
    }
  }, [selectedSectionId]);

  useEffect(() => {
    console.log('Attendance records updated:', attendanceRecords.length, 'for section:', selectedSectionId);
    console.log('Students updated:', students.length, 'for section:', selectedSectionId);
    console.log('Schedules updated:', schedules.length, 'for section:', selectedSectionId);

    if (selectedSectionId) {
      calculateAnalyticsFromAttendance(selectedSectionId);
    }
  }, [selectedSectionId, attendanceRecords, students, schedules]);

  const fetchTeacherSections = async () => {
    if (!user?.id) return;

    try {
      setIsLoading(true);
      const allSections = await sectionApi.getAllSections();
      const teacherSections = allSections.filter(section => section.teacherId === user.id);
      setSections(teacherSections);

      // Select first section by default if available
      if (teacherSections.length > 0 && !selectedSectionId) {
        setSelectedSectionId(teacherSections[0].id);
      }
    } catch (error) {
      console.error('Error fetching sections:', error);
      setError('Failed to load your sections. Please try again later.');
    } finally {
      setIsLoading(false);
    }
  };

  // Fetch attendance data for a section
  const fetchAttendanceData = async (sectionId: number) => {
    try {
      const response = await attendanceApi.getSectionAttendance(sectionId);

      // Log the response to debug
      console.log(`Attendance API response for section ${sectionId}:`, response);

      // Ensure we're working with an array
      let records: Attendance[] = [];

      if (Array.isArray(response)) {
        records = response;
      } else if (response && typeof response === 'object') {
        // If it's a response object with a data property
        const responseObj = response as Record<string, any>;
        if (Array.isArray(responseObj.data)) {
          records = responseObj.data;
        } else if (responseObj.data && typeof responseObj.data === 'object') {
          // Sometimes the data might be nested further
          const dataObj = responseObj.data as Record<string, any>;
          if (Array.isArray(dataObj.data)) {
            records = dataObj.data;
          }
        }
      }

      // Log each record to inspect studentId - for debugging
      records.forEach((record, index) => {
        console.log(`Attendance record ${index}:`, {
          id: record.id,
          date: record.date,
          studentId: record.studentId,
          student: record.student ? `${record.student.firstName} ${record.student.lastName} (ID: ${record.student.id})` : 'No student data'
        });
      });

      console.log('Processed attendance records:', records);
      setAttendanceRecords(Array.isArray(records) ? records : []);
      setError(null);
    } catch (error) {
      console.error('Error fetching attendance data:', error);
      setError('Failed to load attendance data. Please try again later.');
      setAttendanceRecords([]);
    } finally {
      setIsLoading(false);
    }
  };

  // Fetch student data for a section
  const fetchStudentData = async (sectionId: number) => {
    try {
      const response = await studentApi.getBySection(sectionId);

      // Log the response to debug
      console.log(`Student API response for section ${sectionId}:`, response);

      // Ensure we're working with an array
      let students: Student[] = [];

      if (Array.isArray(response)) {
        students = response;
      } else if (response && typeof response === 'object') {
        // If it's a response object with a data property
        const responseObj = response as Record<string, any>;
        if (Array.isArray(responseObj.data)) {
          students = responseObj.data;
        } else if (responseObj.data && typeof responseObj.data === 'object') {
          // Sometimes the data might be nested further
          const dataObj = responseObj.data as Record<string, any>;
          if (Array.isArray(dataObj.data)) {
            students = dataObj.data;
          }
        }
      }

      console.log('Processed student data:', students);
      setStudents(Array.isArray(students) ? students : []);
      setError(null);
    } catch (error) {
      console.error('Error fetching student data:', error);
      setError('Failed to load student data. Please try again later.');
      setStudents([]);
    } finally {
      setIsLoading(false);
    }
  };

  // Fetch schedule data for a section
  const fetchScheduleData = async (sectionId: number) => {
    try {
      const response = await scheduleApi.getBySectionId(sectionId);

      // Log the response to debug
      console.log(`Schedule API response for section ${sectionId}:`, response);

      // Ensure we're working with an array
      let schedules: Schedule[] = [];

      if (Array.isArray(response)) {
        schedules = response;
      } else if (response && typeof response === 'object') {
        // If it's a response object with a data property
        const responseObj = response as Record<string, any>;
        if (Array.isArray(responseObj.data)) {
          schedules = responseObj.data;
        } else if (responseObj.data && typeof responseObj.data === 'object') {
          // Sometimes the data might be nested further
          const dataObj = responseObj.data as Record<string, any>;
          if (Array.isArray(dataObj.data)) {
            schedules = dataObj.data;
          }
        }
      }

      console.log('Processed schedule data:', schedules);
      setSchedules(Array.isArray(schedules) ? schedules : []);
      setError(null);
    } catch (error) {
      console.error('Error fetching schedule data:', error);
      setError('Failed to load schedule data. Please try again later.');
      setSchedules([]);
    } finally {
      setIsLoading(false);
    }
  };

  const handleSectionChange = (e: React.ChangeEvent<HTMLSelectElement>) => {
    const sectionId = parseInt(e.target.value);

    // Reset state when changing sections
    setSelectedSectionId(sectionId);
    setAttendanceRecords([]); // Clear old attendance records
    setStudents([]);       // Clear old student data
    setSchedules([]);      // Clear old schedule data
    setAnalytics(null);    // Clear old analytics data
    setError(null);        // Clear any existing error messages 
    setIsLoading(true);    // Show loading indicator
    console.log('Switching to section:', sectionId);
  };

  const handleDateFilterChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    setDateFilter(e.target.value);
  };

  const clearFilters = () => {
    setDateFilter('');
  };

  // Toggle between attendance records and analytics view
  const handleViewToggle = (value: string) => {
    setShowAnalytics(value === "analytics");
  };

  // Calculate analytics directly from attendance records
  const calculateAnalyticsFromAttendance = async (sectionId: number) => {
    try {
      console.log('Calculating analytics for section:', sectionId);
      console.log('Using attendance records:', attendanceRecords);

      // First, ensure we have all the necessary data
      const diagnostics = await attendanceApi.checkSectionAnalyticsData(sectionId);
      console.log('Analytics prerequisites check:', diagnostics);

      // Even if prerequisites check fails, still attempt to calculate analytics
      // with the data we have, to avoid showing an empty analytics dashboard

      // If we have attendance records, calculate attendance metrics
      if (attendanceRecords.length === 0) {
        console.log('No attendance records to calculate analytics from');
        // Create empty analytics structure with enrollment count
        const emptyAnalytics: AttendanceAnalytics = {
          totalSessions: 0,
          totalStudents: diagnostics.enrollmentCount || 0,
          averageAttendance: 0,
          attendanceByDate: [],
          studentAttendance: []
        };
        setAnalytics(emptyAnalytics);
        return;
      }

      // Group attendance by date
      const attendanceByDate: Record<string, any> = {};
      const studentAttendance: Record<number, any> = {};

      // Get unique dates from attendance records, normalizing format
      const uniqueDates = [...new Set(attendanceRecords.map(record => {
        // Normalize date format to YYYY-MM-DD
        return new Date(record.date).toISOString().split('T')[0];
      }))];
      console.log('Unique attendance dates:', uniqueDates);

      // Get unique student IDs both from student records and attendance records
      const enrolledStudentIds = students.map(student => student.id);
      const attendanceStudentIds = attendanceRecords.map(record => {
        // Try to extract student ID either from studentId property or from nested student object
        if (record.studentId) return record.studentId;
        if (record.student && record.student.id) return record.student.id;

        // Check if there's a nested student object with ID
        const recordAsAny = record as any;
        if (recordAsAny.student && recordAsAny.student.id) {
          return recordAsAny.student.id;
        }

        return undefined;
      }).filter(id => id !== undefined);

      console.log('Enrolled student IDs:', enrolledStudentIds);
      console.log('Attendance student IDs:', attendanceStudentIds);

      // Combine the two sets to ensure we have all students
      const allStudentIds = [...new Set([...enrolledStudentIds, ...attendanceStudentIds])];
      const totalStudents = diagnostics.enrollmentCount || students.length || allStudentIds.length;
      console.log('All student IDs:', allStudentIds, 'Total students:', totalStudents);

      // Calculate attendance for each date
      uniqueDates.forEach(date => {
        // Use normalized dates for filtering
        const recordsOnDate = attendanceRecords.filter(r => {
          const normalizedRecordDate = new Date(r.date).toISOString().split('T')[0];
          return normalizedRecordDate === date;
        });

        const attendanceCount = recordsOnDate.length;
        const percentage = totalStudents > 0 ? (attendanceCount / totalStudents) * 100 : 0;
        console.log(`Date: ${date}, Count: ${attendanceCount}, Percentage: ${percentage}%`);

        attendanceByDate[date] = {
          date, // Keep the normalized date
          count: attendanceCount,
          percentage
        };
      });

      // Calculate student attendance stats - using directly fetched student data
      students.forEach(student => {
        const studentId = student.id;

        // Try multiple ways to match student to attendance records
        const studentRecords = attendanceRecords.filter(r => {
          // Check direct studentId property match
          if (r.studentId === studentId) return true;

          // Check nested student object match
          if (r.student && r.student.id === studentId) return true;

          // Try to access potentially nested properties (fallback)
          const record = r as any;
          if (record.student && record.student.id === studentId) return true;

          return false;
        });

        // Log what we found for debugging
        console.log(`Student ${student.firstName} ${student.lastName} (Physical ID: ${student.studentPhysicalId}, DB ID: ${studentId}):`, {
          foundRecords: studentRecords.length,
          records: studentRecords.map(r => ({ id: r.id, date: r.date }))
        });

        const attendanceCount = studentRecords.length;
        const attendancePercentage = uniqueDates.length > 0
          ? (attendanceCount / uniqueDates.length) * 100
          : 0;

        console.log(`Student ${student.firstName} ${student.lastName} (Physical ID: ${student.studentPhysicalId}), Attendance: ${attendanceCount}/${uniqueDates.length} (${attendancePercentage.toFixed(1)}%)`);

        studentAttendance[studentId] = {
          studentId: student.studentPhysicalId || studentId.toString(),
          studentName: `${student.firstName} ${student.lastName}`,
          attendanceCount,
          attendancePercentage
        };
      });

      // Also check if there are any attendance records without matching students
      // and add them to the student attendance data (just in case)
      attendanceRecords.forEach(record => {
        let recordStudentId: number | undefined;

        // Try to extract student ID from the record
        if (record.studentId) {
          recordStudentId = record.studentId;
        } else if (record.student && record.student.id) {
          recordStudentId = record.student.id;
        } else {
          // Try to access as any (fallback)
          const recordAny = record as any;
          if (recordAny.student && recordAny.student.id) {
            recordStudentId = recordAny.student.id;
          }
        }

        // Skip if no student ID found or if we already have this student
        if (!recordStudentId || studentAttendance[recordStudentId]) return;

        // Get student name from the record if possible
        let studentName = `Student ${recordStudentId}`;
        if (record.student) {
          const student = record.student as any;
          if (student.firstName || student.lastName) {
            studentName = `${student.firstName || ''} ${student.lastName || ''}`.trim();
          }
        }

        // Count how many records this student has
        const studentRecords = attendanceRecords.filter(r => {
          // Try multiple ways to match
          if (r.studentId === recordStudentId) return true;
          if (r.student && r.student.id === recordStudentId) return true;
          const rAny = r as any;
          if (rAny.student && rAny.student.id === recordStudentId) return true;
          return false;
        });

        const attendanceCount = studentRecords.length;
        const attendancePercentage = uniqueDates.length > 0
          ? (attendanceCount / uniqueDates.length) * 100
          : 0;

        console.log(`Student from attendance record (ID: ${recordStudentId}), Attendance: ${attendanceCount}/${uniqueDates.length} (${attendancePercentage.toFixed(1)}%)`);

        // Try to get physical ID if possible
        let physicalId = '';
        if (record.student && record.student.studentPhysicalId) {
          physicalId = record.student.studentPhysicalId;
        }

        studentAttendance[recordStudentId] = {
          studentId: physicalId || recordStudentId.toString(),
          studentName,
          attendanceCount,
          attendancePercentage
        };
      });

      // Calculate average attendance percentage
      const totalAttendanceCount = attendanceRecords.length;
      const averageAttendance = totalStudents > 0 && uniqueDates.length > 0
        ? (totalAttendanceCount / (totalStudents * uniqueDates.length)) * 100
        : 0;

      // Create analytics object
      const calculatedAnalytics: AttendanceAnalytics = {
        totalSessions: uniqueDates.length,
        totalStudents,
        averageAttendance,
        attendanceByDate: Object.values(attendanceByDate),
        studentAttendance: Object.values(studentAttendance)
      };

      console.log('Calculated analytics from attendance records:', calculatedAnalytics);
      setAnalytics(calculatedAnalytics);

    } catch (error) {
      console.error('Error calculating analytics:', error);
      setError('Failed to calculate analytics data.');
    }
  };

  const exportAttendance = () => {
    setIsExporting(true);

    try {
      // Use the same filtered records that are displayed in the UI
      // (this uses the memoized value from useMemo)

      // Convert to CSV
      const headers = ['Student ID', 'Student Name', 'Section ID', 'Date', 'Start Time', 'End Time'];
      const csvContent = [
        headers.join(','),
        ...filteredRecords.map((record: Attendance) => [
          record.student?.studentPhysicalId || '',
          record.student ? `${record.student.firstName} ${record.student.lastName}` : '',
          record.sectionId,
          record.date,
          record.startTime,
          record.endTime || ''
        ].join(','))
      ].join('\n');

      // Create and download file
      const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' });
      const url = URL.createObjectURL(blob);
      const link = document.createElement('a');
      link.setAttribute('href', url);
      link.setAttribute('download', `attendance_section_${selectedSectionId}_${new Date().toISOString().split('T')[0]}.csv`);
      link.style.visibility = 'hidden';
      document.body.appendChild(link);
      link.click();
      document.body.removeChild(link);
    } catch (error) {
      console.error('Error exporting attendance:', error);
      setError('Failed to export attendance data. Please try again later.');
    } finally {
      setIsExporting(false);
    }
  };

  const exportAnalytics = () => {
    if (!analytics) return;

    setIsExporting(true);

    try {
      // Prepare data for export
      const headers = ['Date', 'Attendance Count', 'Attendance Percentage'];
      const rowData = analytics.attendanceByDate.map(item => [
        new Date(item.date).toLocaleDateString(),
        item.count,
        item.percentage.toFixed(2) + '%'
      ]);

      // Add student attendance data
      const studentHeaders = ['\n\nStudent ID', 'Student Name', 'Attendance Count', 'Attendance Percentage'];
      const studentData = analytics.studentAttendance.map(student => [
        student.studentId,
        student.studentName,
        student.attendanceCount,
        student.attendancePercentage.toFixed(2) + '%'
      ]);

      // Create CSV content
      const csvContent = [
        `Attendance Analytics for Section ${selectedSectionId}`,
        `Generated on: ${new Date().toLocaleString()}`,
        `\nTotal Students: ${analytics.totalStudents}`,
        `Total Sessions: ${analytics.totalSessions}`,
        `Average Attendance: ${analytics.averageAttendance.toFixed(2)}%`,
        '\nAttendance by Date:',
        headers.join(','),
        ...rowData.map(row => row.join(',')),
        '\nStudent Attendance:',
        studentHeaders.join(','),
        ...studentData.map(row => row.join(','))
      ].join('\n');

      // Create and download the file
      const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' });
      const url = URL.createObjectURL(blob);
      const link = document.createElement('a');
      link.setAttribute('href', url);
      link.setAttribute('download', `analytics_section_${selectedSectionId}_${new Date().toISOString().split('T')[0]}.csv`);
      link.style.visibility = 'hidden';
      document.body.appendChild(link);
      link.click();
      document.body.removeChild(link);
    } catch (error) {
      console.error('Error exporting analytics:', error);
      setError('Failed to export analytics data. Please try again later.');
    } finally {
      setIsExporting(false);
    }
  };

  const formatDate = (dateString: string) => {
    const options: Intl.DateTimeFormatOptions = { 
      year: 'numeric', 
      month: 'long', 
      day: 'numeric' 
    };
    return new Date(dateString).toLocaleDateString(undefined, options);
  };

  const formatTime = (timeString: string) => {
    const options: Intl.DateTimeFormatOptions = {
      hour: '2-digit',
      minute: '2-digit'
    };
    return new Date(`2000-01-01T${timeString}`).toLocaleTimeString(undefined, options);
  };

  // Use useMemo to calculate filtered records only when dependencies change
  const filteredRecords = useMemo(() => {
    if (!dateFilter) return attendanceRecords;

    console.log('Filtering by date:', dateFilter);
    console.log('Attendance records:', attendanceRecords);

    // Convert dateFilter to YYYY-MM-DD format for comparison
    const formattedDateFilter = new Date(dateFilter).toISOString().split('T')[0];
    console.log('Formatted date filter:', formattedDateFilter);

    // Try different date formats for matching
    return attendanceRecords.filter(record => {
      // Try exact match first
      if (record.date === dateFilter) return true;

      // Try formatted match
      if (record.date === formattedDateFilter) return true;

      // Try comparing as dates
      const recordDate = new Date(record.date).toISOString().split('T')[0];
      return recordDate === formattedDateFilter;
    });
  }, [attendanceRecords, dateFilter]);

  if (isLoading && sections.length === 0) {
    return (
      <DashboardLayout>
        <div className="flex h-full items-center justify-center">
          <p className="text-lg">Loading your sections...</p>
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
        <div className="flex flex-col sm:flex-row sm:items-center justify-between mb-6">
          <div>
            <h2 className="text-2xl font-bold text-[#215f47] flex items-center gap-2">
              <ClipboardCheck className="h-6 w-6" />
              Attendance Tracking
            </h2>
            <p className="text-gray-500 mt-1">Manage and analyze student attendance records</p>
          </div>
          
          {sections.length > 0 && (
            <div className="mt-4 sm:mt-0 min-w-[200px]">
              <Select
                value={selectedSectionId?.toString() || ''}
                onValueChange={(value) => handleSectionChange({ target: { value } } as any)}
              >
                <SelectTrigger className="border-[#215f47]/20 focus:ring-[#215f47]/20 focus:border-[#215f47]">
                  <SelectValue placeholder="Select a section" />
                </SelectTrigger>
                <SelectContent>
                  {sections.map((section) => (
                    <SelectItem key={section.id} value={section.id.toString()}>
                      {section.course?.courseCode || 'Course'} - {section.sectionName}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>
          )}
        </div>
        
        {error && (
          <Alert variant="destructive">
            <AlertTriangle className="h-4 w-4" />
            <AlertTitle>Error</AlertTitle>
            <AlertDescription>{error}</AlertDescription>
          </Alert>
        )}
        
        {sections.length === 0 ? (
          <Card className="border-[#215f47]/20 shadow-sm">
            <CardContent className="flex h-32 items-center justify-center">
              <p className="text-center text-gray-500">
                You have not been assigned to any sections yet.
              </p>
            </CardContent>
          </Card>
        ) : (
          <Tabs defaultValue={showAnalytics ? "analytics" : "attendance"} className="w-full" onValueChange={handleViewToggle}>
            <TabsList className="mb-4 grid w-full grid-cols-2 bg-[#f8f9fa]">
              <TabsTrigger 
                value="attendance" 
                className="data-[state=active]:bg-[#215f47] data-[state=active]:text-white"
              >
                <User className="mr-2 h-4 w-4" />
                Attendance Records
              </TabsTrigger>
              <TabsTrigger 
                value="analytics" 
                className="data-[state=active]:bg-[#215f47] data-[state=active]:text-white"
              >
                <BarChart className="mr-2 h-4 w-4" />
                Analytics Dashboard
              </TabsTrigger>
            </TabsList>
          </Tabs>
        )}
        
        {sections.length === 0 ? null : !showAnalytics ? (
          <>
            <Card className="border-[#215f47]/20 shadow-sm">
              <CardHeader>
                <CardTitle className="text-lg font-medium text-[#215f47]">
                  <div className="flex items-center">
                    <ListFilter className="mr-2 h-5 w-5" />
                    Attendance Filters
                  </div>
                </CardTitle>
              </CardHeader>
              <CardContent>
                <div className="flex flex-wrap items-end gap-4">
                  <div className="w-full sm:w-auto">
                    <Label htmlFor="dateFilter" className="mb-2 block text-sm font-medium text-gray-700">
                      Attendance Date
                    </Label>
                    <Input
                      type="date"
                      id="dateFilter"
                      value={dateFilter}
                      onChange={handleDateFilterChange}
                      className="w-full border-[#215f47]/20 focus:border-[#215f47] focus:ring-[#215f47]/20 sm:w-[240px]"
                    />
                  </div>
                  
                  <div className="mt-4 flex w-full space-x-2 sm:mt-0 sm:w-auto">
                    <Button 
                      variant="outline" 
                      onClick={clearFilters} 
                      className="border-[#215f47]/30 text-[#215f47] hover:bg-[#215f47]/5"
                    >
                      <FilterX className="mr-2 h-4 w-4" />
                      Clear Filters
                    </Button>
                    <Button 
                      onClick={exportAttendance} 
                      disabled={isExporting} 
                      className="bg-[#215f47] hover:bg-[#215f47]/90"
                    >
                      <FileSpreadsheet className="mr-2 h-4 w-4" />
                      {isExporting ? 'Exporting...' : 'Export CSV'}
                    </Button>
                  </div>
                </div>
              </CardContent>
            </Card>
            
            <Card className="border-[#215f47]/20 shadow-sm">
              <CardHeader className="pb-3">
                <div className="flex items-center justify-between">
                  <div>
                    <CardTitle className="text-lg font-medium text-[#215f47]">
                      <div className="flex items-center">
                        <ClipboardCheck className="mr-2 h-5 w-5" />
                        Attendance Records {selectedSectionId && sections.find(s => s.id === selectedSectionId) ? 
                          `(${sections.find(s => s.id === selectedSectionId)?.course?.courseCode} - ${sections.find(s => s.id === selectedSectionId)?.sectionName || ''})` : 
                        ''}
                      </div>
                    </CardTitle>
                    <CardDescription className="mt-1">
                      Showing {filteredRecords.length} records
                    </CardDescription>
                  </div>
                  <Badge variant="outline" className="bg-[#215f47]/5 text-[#215f47] border-[#215f47]/20">
                    {new Date().toLocaleDateString()}
                  </Badge>
                </div>
              </CardHeader>
              
              <CardContent>
                {isLoading ? (
                  <div className="flex h-40 items-center justify-center">
                    <div className="flex flex-col items-center">
                      <RefreshCw className="h-8 w-8 animate-spin text-[#215f47]/60 mb-3" />
                      <p className="text-gray-500">Loading attendance data...</p>
                    </div>
                  </div>
                ) : filteredRecords.length === 0 ? (
                  <div className="flex h-40 flex-col items-center justify-center">
                    <AlertTriangle className="h-10 w-10 text-amber-500/70 mb-3" />
                    <p className="text-gray-500 text-center">
                      No attendance records found for the selected filters.
                    </p>
                  </div>
                ) : (
                  <div className="rounded-md border border-[#215f47]/10">
                    <Table>
                      <TableHeader className="bg-[#215f47]/5">
                        <TableRow>
                          <TableHead className="font-medium text-[#215f47]">
                            Student ID
                          </TableHead>
                          <TableHead className="font-medium text-[#215f47]">
                            Date
                          </TableHead>
                          <TableHead className="font-medium text-[#215f47]">
                            Time In
                          </TableHead>
                          <TableHead className="font-medium text-[#215f47]">
                            Time Out
                          </TableHead>
                        </TableRow>
                      </TableHeader>
                      <TableBody>
                        {filteredRecords.map((record, index) => (
                          <TableRow key={index} className="hover:bg-[#215f47]/5">
                            <TableCell>
                              <div className="flex items-center">
                                <User className="mr-2 h-4 w-4 text-[#215f47]" />
                                <span className="font-medium">{record.student?.studentPhysicalId || 'Unknown'}</span>
                                {record.student && (
                                  <span className="ml-2 text-xs text-gray-500">
                                    ({record.student.firstName} {record.student.lastName})
                                  </span>
                                )}
                              </div>
                            </TableCell>
                            <TableCell>
                              <div className="flex items-center">
                                <Calendar className="mr-2 h-4 w-4 text-[#215f47]" />
                                {formatDate(record.date)}
                              </div>
                            </TableCell>
                            <TableCell>
                              <div className="flex items-center">
                                <Clock className="mr-2 h-4 w-4 text-[#215f47]" />
                                {formatTime(record.startTime)}
                              </div>
                            </TableCell>
                            <TableCell>
                              <div className="flex items-center">
                                <Clock className="mr-2 h-4 w-4 text-[#215f47]" />
                                {record.endTime ? formatTime(record.endTime) : (
                                  <span className="text-amber-500">Not recorded</span>
                                )}
                              </div>
                            </TableCell>
                          </TableRow>
                        ))}
                      </TableBody>
                    </Table>
                  </div>
                )}
              </CardContent>
            </Card>
          </>
        ) : showAnalytics && analytics ? (
          // Analytics Dashboard View
          <>
            {/* Overview Cards */}
            <div className="grid grid-cols-1 gap-4 sm:grid-cols-3">
              <Card className="border-[#215f47]/20 shadow-sm">
                <CardContent className="pt-6">
                  <div className="flex items-center">
                    <div className="flex h-12 w-12 items-center justify-center rounded-lg bg-[#215f47]/10">
                      <CalendarDays className="h-6 w-6 text-[#215f47]" />
                    </div>
                    <div className="ml-4">
                      <h3 className="text-sm font-medium text-gray-600">Total Sessions</h3>
                      <p className="text-2xl font-semibold text-[#215f47]">{analytics?.totalSessions || 0}</p>
                    </div>
                  </div>
                </CardContent>
              </Card>
              
              <Card className="border-[#215f47]/20 shadow-sm">
                <CardContent className="pt-6">
                  <div className="flex items-center">
                    <div className="flex h-12 w-12 items-center justify-center rounded-lg bg-[#215f47]/10">
                      <Users className="h-6 w-6 text-[#215f47]" />
                    </div>
                    <div className="ml-4">
                      <h3 className="text-sm font-medium text-gray-600">Total Students</h3>
                      <p className="text-2xl font-semibold text-[#215f47]">{analytics?.totalStudents || 0}</p>
                    </div>
                  </div>
                </CardContent>
              </Card>
              
              <Card className="border-[#215f47]/20 shadow-sm">
                <CardContent className="pt-6">
                  <div className="flex items-center">
                    <div className="flex h-12 w-12 items-center justify-center rounded-lg bg-[#215f47]/10">
                      <BarChart className="h-6 w-6 text-[#215f47]" />
                    </div>
                    <div className="ml-4">
                      <h3 className="text-sm font-medium text-gray-600">Average Attendance</h3>
                      <p className="text-2xl font-semibold text-[#215f47]">
                        {(analytics?.averageAttendance || 0).toFixed(2)}%
                      </p>
                    </div>
                  </div>
                </CardContent>
              </Card>
            </div>
            
            {/* Attendance by Date Chart */}
            <Card className="border-[#215f47]/20 shadow-sm mt-4">
              <CardHeader className="pb-2">
                <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-3">
                  <CardTitle className="text-lg font-medium text-[#215f47]">
                    <div className="flex items-center">
                      <BarChart className="mr-2 h-5 w-5" />
                      Attendance by Date
                    </div>
                  </CardTitle>
                  <Button 
                    onClick={exportAnalytics} 
                    disabled={isExporting}
                    className="bg-[#215f47] hover:bg-[#215f47]/90"
                  >
                    <FileSpreadsheet className="mr-2 h-4 w-4" />
                    {isExporting ? 'Exporting...' : 'Export Report'}
                  </Button>
                </div>
              </CardHeader>
              
              <CardContent>
                <div className="h-64 pt-4">
                  {(Array.isArray(analytics?.attendanceByDate) && analytics.attendanceByDate.length > 0) ? (
                    <div className="flex h-full items-end space-x-2">
                      {analytics.attendanceByDate.map((item: { date: string; count: number; percentage: number }, index: number) => {
                        // Ensure we have a valid date
                        const dateObj = new Date(item.date);
                        const isValidDate = !isNaN(dateObj.getTime());
                        const displayDate = isValidDate ? dateObj.toLocaleDateString() : item.date;
                        
                        return (
                          <div key={index} className="flex flex-1 flex-col items-center">
                            <div 
                              className="w-full bg-[#215f47]" 
                              style={{ 
                                height: `${Math.max(item.percentage, 4)}%`, // Ensure minimum visibility
                                minHeight: '4px',
                                borderTopLeftRadius: '3px',
                                borderTopRightRadius: '3px'
                              }}
                            ></div>
                            <span className="mt-2 text-xs text-gray-500">{displayDate}</span>
                          </div>
                        );
                      })}
                    </div>
                  ) : (
                    <div className="flex h-full items-center justify-center">
                      <div className="flex flex-col items-center">
                        <CircleAlert className="h-10 w-10 text-amber-500/70 mb-3" />
                        <p className="text-gray-500">No attendance data available to display.</p>
                      </div>
                    </div>
                  )}
                </div>
              </CardContent>
            </Card>
            
            {/* Student Attendance Table */}
            <Card className="border-[#215f47]/20 shadow-sm mt-4">
              <CardHeader className="pb-2">
                <CardTitle className="text-lg font-medium text-[#215f47]">
                  <div className="flex items-center">
                    <User className="mr-2 h-5 w-5" />
                    Student Attendance
                  </div>
                </CardTitle>
                <CardDescription>
                  Detailed attendance statistics for each student
                </CardDescription>
              </CardHeader>
              
              <CardContent>
                {Array.isArray(analytics?.studentAttendance) && analytics.studentAttendance.length > 0 ? (
                  <div className="rounded-md border border-[#215f47]/10">
                    <Table>
                      <TableHeader className="bg-[#215f47]/5">
                        <TableRow>
                          <TableHead className="font-medium text-[#215f47]">
                            Student ID
                          </TableHead>
                          <TableHead className="font-medium text-[#215f47]">
                            Student Name
                          </TableHead>
                          <TableHead className="font-medium text-[#215f47]">
                            Attendance Count
                          </TableHead>
                          <TableHead className="font-medium text-[#215f47]">
                            Attendance Percentage
                          </TableHead>
                        </TableRow>
                      </TableHeader>
                      <TableBody>
                        {analytics.studentAttendance.map((student, index) => (
                          <TableRow key={index} className="hover:bg-[#215f47]/5">
                            <TableCell className="font-medium">
                              {student.studentId}
                            </TableCell>
                            <TableCell>
                              {student.studentName}
                            </TableCell>
                            <TableCell>
                              {student.attendanceCount}
                            </TableCell>
                            <TableCell>
                              <div className="flex items-center">
                                <div className="w-16 bg-gray-200 rounded-full h-2 mr-2">
                                  <div 
                                    className="bg-[#215f47] h-2 rounded-full" 
                                    style={{ width: `${student.attendancePercentage}%` }}
                                  ></div>
                                </div>
                                <span className="text-sm">{student.attendancePercentage.toFixed(2)}%</span>
                              </div>
                            </TableCell>
                          </TableRow>
                        ))}
                      </TableBody>
                    </Table>
                  </div>
                ) : (
                  <div className="flex h-40 flex-col items-center justify-center">
                    <CircleAlert className="h-10 w-10 text-amber-500/70 mb-3" />
                    <p className="text-center text-gray-500">
                      No student attendance data available.
                    </p>
                  </div>
                )}
              </CardContent>
            </Card>
          </>
        ) : showAnalytics ? (
          <Card className="border-[#215f47]/20 shadow-sm">
            <CardContent className="flex flex-col items-center justify-center py-12 text-center">
              {isLoading ? (
                <>
                  <RefreshCw className="h-12 w-12 animate-spin text-[#215f47]/60 mb-4" />
                  <h3 className="text-lg font-medium text-gray-700 mb-1">Loading Analytics</h3>
                  <p className="text-gray-500 max-w-sm">
                    Please wait while we analyze the attendance data...
                  </p>
                </>
              ) : (
                <>
                  <CircleAlert className="h-12 w-12 text-amber-500/70 mb-4" />
                  <h3 className="text-lg font-medium text-gray-700 mb-1">No Analytics Available</h3>
                  <p className="text-gray-500 max-w-sm mb-6">
                    There is no attendance data available for analytics. This could be because there are no attendance records or because the section has not had any class sessions yet.
                  </p>
                  <Button 
                    onClick={() => setShowAnalytics(false)}
                    className="bg-[#215f47] hover:bg-[#215f47]/90"
                  >
                    View Attendance Records
                  </Button>
                </>
              )}
            </CardContent>
          </Card>
        ) : null}
      </div>
    </DashboardLayout>
  );
};

export default AttendanceTracking;
