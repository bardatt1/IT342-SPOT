import { Link } from 'react-router-dom'
import { Button } from '@/components/ui/button'

const classes = [
  {
    id: 1,
    name: 'IT342 - Systems Integration and Architecture',
    schedule: 'MWF 10:00 AM - 11:30 AM',
    students: 35,
    attendance: '92%'
  },
  {
    id: 2,
    name: 'IT343 - Information Assurance and Security',
    schedule: 'TTH 1:00 PM - 2:30 PM',
    students: 40,
    attendance: '88%'
  },
  // Add more classes as needed
]

export default function HomePage() {
  return (
    <div className="min-h-screen bg-gray-50">
      {/* Navigation */}
      <nav className="bg-white shadow-sm">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex justify-between h-16">
            <div className="flex">
              <div className="flex-shrink-0 flex items-center">
                <span className="text-2xl font-bold text-primary">SPOT</span>
              </div>
              <div className="hidden sm:ml-6 sm:flex sm:space-x-8">
                <Link to="/home" className="border-primary text-gray-900 inline-flex items-center px-1 pt-1 border-b-2 text-sm font-medium">
                  Dashboard
                </Link>
                <Link to="/manageseats" className="border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300 inline-flex items-center px-1 pt-1 border-b-2 text-sm font-medium">
                  Manage Seats
                </Link>
                <Link to="/attendance" className="border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300 inline-flex items-center px-1 pt-1 border-b-2 text-sm font-medium">
                  Attendance
                </Link>
                <Link to="/analytics" className="border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300 inline-flex items-center px-1 pt-1 border-b-2 text-sm font-medium">
                  Analytics
                </Link>
              </div>
            </div>
            <div className="flex items-center">
              <Button variant="ghost">
                <svg xmlns="http://www.w3.org/2000/svg" className="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 17h5l-1.405-1.405A2.032 2.032 0 0118 14.158V11a6.002 6.002 0 00-4-5.659V5a2 2 0 10-4 0v.341C7.67 6.165 6 8.388 6 11v3.159c0 .538-.214 1.055-.595 1.436L4 17h5m6 0v1a3 3 0 11-6 0v-1m6 0H9" />
                </svg>
              </Button>
            </div>
          </div>
        </div>
      </nav>

      {/* Main Content */}
      <main className="py-10">
        <div className="max-w-7xl mx-auto sm:px-6 lg:px-8">
          {/* Welcome Section */}
          <div className="bg-white overflow-hidden shadow rounded-lg mb-8">
            <div className="px-4 py-5 sm:p-6">
              <h1 className="text-2xl font-semibold text-gray-900">Welcome back, Teacher!</h1>
              <p className="mt-1 text-sm text-gray-500">Here's an overview of your classes today</p>
            </div>
          </div>

          {/* Classes Grid */}
          <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-3">
            {classes.map((classItem) => (
              <div
                key={classItem.id}
                className="bg-white overflow-hidden shadow rounded-lg hover:shadow-md transition-shadow duration-300"
              >
                <div className="px-4 py-5 sm:p-6">
                  <h3 className="text-lg font-medium text-gray-900">{classItem.name}</h3>
                  <div className="mt-4 text-sm text-gray-500">
                    <p>Schedule: {classItem.schedule}</p>
                    <p>Students: {classItem.students}</p>
                    <p>Attendance Rate: {classItem.attendance}</p>
                  </div>
                  <div className="mt-4">
                    <Button asChild variant="outline" className="mr-2">
                      <Link to="/attendance">Take Attendance</Link>
                    </Button>
                    <Button asChild variant="ghost">
                      <Link to="/manageseats">Manage Seats</Link>
                    </Button>
                  </div>
                </div>
              </div>
            ))}
          </div>
        </div>
      </main>
    </div>
  )
}
