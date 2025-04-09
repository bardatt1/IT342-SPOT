import { useState } from 'react'
import { Button } from '@/components/ui/button'

type Student = {
  id: number
  name: string
  status: 'present' | 'absent' | 'late' | 'unmarked'
}

const initialStudents: Student[] = [
  { id: 1, name: 'John Doe', status: 'unmarked' },
  { id: 2, name: 'Jane Smith', status: 'unmarked' },
  { id: 3, name: 'Mike Johnson', status: 'unmarked' },
  // Add more students as needed
]

export default function AttendancePage() {
  const [students, setStudents] = useState<Student[]>(initialStudents)
  const [selectedDate, setSelectedDate] = useState<string>(
    new Date().toISOString().split('T')[0]
  )

  const handleStatusChange = (studentId: number, status: Student['status']) => {
    setStudents(students.map(student =>
      student.id === studentId ? { ...student, status } : student
    ))
  }

  const getStatusColor = (status: Student['status']) => {
    switch (status) {
      case 'present':
        return 'bg-green-100 text-green-800'
      case 'absent':
        return 'bg-red-100 text-red-800'
      case 'late':
        return 'bg-yellow-100 text-yellow-800'
      default:
        return 'bg-gray-100 text-gray-800'
    }
  }

  return (
    <div className="min-h-screen bg-gray-50 py-10">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="mb-8">
          <h1 className="text-3xl font-bold text-gray-900">Attendance Tracking</h1>
          <div className="mt-4 flex items-center space-x-4">
            <input
              type="date"
              value={selectedDate}
              onChange={(e) => setSelectedDate(e.target.value)}
              className="rounded-md border-gray-300 shadow-sm focus:border-primary focus:ring-primary"
            />
            <Button variant="outline">Generate QR Code</Button>
          </div>
        </div>

        <div className="bg-white shadow overflow-hidden sm:rounded-lg">
          <div className="px-4 py-5 sm:p-6">
            <div className="flex justify-between mb-4">
              <div className="flex space-x-2">
                <Button
                  variant="outline"
                  onClick={() => {
                    setStudents(students.map(student => ({
                      ...student,
                      status: 'present'
                    })))
                  }}
                >
                  Mark All Present
                </Button>
                <Button variant="outline">Save Attendance</Button>
              </div>
              <div className="flex items-center space-x-4">
                <span className="inline-flex items-center px-3 py-1 rounded-full text-xs font-medium bg-green-100 text-green-800">
                  Present: {students.filter(s => s.status === 'present').length}
                </span>
                <span className="inline-flex items-center px-3 py-1 rounded-full text-xs font-medium bg-red-100 text-red-800">
                  Absent: {students.filter(s => s.status === 'absent').length}
                </span>
                <span className="inline-flex items-center px-3 py-1 rounded-full text-xs font-medium bg-yellow-100 text-yellow-800">
                  Late: {students.filter(s => s.status === 'late').length}
                </span>
              </div>
            </div>

            <div className="mt-6">
              <table className="min-w-full divide-y divide-gray-200">
                <thead className="bg-gray-50">
                  <tr>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                      Student Name
                    </th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                      Status
                    </th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                      Actions
                    </th>
                  </tr>
                </thead>
                <tbody className="bg-white divide-y divide-gray-200">
                  {students.map((student) => (
                    <tr key={student.id}>
                      <td className="px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-900">
                        {student.name}
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap">
                        <span className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium ${getStatusColor(student.status)}`}>
                          {student.status.charAt(0).toUpperCase() + student.status.slice(1)}
                        </span>
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                        <div className="flex space-x-2">
                          <Button
                            variant="ghost"
                            className="text-green-600 hover:text-green-900"
                            onClick={() => handleStatusChange(student.id, 'present')}
                          >
                            Present
                          </Button>
                          <Button
                            variant="ghost"
                            className="text-red-600 hover:text-red-900"
                            onClick={() => handleStatusChange(student.id, 'absent')}
                          >
                            Absent
                          </Button>
                          <Button
                            variant="ghost"
                            className="text-yellow-600 hover:text-yellow-900"
                            onClick={() => handleStatusChange(student.id, 'late')}
                          >
                            Late
                          </Button>
                        </div>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>
        </div>
      </div>
    </div>
  )
}
