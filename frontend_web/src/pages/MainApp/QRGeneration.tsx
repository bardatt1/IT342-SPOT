import { useState } from 'react'
import { Button } from '@/components/ui/button'

type Class = {
  id: number
  name: string
  section: string
  schedule: string
}

const classes: Class[] = [
  {
    id: 1,
    name: 'IT342',
    section: 'A',
    schedule: 'MWF 10:00 AM - 11:30 AM'
  },
  {
    id: 2,
    name: 'IT343',
    section: 'B',
    schedule: 'TTH 1:00 PM - 2:30 PM'
  },
  // Add more classes as needed
]

export default function QRGenerationPage() {
  const [selectedClass, setSelectedClass] = useState<Class | null>(null)

  return (
    <div className="min-h-screen bg-gray-50 py-10">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="mb-8">
          <h1 className="text-3xl font-bold text-gray-900">QR Code Generation</h1>
          <p className="mt-2 text-sm text-gray-600">
            Generate QR codes for attendance tracking
          </p>
        </div>

        <div className="grid grid-cols-1 lg:grid-cols-2 gap-8">
          {/* Class Selection */}
          <div className="bg-white shadow rounded-lg p-6">
            <h2 className="text-lg font-medium text-gray-900 mb-4">Select Class</h2>
            <div className="space-y-4">
              {classes.map((classItem) => (
                <div
                  key={classItem.id}
                  className={`p-4 rounded-lg border cursor-pointer transition-colors duration-200 ${
                    selectedClass?.id === classItem.id
                      ? 'border-primary bg-primary/5'
                      : 'border-gray-200 hover:border-primary/50'
                  }`}
                  onClick={() => setSelectedClass(classItem)}
                >
                  <h3 className="font-medium text-gray-900">
                    {classItem.name} - Section {classItem.section}
                  </h3>
                  <p className="text-sm text-gray-500 mt-1">{classItem.schedule}</p>
                </div>
              ))}
            </div>
          </div>

          {/* QR Code Display */}
          <div className="bg-white shadow rounded-lg p-6">
            <h2 className="text-lg font-medium text-gray-900 mb-4">QR Code</h2>
            {selectedClass ? (
              <div className="text-center">
                <div className="bg-gray-100 rounded-lg p-8 inline-block mb-4">
                  {/* Placeholder for QR Code */}
                  <div className="w-48 h-48 bg-gray-200 rounded-lg flex items-center justify-center">
                    <p className="text-gray-500">QR Code will appear here</p>
                  </div>
                </div>
                <div className="space-y-4">
                  <div className="text-sm text-gray-600">
                    <p>Class: {selectedClass.name}</p>
                    <p>Section: {selectedClass.section}</p>
                    <p>Schedule: {selectedClass.schedule}</p>
                    <p>Date: {new Date().toLocaleDateString()}</p>
                  </div>
                  <div className="flex justify-center space-x-4">
                    <Button>Download QR</Button>
                    <Button variant="outline">Share QR</Button>
                  </div>
                </div>
              </div>
            ) : (
              <div className="text-center py-12">
                <p className="text-gray-500">Select a class to generate QR code</p>
              </div>
            )}
          </div>
        </div>

        {/* Instructions */}
        <div className="mt-8 bg-white shadow rounded-lg p-6">
          <h2 className="text-lg font-medium text-gray-900 mb-4">Instructions</h2>
          <div className="prose prose-sm text-gray-500">
            <ol className="list-decimal list-inside space-y-2">
              <li>Select the class for which you want to generate a QR code</li>
              <li>The QR code will be generated automatically</li>
              <li>Students can scan this QR code using their mobile devices to mark their attendance</li>
              <li>Each QR code is valid for one class session only</li>
              <li>You can download or share the QR code with your students</li>
            </ol>
          </div>
        </div>
      </div>
    </div>
  )
}
