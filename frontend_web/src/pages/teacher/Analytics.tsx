// ATTENDANCE TRACKING AND ANALYTICS ARE IN THE SAME PAGE. REFER TO THAT
// 
//  import { useState, useEffect } from 'react';
// import DashboardLayout from '../../components/ui/layout/DashboardLayout';
// import { useAuth } from '../../contexts/AuthContext';
// import { sectionApi, type Section } from '../../lib/api/section';
// import { attendanceApi, type AttendanceAnalytics } from '../../lib/api/attendance';
// import { Button } from '../../components/ui/button';
// import { BarChart, Calendar, Users, Download } from 'lucide-react';

// const Analytics = () => {
//   const { user } = useAuth();
//   const [sections, setSections] = useState<Section[]>([]);
//   const [selectedSectionId, setSelectedSectionId] = useState<number | null>(null);
//   const [analytics, setAnalytics] = useState<AttendanceAnalytics | null>(null);
//   const [isLoading, setIsLoading] = useState(true);
//   const [isExporting, setIsExporting] = useState(false);
//   const [error, setError] = useState<string | null>(null);

//   useEffect(() => {
//     fetchTeacherSections();
//   }, [user?.id]);

//   useEffect(() => {
//     if (selectedSectionId) {
//       fetchAnalyticsData(selectedSectionId);
//     }
//   }, [selectedSectionId]);

//   const fetchTeacherSections = async () => {
//     if (!user?.id) return;
    
//     try {
//       setIsLoading(true);
//       console.log(`Fetching sections for teacher with ID: ${user.id}`);
//       const allSections = await sectionApi.getAllSections();
      
//       // Extract teacher ID properly from the nested teacher object
//       const teacherSections = allSections.filter(section => {
//         // In the backend DTO, teacher is a nested object with id
//         const sectionTeacherId = section.teacher?.id || 
//                                (section.teacher ? section.teacher.id : null);
                               
//         console.log(`Section ${section.id} has teacher:`, section.teacher, 
//                   `- Extracted teacherId:`, sectionTeacherId);
                  
//         return sectionTeacherId === user.id;
//       });
      
//       console.log(`Found ${teacherSections.length} sections for teacher ${user.id}`);
//       setSections(teacherSections);
      
//       // Select first section by default if available
//       if (teacherSections.length > 0 && !selectedSectionId) {
//         setSelectedSectionId(teacherSections[0].id);
//       }
//     } catch (error) {
//       console.error('Error fetching sections:', error);
//       setError('Failed to load your sections. Please try again later.');
//     } finally {
//       setIsLoading(false);
//     }
//   };

//   const fetchAnalyticsData = async (sectionId: number) => {
//     try {
//       setIsLoading(true);
//       console.log(`Fetching analytics data for section ID: ${sectionId}`);
      
//       // First run enhanced diagnostics to check all analytics prerequisites
//       console.log('Running comprehensive analytics diagnostics...');
//       const diagnostics = await attendanceApi.checkSectionAnalyticsData(sectionId);
//       console.log('Comprehensive diagnostic results:', diagnostics);
      
//       if (diagnostics.error) {
//         setError(`Error checking section data: ${diagnostics.message}`);
//       } else if (!diagnostics.analyticsCanBeCalculated) {
//         // Show specific error messages based on what prerequisites are missing
//         const missingItems = diagnostics.missingPrerequisites.join(', ');
//         console.warn(`Analytics prerequisites missing: ${missingItems}`);
        
//         if (!diagnostics.hasSchedules) {
//           setError(
//             `No class schedules defined for this section. Analytics requires schedules to calculate class days. ` +
//             `Please contact the administrator to set up schedules for this section.`
//           );
//         } else if (!diagnostics.hasEnrollments) {
//           setError(
//             `No students enrolled in this section (${sectionId}). ` +
//             `Analytics cannot be calculated until students are enrolled.`
//           );
//         } else {
//           setError(`Analytics cannot be calculated: ${missingItems}`); 
//         }
//       }
      
//       // Fetch analytics data even if prerequisites are missing
//       // This will help us see what the backend is returning
//       const data = await attendanceApi.getSectionAnalytics(sectionId);
//       console.log('Received analytics data:', data);
//       setAnalytics(data);
//     } catch (error) {
//       console.error('Error fetching analytics data:', error);
//       setError('Failed to load analytics data. Please try again later.');
//       setAnalytics(null);
//     } finally {
//       setIsLoading(false);
//     }
//   };

//   const handleSectionChange = (e: React.ChangeEvent<HTMLSelectElement>) => {
//     const sectionId = parseInt(e.target.value);
//     setSelectedSectionId(sectionId);
    
//     // Clear any existing error messages when switching sections
//     setError(null);
//     setAnalytics(null); // Reset analytics data when changing sections
//   };

//   const exportAnalytics = () => {
//     if (!analytics) return;
    
//     setIsExporting(true);
    
//     try {
//       // Convert to CSV
//       const headers = ['Student ID', 'Student Name', 'Attendance Count', 'Attendance Percentage'];
//       const csvContent = [
//         headers.join(','),
//         ...analytics.studentAttendance.map(student => [
//           student.studentId,
//           student.studentName,
//           student.attendanceCount,
//           `${student.attendancePercentage.toFixed(2)}%`
//         ].join(','))
//       ].join('\n');
      
//       // Create and download file
//       const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' });
//       const url = URL.createObjectURL(blob);
//       const link = document.createElement('a');
//       link.setAttribute('href', url);
//       link.setAttribute('download', `analytics_section_${selectedSectionId}_${new Date().toISOString().split('T')[0]}.csv`);
//       link.style.visibility = 'hidden';
//       document.body.appendChild(link);
//       link.click();
//       document.body.removeChild(link);
//     } catch (error) {
//       console.error('Error exporting analytics:', error);
//       setError('Failed to export analytics data. Please try again later.');
//     } finally {
//       setIsExporting(false);
//     }
//   };

//   if (isLoading && sections.length === 0) {
//     return (
//       <DashboardLayout>
//         <div className="flex h-full items-center justify-center">
//           <p className="text-lg">Loading your sections...</p>
//         </div>
//       </DashboardLayout>
//     );
//   }

//   return (
//     <DashboardLayout>
//       <div className="space-y-6">
//         <div className="flex flex-col justify-between sm:flex-row sm:items-center">
//           <h2 className="text-xl font-semibold">Analytics</h2>
          
//           {sections.length > 0 && (
//             <div className="mt-4 flex space-x-2 sm:mt-0">
//               <select
//                 value={selectedSectionId || ''}
//                 onChange={handleSectionChange}
//                 className="rounded-md border border-gray-300 bg-white px-3 py-2 text-sm shadow-sm focus:border-blue-500 focus:outline-none focus:ring-blue-500"
//               >
//                 {sections.map(section => (
//                   <option key={section.id} value={section.id}>
//                     Section ID: {section.id} ({section.sectionName})
//                   </option>
//                 ))}
//               </select>
              
//               {analytics && (
//                 <Button 
//                   onClick={exportAnalytics} 
//                   disabled={isExporting || !analytics}
//                   className="flex items-center"
//                 >
//                   <Download className="mr-1 h-4 w-4" />
//                   {isExporting ? 'Exporting...' : 'Export CSV'}
//                 </Button>
//               )}
//             </div>
//           )}
//         </div>
        
//         {error && (
//           <div className="rounded-md bg-red-50 p-4 relative">
//             <div className="flex">
//               <div className="flex-grow">
//                 <h3 className="text-sm font-medium text-red-800">Error</h3>
//                 <div className="mt-2 text-sm text-red-700">
//                   <p>{error}</p>
//                 </div>
//               </div>
//               <button 
//                 onClick={() => setError(null)} 
//                 className="absolute top-2 right-2 text-red-500 hover:text-red-700"
//                 aria-label="Dismiss error"
//               >
//                 <svg xmlns="http://www.w3.org/2000/svg" className="h-5 w-5" viewBox="0 0 20 20" fill="currentColor">
//                   <path fillRule="evenodd" d="M4.293 4.293a1 1 0 011.414 0L10 8.586l4.293-4.293a1 1 0 111.414 1.414L11.414 10l4.293 4.293a1 1 0 01-1.414 1.414L10 11.414l-4.293 4.293a1 1 0 01-1.414-1.414L8.586 10 4.293 5.707a1 1 0 010-1.414z" clipRule="evenodd" />
//                 </svg>
//               </button>
//             </div>
//           </div>
//         )}
        
//         {sections.length === 0 ? (
//           <div className="rounded-lg bg-white p-6 shadow">
//             <p className="text-center text-gray-500">
//               You have not been assigned to any sections yet.
//             </p>
//           </div>
//         ) : isLoading ? (
//           <div className="rounded-lg bg-white p-6 shadow">
//             <div className="flex h-40 items-center justify-center">
//               <p className="text-gray-500">Loading analytics data...</p>
//             </div>
//           </div>
//         ) : analytics ? (
//           <>
//             {/* Overview Cards */}
//             <div className="grid grid-cols-1 gap-4 sm:grid-cols-3">
//               <div className="rounded-lg bg-white p-6 shadow">
//                 <div className="flex items-center">
//                   <div className="flex h-12 w-12 items-center justify-center rounded-lg bg-blue-100">
//                     <Calendar className="h-6 w-6 text-blue-600" />
//                   </div>
//                   <div className="ml-4">
//                     <h3 className="text-sm font-medium text-gray-500">Total Sessions</h3>
//                     <p className="text-2xl font-semibold text-gray-900">{analytics?.totalSessions || 0}</p>
//                   </div>
//                 </div>
//               </div>
              
//               <div className="rounded-lg bg-white p-6 shadow">
//                 <div className="flex items-center">
//                   <div className="flex h-12 w-12 items-center justify-center rounded-lg bg-green-100">
//                     <Users className="h-6 w-6 text-green-600" />
//                   </div>
//                   <div className="ml-4">
//                     <h3 className="text-sm font-medium text-gray-500">Total Students</h3>
//                     <p className="text-2xl font-semibold text-gray-900">{analytics?.totalStudents || 0}</p>
//                   </div>
//                 </div>
//               </div>
              
//               <div className="rounded-lg bg-white p-6 shadow">
//                 <div className="flex items-center">
//                   <div className="flex h-12 w-12 items-center justify-center rounded-lg bg-purple-100">
//                     <BarChart className="h-6 w-6 text-purple-600" />
//                   </div>
//                   <div className="ml-4">
//                     <h3 className="text-sm font-medium text-gray-500">Average Attendance</h3>
//                     <p className="text-2xl font-semibold text-gray-900">
//                       {(analytics?.averageAttendance || 0).toFixed(2)}%
//                     </p>
//                   </div>
//                 </div>
//               </div>
//             </div>
            
//             {/* Attendance by Date Chart */}
//             <div className="rounded-lg bg-white p-6 shadow">
//               <h3 className="mb-4 text-lg font-medium">Attendance by Date</h3>
              
//               <div className="h-64">
//                 {/* This would ideally be a real chart component */}
//                 <div className="flex h-full items-end space-x-2">
//                   {Array.isArray(analytics?.attendanceByDate) && analytics.attendanceByDate.map((item: { date: string; percentage: number }, index: number) => (
//                     <div key={index} className="flex flex-1 flex-col items-center">
//                       <div 
//                         className="w-full bg-blue-500" 
//                         style={{ 
//                           height: `${item.percentage}%`,
//                           minHeight: '4px'
//                         }}
//                       ></div>
//                       <span className="mt-2 text-xs text-gray-500">{new Date(item.date).toLocaleDateString()}</span>
//                     </div>
//                   ))}
//                 </div>
//               </div>
//             </div>
            
//             {/* Student Attendance Table */}
//             <div className="overflow-hidden rounded-lg bg-white shadow">
//               <div className="px-4 py-5 sm:px-6">
//                 <h3 className="text-lg font-medium leading-6 text-gray-900">Student Attendance</h3>
//                 <p className="mt-1 max-w-2xl text-sm text-gray-500">
//                   Detailed attendance statistics for each student
//                 </p>
//               </div>
              
//               <div className="overflow-x-auto">
//                 <table className="min-w-full divide-y divide-gray-200">
//                   <thead className="bg-gray-50">
//                     <tr>
//                       <th className="px-6 py-3 text-left text-xs font-medium uppercase tracking-wider text-gray-500">
//                         Student ID
//                       </th>
//                       <th className="px-6 py-3 text-left text-xs font-medium uppercase tracking-wider text-gray-500">
//                         Student Name
//                       </th>
//                       <th className="px-6 py-3 text-left text-xs font-medium uppercase tracking-wider text-gray-500">
//                         Attendance Count
//                       </th>
//                       <th className="px-6 py-3 text-left text-xs font-medium uppercase tracking-wider text-gray-500">
//                         Attendance Percentage
//                       </th>
//                     </tr>
//                   </thead>
//                   <tbody className="divide-y divide-gray-200 bg-white">
//                     {Array.isArray(analytics?.studentAttendance) && analytics.studentAttendance.map((student: { studentId: number; studentName: string; attendanceCount: number; attendancePercentage: number }, index: number) => (
//                       <tr key={index}>
//                         <td className="whitespace-nowrap px-6 py-4 text-sm font-medium text-gray-900">
//                           {student.studentId}
//                         </td>
//                         <td className="whitespace-nowrap px-6 py-4 text-sm text-gray-500">
//                           {student.studentName}
//                         </td>
//                         <td className="whitespace-nowrap px-6 py-4 text-sm text-gray-500">
//                           {student.attendanceCount}
//                         </td>
//                         <td className="whitespace-nowrap px-6 py-4 text-sm text-gray-500">
//                           <div className="flex items-center">
//                             <div className="w-16 bg-gray-200 rounded-full h-2.5 mr-2">
//                               <div 
//                                 className="bg-blue-600 h-2.5 rounded-full" 
//                                 style={{ width: `${student.attendancePercentage}%` }}
//                               ></div>
//                             </div>
//                             <span>{student.attendancePercentage.toFixed(2)}%</span>
//                           </div>
//                         </td>
//                       </tr>
//                     ))}
//                   </tbody>
//                 </table>
//               </div>
//             </div>
//           </>
//         ) : (
//           <div className="rounded-lg bg-white p-6 shadow">
//             <p className="text-center text-gray-500">
//               No analytics data available. Please select a section.
//             </p>
//           </div>
//         )}
//       </div>
//     </DashboardLayout>
//   );
// };

// export default Analytics;
