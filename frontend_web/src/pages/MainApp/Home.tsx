import { useState, useEffect } from 'react'
import { Link } from 'react-router-dom'
import { Button } from '@/components/ui/button'
import { courseApi, Course, Session } from '@/services/api/courses'
import { CalendarClock, Users, BookOpen, Layers, AlertCircle } from 'lucide-react'
import { useToast } from '@/components/ui/use-toast'

const formatDate = (dateString: string) => {
  const date = new Date(dateString)
  return date.toLocaleDateString('en-US', {
    weekday: 'short',
    month: 'short',
    day: 'numeric',
    hour: 'numeric',
    minute: '2-digit'
  })
}

const formatTime = (timeString: string) => {
  const date = new Date(timeString)
  return date.toLocaleTimeString('en-US', {
    hour: 'numeric',
    minute: '2-digit'
  })
}

export default function HomePage() {
  const { toast } = useToast()
  const [courses, setCourses] = useState<Course[]>([])
  const [upcomingSessions, setUpcomingSessions] = useState<Session[]>([])
  const [loading, setLoading] = useState<{courses: boolean, sessions: boolean}>({ 
    courses: true,
    sessions: true
  })
  const [error, setError] = useState<{courses?: string, sessions?: string}>({})

  // Get user data from local storage
  const userString = localStorage.getItem('user')
  let user = null
  try {
    user = userString && userString !== 'undefined' ? JSON.parse(userString) : null
  } catch (error) {
    console.error('Error parsing user data:', error)
    // If there's an error parsing, remove the invalid data
    localStorage.removeItem('user')
  }
  const userRole = user?.role || 'UNKNOWN'

  // Fetch courses and upcoming sessions
  useEffect(() => {
    const fetchData = async () => {
      try {
        setLoading(prev => ({ ...prev, courses: true }))
        const coursesResponse = await courseApi.getCourses()
        setCourses(coursesResponse.data)
      } catch (err: any) {
        console.error('Error fetching courses:', err)
        setError(prev => ({ ...prev, courses: err.message || 'Failed to load courses' }))
        toast({
          title: 'Error',
          description: 'Could not load your courses. Please try again.',
          variant: 'destructive'
        })
      } finally {
        setLoading(prev => ({ ...prev, courses: false }))
      }

      try {
        setLoading(prev => ({ ...prev, sessions: true }))
        let sessionsResponse
        
        if (userRole === 'STUDENT') {
          sessionsResponse = await courseApi.getActiveSessionsForStudent()
        } else {
          sessionsResponse = await courseApi.getUpcomingSessions()
        }
        
        setUpcomingSessions(sessionsResponse.data)
      } catch (err: any) {
        console.error('Error fetching sessions:', err)
        setError(prev => ({ ...prev, sessions: err.message || 'Failed to load sessions' }))
        toast({
          title: 'Error',
          description: 'Could not load your sessions. Please try again.',
          variant: 'destructive'
        })
      } finally {
        setLoading(prev => ({ ...prev, sessions: false }))
      }
    }

    fetchData()
  }, [])

  return (
    <div>
      {/* Welcome Section */}
      <div className="mb-8">
        <h1 className="text-3xl font-bold text-gray-900">Welcome back, {user?.firstName || 'User'}!</h1>
        <p className="mt-2 text-gray-600">
          {userRole === 'TEACHER' 
            ? 'Manage your classes and track student attendance.'
            : 'View your upcoming classes and attendance records.'}
        </p>
      </div>

      {/* Stats Cards */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-8">
        <div className="bg-white rounded-xl shadow-sm p-6 flex items-start space-x-4">
          <div className="p-3 rounded-lg bg-blue-50">
            <BookOpen className="h-6 w-6 text-blue-500" />
          </div>
          <div>
            <p className="text-sm font-medium text-gray-600">Total Courses</p>
            <p className="text-2xl font-bold text-gray-900">{loading.courses ? '...' : courses.length}</p>
          </div>
        </div>

        <div className="bg-white rounded-xl shadow-sm p-6 flex items-start space-x-4">
          <div className="p-3 rounded-lg bg-indigo-50">
            <Users className="h-6 w-6 text-indigo-500" />
          </div>
          <div>
            <p className="text-sm font-medium text-gray-600">Total Students</p>
            <p className="text-2xl font-bold text-gray-900">
              {loading.courses ? '...' : courses.reduce((acc, course) => acc + (course.students?.length || 0), 0)}
            </p>
          </div>
        </div>

        <div className="bg-white rounded-xl shadow-sm p-6 flex items-start space-x-4">
          <div className="p-3 rounded-lg bg-emerald-50">
            <CalendarClock className="h-6 w-6 text-emerald-500" />
          </div>
          <div>
            <p className="text-sm font-medium text-gray-600">Upcoming Sessions</p>
            <p className="text-2xl font-bold text-gray-900">{loading.sessions ? '...' : upcomingSessions.length}</p>
          </div>
        </div>

        <div className="bg-white rounded-xl shadow-sm p-6 flex items-start space-x-4">
          <div className="p-3 rounded-lg bg-amber-50">
            <Layers className="h-6 w-6 text-amber-500" />
          </div>
          <div>
            <p className="text-sm font-medium text-gray-600">Seat Plans</p>
            <p className="text-2xl font-bold text-gray-900">
              {loading.courses ? '...' : '5'} {/* This would come from actual API */}
            </p>
          </div>
        </div>
      </div>

      {/* Upcoming Sessions */}
      <h2 className="text-xl font-bold text-gray-900 mb-4">
        {userRole === 'STUDENT' ? 'Active Sessions' : 'Upcoming Sessions'}
      </h2>
      
      {loading.sessions ? (
        <div className="bg-white rounded-xl shadow-sm p-6 flex justify-center">
          <div className="animate-spin rounded-full h-8 w-8 border-t-2 border-b-2 border-primary"></div>
        </div>
      ) : error.sessions ? (
        <div className="bg-white rounded-xl shadow-sm p-6 flex items-center justify-center text-red-500">
          <AlertCircle className="mr-2 h-5 w-5" />
          {error.sessions}
        </div>
      ) : upcomingSessions.length === 0 ? (
        <div className="bg-white rounded-xl shadow-sm p-6 text-center text-gray-500">
          <p>
            {userRole === 'STUDENT' 
              ? 'No active sessions right now. Check back later!'
              : 'No upcoming sessions scheduled.'}
          </p>
        </div>
      ) : (
        <div className="bg-white rounded-xl shadow-sm overflow-hidden">
          <div className="overflow-x-auto">
            <table className="min-w-full divide-y divide-gray-200">
              <thead className="bg-gray-50">
                <tr>
                  <th scope="col" className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Course
                  </th>
                  <th scope="col" className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Start Time
                  </th>
                  <th scope="col" className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    End Time
                  </th>
                  <th scope="col" className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Status
                  </th>
                  <th scope="col" className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Actions
                  </th>
                </tr>
              </thead>
              <tbody className="bg-white divide-y divide-gray-200">
                {upcomingSessions.map((session) => (
                  <tr key={session.id}>
                    <td className="px-6 py-4 whitespace-nowrap">
                      <div className="text-sm font-medium text-gray-900">{session.course?.courseCode}</div>
                      <div className="text-sm text-gray-500">{session.course?.name}</div>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap">
                      <div className="text-sm text-gray-900">{formatDate(session.startTime)}</div>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap">
                      <div className="text-sm text-gray-900">{formatTime(session.endTime)}</div>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap">
                      <span className={
                        `px-2 inline-flex text-xs leading-5 font-semibold rounded-full ${
                          session.status === 'ACTIVE' ? 'bg-green-100 text-green-800' :
                          session.status === 'SCHEDULED' ? 'bg-blue-100 text-blue-800' :
                          session.status === 'COMPLETED' ? 'bg-gray-100 text-gray-800' :
                          'bg-red-100 text-red-800'
                        }`
                      }>
                        {session.status}
                      </span>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                      {userRole === 'TEACHER' ? (
                        session.status === 'SCHEDULED' ? (
                          <Link to={`/qrgeneration?sessionId=${session.id}`} className="text-primary hover:text-primary/80 font-medium">
                            Start Session
                          </Link>
                        ) : (
                          <Link to={`/attendance?sessionId=${session.id}`} className="text-primary hover:text-primary/80 font-medium">
                            View Attendance
                          </Link>
                        )
                      ) : (
                        <Link to={`/attendance?sessionId=${session.id}`} className="text-primary hover:text-primary/80 font-medium">
                          Check In
                        </Link>
                      )}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      )}

      {/* Courses */}
      <h2 className="text-xl font-bold text-gray-900 mb-4 mt-8">Your Courses</h2>
      
      {loading.courses ? (
        <div className="bg-white rounded-xl shadow-sm p-6 flex justify-center">
          <div className="animate-spin rounded-full h-8 w-8 border-t-2 border-b-2 border-primary"></div>
        </div>
      ) : error.courses ? (
        <div className="bg-white rounded-xl shadow-sm p-6 flex items-center justify-center text-red-500">
          <AlertCircle className="mr-2 h-5 w-5" />
          {error.courses}
        </div>
      ) : courses.length === 0 ? (
        <div className="bg-white rounded-xl shadow-sm p-6 text-center text-gray-500">
          <p>You don't have any courses yet.</p>
          {userRole === 'TEACHER' && (
            <Button className="mt-4">
              Create a Course
            </Button>
          )}
        </div>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {courses.map((course) => (
            <div key={course.id} className="bg-white overflow-hidden shadow-sm rounded-xl transition-all hover:shadow-md">
              <div className="p-6">
                <div className="flex justify-between items-start">
                  <div>
                    <h3 className="text-lg font-bold text-gray-900">{course.courseCode}</h3>
                    <p className="text-gray-600 mt-1">{course.name}</p>
                  </div>
                  <div className="bg-blue-100 text-blue-800 text-xs px-2 py-1 rounded-full">
                    {course.students?.length || 0} Students
                  </div>
                </div>
                <div className="mt-4 space-y-2">
                  {course.schedule && (
                    <div className="flex items-center text-sm">
                      <CalendarClock className="mr-2 h-4 w-4 text-gray-400" />
                      <span>{course.schedule}</span>
                    </div>
                  )}
                  {course.room && (
                    <div className="flex items-center text-sm">
                      <Layers className="mr-2 h-4 w-4 text-gray-400" />
                      <span>Room {course.room}</span>
                    </div>
                  )}
                </div>
                <div className="mt-5 flex justify-end">
                  <Link to={`/manageseats?courseId=${course.id}`} className="text-sm text-primary hover:text-primary/80 font-medium">
                    Manage Seats
                  </Link>
                </div>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  )
}
