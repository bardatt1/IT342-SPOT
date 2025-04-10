import { useState, useEffect } from 'react'
import { Button } from '@/components/ui/button'
import { useToast } from '@/components/ui/use-toast'

// Import cn utility for class name merging
import { cn } from '@/lib/utils'
import { sessionApi, SessionAttendance } from '@/services/api/sessions'
import { Session } from '@/services/api/courses'
import { useLocation, useNavigate } from 'react-router-dom'
import { Loader2, AlertCircle, QrCode, Check, X, Clock, Search } from 'lucide-react'

// Define all our types at the top of the file
interface AttendanceRecord {
  id: number
  studentId: number
  studentName: string
  status: 'PRESENT' | 'ABSENT' | 'LATE' | 'UNMARKED'
  checkInTime?: string
  seat?: {
    id: number
    row: number
    column: number
    label: string
  }
}

export default function AttendancePage() {
  const { toast } = useToast()
  const navigate = useNavigate()
  const location = useLocation()
  const searchParams = new URLSearchParams(location.search)
  const sessionId = searchParams.get('sessionId')

  // States
  const [attendanceRecords, setAttendanceRecords] = useState<AttendanceRecord[]>([])
  const [currentSession, setCurrentSession] = useState<Session | null>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [searchText, setSearchText] = useState('')
  const [isSubmitting, setIsSubmitting] = useState(false)
  const [isTeacher, setIsTeacher] = useState(false)
  const [qrCode, setQrCode] = useState('')

  // Get user data from local storage
  const userString = localStorage.getItem('user')
  const user = userString ? JSON.parse(userString) : null
  
  useEffect(() => {
    // Check if user is a teacher
    setIsTeacher(user?.role === 'TEACHER')

    const fetchData = async () => {
      if (!sessionId) {
        setLoading(false)
        return
      }

      try {
        setLoading(true)
        
        // Get session details
        const sessionResponse = await sessionApi.getSession(parseInt(sessionId))
        setCurrentSession(sessionResponse.data)
        
        // Get attendance records for this session
        try {
          const attendanceResponse = await sessionApi.getSessionAttendance(parseInt(sessionId))
          
          // Transform the data
          const records: AttendanceRecord[] = attendanceResponse.data.map((record: SessionAttendance) => ({
            id: record.id,
            studentId: record.studentId,
            studentName: record.studentName,
            status: record.status || 'UNMARKED',
            checkInTime: record.checkInTime,
            seat: record.seat
          }))
          
          setAttendanceRecords(records)
        } catch (attendanceErr) {
          console.warn('Could not fetch attendance data. Using empty records for now.')
          // If attendance records don't exist yet, show empty state but don't show error
          setAttendanceRecords([])
        }
      } catch (err: any) {
        console.error('Error fetching attendance data:', err)
        setError(err.message || 'Failed to load attendance data')
        toast({
          title: 'Error',
          description: 'Could not load attendance data. Please try again.',
          variant: 'destructive'
        })
      } finally {
        setLoading(false)
      }
    }

    fetchData()
  }, [sessionId, toast, user])

  const handleStatusChange = async (recordId: number, status: 'PRESENT' | 'ABSENT' | 'LATE') => {
    try {
      setIsSubmitting(true)
      
      // Update locally first for immediate feedback
      setAttendanceRecords(prev => 
        prev.map(record => 
          record.id === recordId ? { ...record, status } : record
        )
      )
      
      // Update on the backend if the API is available  
      try {
        await sessionApi.updateAttendanceStatus(recordId, status)
        
        toast({
          title: 'Success',
          description: 'Attendance status updated successfully.',
        })
      } catch (apiErr) {
        console.warn('API call failed, but UI was updated:', apiErr)
        // Keep the local update even if API fails - this allows offline functionality
        toast({
          title: 'Warning',
          description: 'Changes saved locally but not synced to server.'
          // Using default variant since 'warning' is not available
        })
      }
    } catch (err: any) {
      toast({
        title: 'Error',
        description: err.message || 'Failed to update attendance status.',
        variant: 'destructive'
      })
      
      // Revert the change locally
      const originalRecord = attendanceRecords.find(r => r.id === recordId)
      if (originalRecord) {
        setAttendanceRecords(prev => 
          prev.map(record => 
            record.id === recordId ? originalRecord : record
          )
        )
      }
    } finally {
      setIsSubmitting(false)
    }
  }

  const handleMarkAllPresent = async () => {
    if (!sessionId) return
    
    try {
      setIsSubmitting(true)
      
      // Update locally first
      setAttendanceRecords(prev => 
        prev.map(record => ({ ...record, status: 'PRESENT' }))
      )
      
      // Update on backend when API is available
      try {
        await sessionApi.markAllPresent(parseInt(sessionId))
        
        toast({
          title: 'Success',
          description: 'All students marked as present.',
        })
      } catch (apiErr) {
        console.warn('API call failed, but UI was updated:', apiErr)
        // Keep the local update even if API fails
        toast({
          title: 'Warning',
          description: 'Changes saved locally but not synced to server.'
          // Using default variant since 'warning' is not available
        })
      }
    } catch (err: any) {
      toast({
        title: 'Error',
        description: err.message || 'Failed to update attendance status.',
        variant: 'destructive'
      })
    } finally {
      setIsSubmitting(false)
    }
  }

  const handleMarkAttendance = async () => {
    if (!sessionId) return
    
    try {
      setIsSubmitting(true)
      
      // For now, show a dialog to enter QR code manually
      // In the future this would use the camera to scan
      const qrCodeInput = prompt('Enter QR code:');
      
      if (!qrCodeInput) {
        setIsSubmitting(false);
        return;
      }
      
      // Process the check-in
      await sessionApi.markAttendance(parseInt(sessionId), qrCodeInput)
      
      toast({
        title: 'Success',
        description: 'Attendance marked successfully!',
      })
      
      // Refresh attendance data
      try {
        const attendanceResponse = await sessionApi.getSessionAttendance(parseInt(sessionId))
        const records: AttendanceRecord[] = attendanceResponse.data.map((record: SessionAttendance) => ({
          id: record.id,
          studentId: record.studentId,
          studentName: record.studentName,
          status: record.status || 'UNMARKED',
          checkInTime: record.checkInTime,
          seat: record.seat
        }))
        
        setAttendanceRecords(records)
      } catch (refreshErr) {
        console.error('Failed to refresh attendance data', refreshErr)
        // Don't show toast here as the check-in was successful
      }
    } catch (err: any) {
      toast({
        title: 'Error',
        description: err.message || 'Failed to mark attendance.',
        variant: 'destructive'
      })
    } finally {
      setIsSubmitting(false)
    }
  }

  // QR code scanning is directly handled in handleMarkAttendance
  // In the future, this could be enhanced with a camera-based scanner

  const filteredAttendanceRecords = searchText ? 
    attendanceRecords.filter(record => 
      record.studentName.toLowerCase().includes(searchText.toLowerCase())
    ) : 
    attendanceRecords

  const getStatusColor = (status: string) => {
    switch (status) {
      case 'PRESENT':
        return 'bg-green-100 text-green-800'
      case 'ABSENT':
        return 'bg-red-100 text-red-800'
      case 'LATE':
        return 'bg-yellow-100 text-yellow-800'
      default:
        return 'bg-gray-100 text-gray-800'
    }
  }

  return (
    <div>
      <div className="mb-8">
        <h1 className="text-3xl font-bold text-gray-900">Attendance Tracking</h1>
        {currentSession && (
          <p className="mt-2 text-gray-600">
            {currentSession.course?.courseCode} - {currentSession.course?.name}
          </p>
        )}
      </div>

      {loading ? (
        <div className="bg-white rounded-xl shadow-sm p-6 flex justify-center items-center min-h-[300px]">
          <div className="flex flex-col items-center">
            <Loader2 className="h-8 w-8 animate-spin text-primary mb-2" />
            <span className="text-gray-500">Loading attendance data...</span>
          </div>
        </div>
      ) : error ? (
        <div className="bg-white rounded-xl shadow-sm p-6 flex items-center justify-center min-h-[300px] text-red-500">
          <AlertCircle className="mr-2 h-5 w-5" />
          <span>{error}</span>
        </div>
      ) : !sessionId ? (
        <div className="bg-white rounded-xl shadow-sm p-6 flex flex-col items-center justify-center min-h-[300px] text-gray-500">
          <AlertCircle className="mb-4 h-10 w-10 text-amber-500" />
          <h3 className="text-lg font-medium text-gray-700 mb-1">No Session Selected</h3>
          <p className="mb-4">Please select a session to view attendance data.</p>
          <Button onClick={() => navigate('/home')}>Go to Dashboard</Button>
        </div>
      ) : (
        <div className="space-y-6">
          {/* Actions Bar */}
          <div className="bg-white rounded-xl shadow-sm p-4">
            <div className="flex flex-col md:flex-row md:justify-between md:items-center gap-4">
              {/* Search */}
              <div className="relative">
                <div className="absolute inset-y-0 left-0 flex items-center pl-3 pointer-events-none">
                  <Search className="h-4 w-4 text-gray-400" />
                </div>
                <input
                  type="text"
                  placeholder="Search students..."
                  value={searchText}
                  onChange={(e: React.ChangeEvent<HTMLInputElement>) => setSearchText(e.target.value)}
                  className={cn(
                    "pl-10 w-full md:w-64 flex h-10 rounded-md border px-3 py-2 text-sm",
                    "bg-white focus:outline-none focus:ring-2 focus:ring-primary focus:border-transparent",
                    "file:border-0 file:bg-transparent file:text-sm file:font-medium",
                    "placeholder:text-gray-500 disabled:cursor-not-allowed disabled:opacity-50"
                  )}
                />
              </div>

              {/* Action Buttons */}
              <div className="flex flex-wrap gap-2">
                {isTeacher ? (
                  <>
                    <Button
                      variant="outline"
                      onClick={handleMarkAllPresent}
                      disabled={isSubmitting || attendanceRecords.length === 0}
                    >
                      {isSubmitting ? (
                        <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                      ) : (
                        <Check className="mr-2 h-4 w-4" />
                      )}
                      Mark All Present
                    </Button>
                    <Button
                      variant="outline"
                      onClick={() => navigate(`/qrgeneration?sessionId=${sessionId}`)}
                    >
                      <QrCode className="mr-2 h-4 w-4" />
                      QR Code
                    </Button>
                  </>
                ) : (
                  <Button
                    onClick={handleMarkAttendance}
                    disabled={isSubmitting}
                  >
                    {isSubmitting ? (
                      <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                    ) : (
                      <QrCode className="mr-2 h-4 w-4" />
                    )}
                    Mark Attendance
                  </Button>
                )}
              </div>
              
              {/* Status Counts */}
              <div className="flex flex-wrap gap-2">
                <span className="inline-flex items-center px-2.5 py-1 rounded-full text-xs font-medium bg-green-100 text-green-800">
                  Present: {attendanceRecords.filter(r => r.status === 'PRESENT').length}
                </span>
                <span className="inline-flex items-center px-2.5 py-1 rounded-full text-xs font-medium bg-red-100 text-red-800">
                  Absent: {attendanceRecords.filter(r => r.status === 'ABSENT').length}
                </span>
                <span className="inline-flex items-center px-2.5 py-1 rounded-full text-xs font-medium bg-yellow-100 text-yellow-800">
                  Late: {attendanceRecords.filter(r => r.status === 'LATE').length}
                </span>
                <span className="inline-flex items-center px-2.5 py-1 rounded-full text-xs font-medium bg-gray-100 text-gray-800">
                  Unmarked: {attendanceRecords.filter(r => r.status === 'UNMARKED').length}
                </span>
              </div>
            </div>
          </div>

          {/* Attendance Records */}
          <div className="bg-white rounded-xl shadow-sm overflow-hidden">
            {filteredAttendanceRecords.length === 0 ? (
              <div className="p-6 text-center text-gray-500">
                <p>No attendance records found.</p>
              </div>
            ) : (
              <div className="overflow-x-auto">
                <table className="min-w-full divide-y divide-gray-200">
                  <thead className="bg-gray-50">
                    <tr>
                      <th scope="col" className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                        Student Name
                      </th>
                      {isTeacher && (
                        <th scope="col" className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                          Seat
                        </th>
                      )}
                      <th scope="col" className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                        Check-in Time
                      </th>
                      <th scope="col" className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                        Status
                      </th>
                      {isTeacher && (
                        <th scope="col" className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                          Actions
                        </th>
                      )}
                    </tr>
                  </thead>
                  <tbody className="bg-white divide-y divide-gray-200">
                    {filteredAttendanceRecords.map((record) => (
                      <tr key={record.id}>
                        <td className="px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-900">
                          {record.studentName}
                        </td>
                        {isTeacher && (
                          <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                            {record.seat ? record.seat.label : 'Not assigned'}
                          </td>
                        )}
                        <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                          {record.checkInTime ? 
                            new Date(record.checkInTime).toLocaleTimeString('en-US', {
                              hour: '2-digit', 
                              minute: '2-digit',
                              hour12: true
                            }) : 
                            '—'
                          }
                        </td>
                        <td className="px-6 py-4 whitespace-nowrap">
                          <span className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium ${getStatusColor(record.status)}`}>
                            {record.status.charAt(0) + record.status.slice(1).toLowerCase()}
                          </span>
                        </td>
                        {isTeacher && (
                          <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                            <div className="flex space-x-2">
                              <Button
                                size="sm"
                                variant="ghost"
                                className="text-green-600 hover:text-green-900"
                                onClick={() => handleStatusChange(record.id, 'PRESENT')}
                                disabled={isSubmitting || record.status === 'PRESENT'}
                              >
                                <Check className="h-4 w-4" />
                              </Button>
                              <Button
                                size="sm"
                                variant="ghost"
                                className="text-red-600 hover:text-red-900"
                                onClick={() => handleStatusChange(record.id, 'ABSENT')}
                                disabled={isSubmitting || record.status === 'ABSENT'}
                              >
                                <X className="h-4 w-4" />
                              </Button>
                              <Button
                                size="sm"
                                variant="ghost"
                                className="text-yellow-600 hover:text-yellow-900"
                                onClick={() => handleStatusChange(record.id, 'LATE')}
                                disabled={isSubmitting || record.status === 'LATE'}
                              >
                                <Clock className="h-4 w-4" />
                              </Button>
                            </div>
                          </td>
                        )}
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            )}
          </div>

          {/* QR Code Modal (for future implementation) */}
          {qrCode && (
            <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
              <div className="bg-white rounded-lg p-6 max-w-md w-full">
                <h3 className="text-lg font-medium mb-4">Scan QR Code</h3>
                <div className="flex justify-center mb-4">
                  <img src={qrCode} alt="QR Code" className="w-64 h-64" />
                </div>
                <div className="flex justify-end">
                  <Button onClick={() => setQrCode('')}>Close</Button>
                </div>
              </div>
            </div>
          )}
        </div>
      )}
    </div>
  )
}
