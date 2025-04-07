import { useState } from 'react'
import { Button } from '@/components/ui/button'

type Seat = {
  id: number
  row: number
  col: number
  studentName: string | null
}

export default function ManageSeatsPage() {
  const [seats, setSeats] = useState<Seat[]>(() => {
    const initialSeats: Seat[] = []
    for (let row = 0; row < 5; row++) {
      for (let col = 0; col < 6; col++) {
        initialSeats.push({
          id: row * 6 + col,
          row,
          col,
          studentName: null
        })
      }
    }
    return initialSeats
  })

  const handleAssignStudent = (seatId: number) => {
    const studentName = prompt('Enter student name:')
    if (studentName) {
      setSeats(seats.map(seat => 
        seat.id === seatId ? { ...seat, studentName } : seat
      ))
    }
  }

  return (
    <div className="min-h-screen bg-gray-50 py-10">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="mb-8">
          <h1 className="text-3xl font-bold text-gray-900">Seat Plan Management</h1>
          <p className="mt-2 text-sm text-gray-600">
            Click on a seat to assign or update a student
          </p>
        </div>

        <div className="bg-white rounded-lg shadow p-6">
          <div className="mb-4 flex justify-end space-x-4">
            <Button variant="outline">Save Layout</Button>
            <Button variant="outline">Clear All</Button>
          </div>

          <div className="grid grid-cols-6 gap-4">
            {seats.map((seat) => (
              <div
                key={seat.id}
                className="aspect-square border rounded-lg flex items-center justify-center p-2 cursor-pointer hover:bg-gray-50"
                onClick={() => handleAssignStudent(seat.id)}
              >
                {seat.studentName ? (
                  <div className="text-center">
                    <div className="w-8 h-8 bg-primary/10 rounded-full flex items-center justify-center mb-1">
                      {seat.studentName.charAt(0)}
                    </div>
                    <p className="text-xs truncate">{seat.studentName}</p>
                  </div>
                ) : (
                  <div className="text-gray-400">Empty</div>
                )}
              </div>
            ))}
          </div>

          <div className="mt-6">
            <div className="text-sm text-gray-500">
              <p>Instructions:</p>
              <ul className="list-disc pl-5 mt-2">
                <li>Click on any seat to assign a student</li>
                <li>Click on an assigned seat to update the student</li>
                <li>Use the Save Layout button to save your changes</li>
                <li>Use Clear All to reset the seating arrangement</li>
              </ul>
            </div>
          </div>
        </div>
      </div>
    </div>
  )
}
