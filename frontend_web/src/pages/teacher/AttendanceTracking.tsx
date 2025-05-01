import { useState, useEffect, useMemo } from 'react';
import DashboardLayout from '../../components/ui/layout/DashboardLayout';
import { useAuth } from '../../contexts/AuthContext';
import { sectionApi, type Section } from '../../lib/api/section';
import { attendanceApi, type Attendance } from '../../lib/api/attendance';
import { Button } from '../../components/ui/button';
import { Calendar, Clock, User, Download } from 'lucide-react';

const AttendanceTracking = () => {
  const { user } = useAuth();
  const [sections, setSections] = useState<Section[]>([]);
  const [selectedSectionId, setSelectedSectionId] = useState<number | null>(null);
  const [attendanceRecords, setAttendanceRecords] = useState<Attendance[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [isExporting, setIsExporting] = useState(false);
  const [error, setError] = useState<string | null>(null);
  
  // Filters
  const [dateFilter, setDateFilter] = useState<string>('');

  useEffect(() => {
    fetchTeacherSections();
  }, [user?.id]);

  useEffect(() => {
    if (selectedSectionId) {
      fetchAttendanceData(selectedSectionId);
    }
  }, [selectedSectionId]);

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

  const fetchAttendanceData = async (sectionId: number) => {
    try {
      setIsLoading(true);
      const response = await attendanceApi.getSectionAttendance(sectionId);
      
      // Log the response to debug
      console.log('Attendance API response:', response);
      
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

  const handleSectionChange = (e: React.ChangeEvent<HTMLSelectElement>) => {
    const sectionId = parseInt(e.target.value);
    setSelectedSectionId(sectionId);
  };

  const handleDateFilterChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    setDateFilter(e.target.value);
  };

  const clearFilters = () => {
    setDateFilter('');
  };

  const exportAttendance = () => {
    setIsExporting(true);
    
    try {
      // Use the same filtered records that are displayed in the UI
      // (this uses the memoized value from useMemo)
      
      // Convert to CSV
      const headers = ['Student ID', 'Section ID', 'Date', 'Start Time', 'End Time'];
      const csvContent = [
        headers.join(','),
        ...filteredRecords.map((record: Attendance) => [
          record.studentId,
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

  // The filterAttendanceRecords function is no longer needed as we use useMemo instead

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
    
    return attendanceRecords.filter(record => 
      record.date === dateFilter
    );
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
                  <p className="text-center text-gray-500">
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
                              {record.studentId}
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
        )}
      </div>
    </DashboardLayout>
  );
};

export default AttendanceTracking;
