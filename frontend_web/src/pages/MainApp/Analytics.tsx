import { useState, useEffect } from 'react'
import { useToast } from '@/components/ui/use-toast'
import { analyticsApi, AttendanceMetrics } from '@/services/api/analytics'
import { Loader2 } from 'lucide-react'

export default function AnalyticsPage() {
  const { toast } = useToast()
  const [loading, setLoading] = useState(true)
  const [metrics, setMetrics] = useState<AttendanceMetrics | null>(null)
  
  useEffect(() => {
    const fetchAnalytics = async () => {
      try {
        setLoading(true)
        const response = await analyticsApi.getAttendanceMetrics()
        setMetrics(response.data)
      } catch (err: any) {
        console.error('Error fetching analytics:', err)
        toast({
          title: 'Error',
          description: 'Could not load analytics data. Using sample data instead.',
          variant: 'destructive'
        })
        
        // Use sample data as fallback
        setMetrics({
          overallAttendanceRate: 92,
          averageLateStudents: 3,
          totalClasses: 12,
          totalStudents: 156,
          attendanceTrend: [
            { date: '2023-01-01', attendanceRate: 90 },
            { date: '2023-01-15', attendanceRate: 92 },
            { date: '2023-02-01', attendanceRate: 94 }
          ],
          classwiseAttendance: [
            { courseCode: 'IT342', courseName: 'Web Development', attendanceRate: 94 },
            { courseCode: 'IT343', courseName: 'Database Systems', attendanceRate: 88 },
            { courseCode: 'IT344', courseName: 'Network Security', attendanceRate: 91 }
          ]
        })
      } finally {
        setLoading(false)
      }
    }
    
    fetchAnalytics()
  }, [toast])
  if (loading) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <div className="flex flex-col items-center space-y-4">
          <Loader2 className="h-12 w-12 animate-spin text-primary" />
          <p className="text-lg text-gray-500">Loading analytics data...</p>
        </div>
      </div>
    )
  }
  
  return (
    <div className="min-h-screen bg-gray-50 py-10">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="mb-8">
          <h1 className="text-3xl font-bold text-gray-900">Attendance Analytics</h1>
          <p className="mt-2 text-sm text-gray-600">
            View attendance trends and insights
          </p>
        </div>

        <div className="grid grid-cols-1 gap-6 sm:grid-cols-2 lg:grid-cols-4">
          {/* Overall Attendance Rate */}
          <div className="bg-white overflow-hidden shadow rounded-lg">
            <div className="p-5">
              <div className="flex items-center">
                <div className="flex-shrink-0">
                  <svg className="h-6 w-6 text-gray-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z" />
                  </svg>
                </div>
                <div className="ml-5 w-0 flex-1">
                  <dl>
                    <dt className="text-sm font-medium text-gray-500 truncate">
                      Overall Attendance Rate
                    </dt>
                    <dd className="flex items-baseline">
                      <div className="text-2xl font-semibold text-gray-900">
                        {metrics?.overallAttendanceRate || 0}%
                      </div>
                      <div className="ml-2 flex items-baseline text-sm font-semibold text-green-600">
                        <svg className="w-3 h-3" fill="currentColor" viewBox="0 0 12 12">
                          <path d="M3 8l3-3 3 3" />
                        </svg>
                        <span className="sr-only">Increased by</span>
                        2%
                      </div>
                    </dd>
                  </dl>
                </div>
              </div>
            </div>
          </div>

          {/* Average Late Students */}
          <div className="bg-white overflow-hidden shadow rounded-lg">
            <div className="p-5">
              <div className="flex items-center">
                <div className="flex-shrink-0">
                  <svg className="h-6 w-6 text-gray-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z" />
                  </svg>
                </div>
                <div className="ml-5 w-0 flex-1">
                  <dl>
                    <dt className="text-sm font-medium text-gray-500 truncate">
                      Average Late Students
                    </dt>
                    <dd className="flex items-baseline">
                      <div className="text-2xl font-semibold text-gray-900">
                        {metrics?.averageLateStudents || 0}
                      </div>
                      <div className="ml-2 flex items-baseline text-sm font-semibold text-red-600">
                        <svg className="w-3 h-3 transform rotate-180" fill="currentColor" viewBox="0 0 12 12">
                          <path d="M3 8l3-3 3 3" />
                        </svg>
                        <span className="sr-only">Decreased by</span>
                        1
                      </div>
                    </dd>
                  </dl>
                </div>
              </div>
            </div>
          </div>

          {/* Total Classes */}
          <div className="bg-white overflow-hidden shadow rounded-lg">
            <div className="p-5">
              <div className="flex items-center">
                <div className="flex-shrink-0">
                  <svg className="h-6 w-6 text-gray-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 11H5m14 0a2 2 0 012 2v6a2 2 0 01-2 2H5a2 2 0 01-2-2v-6a2 2 0 012-2m14 0V9a2 2 0 00-2-2M5 11V9a2 2 0 012-2m0 0V5a2 2 0 012-2h6a2 2 0 012 2v2M7 7h10" />
                  </svg>
                </div>
                <div className="ml-5 w-0 flex-1">
                  <dl>
                    <dt className="text-sm font-medium text-gray-500 truncate">
                      Total Classes
                    </dt>
                    <dd className="text-2xl font-semibold text-gray-900">
                      {metrics?.totalClasses || 0}
                    </dd>
                  </dl>
                </div>
              </div>
            </div>
          </div>

          {/* Total Students */}
          <div className="bg-white overflow-hidden shadow rounded-lg">
            <div className="p-5">
              <div className="flex items-center">
                <div className="flex-shrink-0">
                  <svg className="h-6 w-6 text-gray-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0zm6 3a2 2 0 11-4 0 2 2 0 014 0zM7 10a2 2 0 11-4 0 2 2 0 014 0z" />
                  </svg>
                </div>
                <div className="ml-5 w-0 flex-1">
                  <dl>
                    <dt className="text-sm font-medium text-gray-500 truncate">
                      Total Students
                    </dt>
                    <dd className="text-2xl font-semibold text-gray-900">
                      {metrics?.totalStudents || 0}
                    </dd>
                  </dl>
                </div>
              </div>
            </div>
          </div>
        </div>

        {/* Attendance Trends Chart (Placeholder) */}
        <div className="mt-8 bg-white shadow rounded-lg">
          <div className="px-4 py-5 sm:p-6">
            <h3 className="text-lg leading-6 font-medium text-gray-900">
              Attendance Trends
            </h3>
            <div className="mt-4 aspect-[2/1] bg-gray-50 rounded-lg flex items-center justify-center">
              <p className="text-gray-500">Chart will be implemented here</p>
            </div>
          </div>
        </div>

        {/* Class-wise Attendance */}
        <div className="mt-8 bg-white shadow rounded-lg">
          <div className="px-4 py-5 sm:p-6">
            <h3 className="text-lg leading-6 font-medium text-gray-900">
              Class-wise Attendance
            </h3>
            <div className="mt-4">
              <div className="space-y-4">
                {metrics?.classwiseAttendance?.map((course) => (
                  <div key={course.courseCode} className="bg-gray-50 rounded-lg p-4">
                    <div className="flex items-center justify-between">
                      <h4 className="text-sm font-medium text-gray-900">{course.courseCode}</h4>
                      <span className="text-sm text-gray-500">{course.attendanceRate}% attendance</span>
                    </div>
                    <div className="mt-2 w-full bg-gray-200 rounded-full h-2">
                      <div
                        className="bg-primary h-2 rounded-full"
                        style={{ width: `${course.attendanceRate}%` }}
                      />
                    </div>
                  </div>
                ))}
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  )
}
