import { useState, useEffect, useMemo } from 'react';
import DashboardLayout from '../../components/ui/layout/DashboardLayout';
import { useAuth } from '../../contexts/AuthContext';
import { sectionApi, type Section } from '../../lib/api/section';
import { attendanceApi, type Attendance, type AttendanceAnalytics } from '../../lib/api/attendance';
import { studentApi, type Student } from '../../lib/api/student';
import { scheduleApi, type Schedule } from '../../lib/api/schedule';
import { Button } from '../../components/ui/button';
import { Calendar, Clock, User, Download, Users, BarChart } from 'lucide-react';

const AttendanceTracking = () => {
  const { user } = useAuth();
  const [sections, setSections] = useState<Section[]>([]);
  const [selectedSectionId, setSelectedSectionId] = useState<number | null>(null);
  const [attendanceRecords, setAttendanceRecords] = useState<Attendance[]>([]);
  const [students, setStudents] = useState<Student[]>([]);
  const [schedules, setSchedules] = useState<Schedule[]>([]);
  const [analytics, setAnalytics] = useState<AttendanceAnalytics | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [isExporting, setIsExporting] = useState(false);
  const [error, setError] = useState<string | null>(null);
  
  // Filters
  const [dateFilter, setDateFilter] = useState<string>('');
  
  // Analytics view state
  const [showAnalytics, setShowAnalytics] = useState<boolean>(false);

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
  
  const toggleView = () => {
    setShowAnalytics(!showAnalytics);
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
      <div className="space-y-6">
        <div className="flex flex-col justify-between sm:flex-row sm:items-center">
          <h2 className="text-xl font-semibold">Attendance Tracking</h2>
          
          {sections.length > 0 && (
            <div className="mt-4 sm:mt-0">
              <select
                value={selectedSectionId || ''}
                onChange={handleSectionChange}
                className="rounded-md border border-gray-300 bg-white px-3 py-2 text-sm shadow-sm focus:border-blue-500 focus:outline-none focus:ring-blue-500"
              >
                {sections.map(section => (
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
            <p className="text-center text-gray-500">
              You have not been assigned to any sections yet.
            </p>
          </div>
        ) : (
          <div className="mb-4 flex space-x-2">
            <Button
              onClick={toggleView}
              className={`flex items-center ${showAnalytics ? '' : 'bg-blue-500 text-white hover:bg-blue-600'}`}
              variant={showAnalytics ? 'outline' : 'default'}
            >
              <User className="mr-1 h-4 w-4" />
              Attendance Records
            </Button>
            <Button
              onClick={toggleView}
              className={`flex items-center ${!showAnalytics ? '' : 'bg-blue-500 text-white hover:bg-blue-600'}`}
              variant={!showAnalytics ? 'outline' : 'default'}
            >
              <BarChart className="mr-1 h-4 w-4" />
              Analytics Dashboard
            </Button>
          </div>
        )}
        
        {sections.length === 0 ? null : !showAnalytics ? (
          <>
            <div className="rounded-lg bg-white p-6 shadow">
              <h3 className="mb-4 text-lg font-medium">Filters</h3>
              
              <div className="flex flex-wrap items-end gap-4">
                <div>
                  <label htmlFor="dateFilter" className="block text-sm font-medium text-gray-700">
                    Date
                  </label>
                  <input
                    type="date"
                    id="dateFilter"
                    value={dateFilter}
                    onChange={handleDateFilterChange}
                    className="mt-1 block w-full rounded-md border border-gray-300 px-3 py-2 shadow-sm focus:border-blue-500 focus:outline-none focus:ring-blue-500 sm:text-sm"
                  />
                </div>
                
                <div className="flex space-x-2">
                  <Button variant="outline" onClick={clearFilters} className="flex-shrink-0">
                    Clear Filters
                  </Button>
                  <Button onClick={exportAttendance} disabled={isExporting} className="flex items-center flex-shrink-0">
                    <Download className="mr-1 h-4 w-4" />
                    {isExporting ? 'Exporting...' : 'Export CSV'}
                  </Button>
                </div>
              </div>
            </div>
            
            <div className="overflow-hidden rounded-lg bg-white shadow">
              <div className="px-4 py-5 sm:px-6">
                <h3 className="text-lg font-medium leading-6 text-gray-900">
                  Attendance Records {selectedSectionId && `for Section ${selectedSectionId}`}
                </h3>
                <p className="mt-1 max-w-2xl text-sm text-gray-500">
                  Showing {filteredRecords.length} records
                </p>
              </div>
              
              {isLoading ? (
                <div className="flex h-40 items-center justify-center">
                  <p className="text-gray-500">Loading attendance data...</p>
                </div>
              ) : filteredRecords.length === 0 ? (
                <div className="border-t border-gray-200 px-4 py-5 sm:px-6">
                  <p className="text-center text-gray-500 mb-2">
                    No attendance records found for the selected filters.
                  </p>
                </div>
              ) : (
                <div className="overflow-x-auto">
                  <table className="min-w-full divide-y divide-gray-200">
                    <thead className="bg-gray-50">
                      <tr>
                        <th className="px-6 py-3 text-left text-xs font-medium uppercase tracking-wider text-gray-500">
                          Student ID
                        </th>
                        <th className="px-6 py-3 text-left text-xs font-medium uppercase tracking-wider text-gray-500">
                          Date
                        </th>
                        <th className="px-6 py-3 text-left text-xs font-medium uppercase tracking-wider text-gray-500">
                          Time In
                        </th>
                        <th className="px-6 py-3 text-left text-xs font-medium uppercase tracking-wider text-gray-500">
                          Time Out
                        </th>
                      </tr>
                    </thead>
                    <tbody className="divide-y divide-gray-200 bg-white">
                      {filteredRecords.map((record, index) => (
                        <tr key={index}>
                          <td className="whitespace-nowrap px-6 py-4 text-sm font-medium text-gray-900">
                            <div className="flex items-center">
                              <User className="mr-2 h-4 w-4 text-gray-400" />
                              {record.student?.studentPhysicalId || 'Unknown'}
                              {record.student && (
                                <span className="ml-2 text-gray-500 text-xs">
                                  ({record.student.firstName} {record.student.lastName})
                                </span>
                              )}
                            </div>
                          </td>
                          <td className="whitespace-nowrap px-6 py-4 text-sm text-gray-500">
                            <div className="flex items-center">
                              <Calendar className="mr-2 h-4 w-4 text-gray-400" />
                              {formatDate(record.date)}
                            </div>
                          </td>
                          <td className="whitespace-nowrap px-6 py-4 text-sm text-gray-500">
                            <div className="flex items-center">
                              <Clock className="mr-2 h-4 w-4 text-gray-400" />
                              {formatTime(record.startTime)}
                            </div>
                          </td>
                          <td className="whitespace-nowrap px-6 py-4 text-sm text-gray-500">
                            <div className="flex items-center">
                              <Clock className="mr-2 h-4 w-4 text-gray-400" />
                              {record.endTime ? formatTime(record.endTime) : 'Not recorded'}
                            </div>
                          </td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
              )}
            </div>
          </> 
        ) : showAnalytics && analytics ? (
          // Analytics Dashboard View
          <>
            {/* Overview Cards */}
            <div className="grid grid-cols-1 gap-4 sm:grid-cols-3">
              <div className="rounded-lg bg-white p-6 shadow">
                <div className="flex items-center">
                  <div className="flex h-12 w-12 items-center justify-center rounded-lg bg-blue-100">
                    <Calendar className="h-6 w-6 text-blue-600" />
                  </div>
                  <div className="ml-4">
                    <h3 className="text-sm font-medium text-gray-500">Total Sessions</h3>
                    <p className="text-2xl font-semibold text-gray-900">{analytics?.totalSessions || 0}</p>
                  </div>
                </div>
              </div>
              
              <div className="rounded-lg bg-white p-6 shadow">
                <div className="flex items-center">
                  <div className="flex h-12 w-12 items-center justify-center rounded-lg bg-green-100">
                    <Users className="h-6 w-6 text-green-600" />
                  </div>
                  <div className="ml-4">
                    <h3 className="text-sm font-medium text-gray-500">Total Students</h3>
                    <p className="text-2xl font-semibold text-gray-900">{analytics?.totalStudents || 0}</p>
                  </div>
                </div>
              </div>
              
              <div className="rounded-lg bg-white p-6 shadow">
                <div className="flex items-center">
                  <div className="flex h-12 w-12 items-center justify-center rounded-lg bg-purple-100">
                    <BarChart className="h-6 w-6 text-purple-600" />
                  </div>
                  <div className="ml-4">
                    <h3 className="text-sm font-medium text-gray-500">Average Attendance</h3>
                    <p className="text-2xl font-semibold text-gray-900">
                      {(analytics?.averageAttendance || 0).toFixed(2)}%
                    </p>
                  </div>
                </div>
              </div>
            </div>
            
            {/* Attendance by Date Chart */}
            <div className="rounded-lg bg-white p-6 shadow mt-4">
              <div className="flex justify-between items-center mb-4">
                <h3 className="text-lg font-medium">Attendance by Date</h3>
                <Button onClick={exportAnalytics} disabled={isExporting} className="flex items-center">
                  <Download className="mr-1 h-4 w-4" />
                  {isExporting ? 'Exporting...' : 'Export Report'}
                </Button>
              </div>
              
              <div className="h-64">
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
                            className="w-full bg-blue-500" 
                            style={{ 
                              height: `${Math.max(item.percentage, 4)}%`, // Ensure minimum visibility
                              minHeight: '4px'
                            }}
                          ></div>
                          <span className="mt-2 text-xs text-gray-500">{displayDate}</span>
                        </div>
                      );
                    })}
                  </div>
                ) : (
                  <div className="flex h-full items-center justify-center">
                    <p className="text-gray-500">No attendance data available to display.</p>
                  </div>
                )}
              </div>
            </div>
            
            {/* Student Attendance Table */}
            <div className="overflow-hidden rounded-lg bg-white shadow mt-4">
              <div className="px-4 py-5 sm:px-6">
                <h3 className="text-lg font-medium leading-6 text-gray-900">Student Attendance</h3>
                <p className="mt-1 max-w-2xl text-sm text-gray-500">
                  Detailed attendance statistics for each student
                </p>
              </div>
              
              {Array.isArray(analytics?.studentAttendance) && analytics.studentAttendance.length > 0 ? (
                <div className="overflow-x-auto">
                  <table className="min-w-full divide-y divide-gray-200">
                    <thead className="bg-gray-50">
                      <tr>
                        <th className="px-6 py-3 text-left text-xs font-medium uppercase tracking-wider text-gray-500">
                          Student ID
                        </th>
                        <th className="px-6 py-3 text-left text-xs font-medium uppercase tracking-wider text-gray-500">
                          Student Name
                        </th>
                        <th className="px-6 py-3 text-left text-xs font-medium uppercase tracking-wider text-gray-500">
                          Attendance Count
                        </th>
                        <th className="px-6 py-3 text-left text-xs font-medium uppercase tracking-wider text-gray-500">
                          Attendance Percentage
                        </th>
                      </tr>
                    </thead>
                    <tbody className="divide-y divide-gray-200 bg-white">
                      {analytics.studentAttendance.map((student, index) => (
                        <tr key={index}>
                          <td className="whitespace-nowrap px-6 py-4 text-sm font-medium text-gray-900">
                            {student.studentId}
                          </td>
                          <td className="whitespace-nowrap px-6 py-4 text-sm text-gray-500">
                            {student.studentName}
                          </td>
                          <td className="whitespace-nowrap px-6 py-4 text-sm text-gray-500">
                            {student.attendanceCount}
                          </td>
                          <td className="whitespace-nowrap px-6 py-4 text-sm text-gray-500">
                            <div className="flex items-center">
                              <div className="w-16 bg-gray-200 rounded-full h-2.5 mr-2">
                                <div 
                                  className="bg-blue-600 h-2.5 rounded-full" 
                                  style={{ width: `${student.attendancePercentage}%` }}
                                ></div>
                              </div>
                              {student.attendancePercentage.toFixed(2)}%
                            </div>
                          </td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
              ) : (
                <div className="border-t border-gray-200 px-4 py-5 sm:px-6">
                  <p className="text-center text-gray-500">
                    No student attendance data available.
                  </p>
                </div>
              )}
            </div>
          </>
        ) : showAnalytics ? (
          <div className="rounded-lg bg-white p-6 shadow">
            <div className="flex h-40 items-center justify-center">
              <p className="text-gray-500">{isLoading ? 'Loading analytics data...' : 'No analytics data available.'}</p>
            </div>
          </div>
        ) : null}
      </div>
    </DashboardLayout>
  );
};

export default AttendanceTracking;
