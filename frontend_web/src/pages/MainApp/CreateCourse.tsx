import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { courseApi } from '@/services/api/courses'
import { useToast } from '@/components/ui/use-toast'

export default function CreateCoursePage() {
  const navigate = useNavigate()
  const { toast } = useToast()
  const [loading, setLoading] = useState(false)
  const [formData, setFormData] = useState({
    name: '',
    courseCode: '',
    description: '',
    schedule: '',
    room: ''
  })

  const handleChange = (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) => {
    const { name, value } = e.target
    setFormData(prev => ({
      ...prev,
      [name]: value
    }))
  }

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    
    // Validation
    if (!formData.name.trim()) {
      toast({
        title: 'Error',
        description: 'Class name is required',
        variant: 'destructive'
      })
      return
    }
    
    if (!formData.schedule.trim()) {
      toast({
        title: 'Error',
        description: 'Time period is required',
        variant: 'destructive'
      })
      return
    }
    
    if (!formData.room.trim()) {
      toast({
        title: 'Error',
        description: 'Room is required',
        variant: 'destructive'
      })
      return
    }
    
    // Generate a course code if not provided
    if (!formData.courseCode.trim()) {
      const generatedCode = formData.name
        .split(' ')
        .map(word => word.charAt(0).toUpperCase())
        .join('')
        .substring(0, 5) + Math.floor(Math.random() * 1000).toString().padStart(3, '0')
      
      formData.courseCode = generatedCode
    }
    
    try {
      setLoading(true)
      await courseApi.createCourse(formData)
      
      toast({
        title: 'Success',
        description: 'Course created successfully',
      })
      
      // Navigate back to home/dashboard
      navigate('/home')
    } catch (error: any) {
      console.error('Failed to create course:', error)
      toast({
        title: 'Error',
        description: error.response?.data?.message || 'Failed to create course. Please try again.',
        variant: 'destructive'
      })
    } finally {
      setLoading(false)
    }
  }
  
  const handleCancel = () => {
    navigate('/home')
  }

  return (
    <div className="max-w-2xl mx-auto">
      <div className="mb-8">
        <h1 className="text-3xl font-bold text-gray-900">Course Creation</h1>
        <p className="mt-2 text-gray-600">Create a new class</p>
      </div>
      
      <div className="bg-white rounded-xl shadow-sm p-6">
        <form onSubmit={handleSubmit}>
          <div className="space-y-6">
            <div>
              <label htmlFor="name" className="block text-lg font-medium text-gray-900 mb-2">
                Course Name
              </label>
              <Input
                id="name"
                name="name"
                value={formData.name}
                onChange={handleChange}
                placeholder="CSIT342 System Integration"
                className="w-full"
              />
            </div>
            
            <div>
              <label htmlFor="schedule" className="block text-lg font-medium text-gray-900 mb-2">
                Time Period
              </label>
              <Input
                id="schedule"
                name="schedule"
                value={formData.schedule}
                onChange={handleChange}
                placeholder="Thursday 12:00PM - 1:00PM"
                className="w-full"
              />
            </div>
            
            <div>
              <label htmlFor="room" className="block text-lg font-medium text-gray-900 mb-2">
                Room
              </label>
              <Input
                id="room"
                name="room"
                value={formData.room}
                onChange={handleChange}
                placeholder="Room 203"
                className="w-full"
              />
            </div>
            
            <div>
              <label htmlFor="description" className="block text-lg font-medium text-gray-900 mb-2">
                Description (Optional)
              </label>
              <textarea
                id="description"
                name="description"
                value={formData.description}
                onChange={handleChange}
                rows={3}
                placeholder="Enter course description"
                className="w-full rounded-md border border-input bg-background px-3 py-2 text-sm ring-offset-background focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2"
              />
            </div>
            
            <div>
              <label htmlFor="courseCode" className="block text-lg font-medium text-gray-900 mb-2">
                Course Code
              </label>
              <Input
                id="courseCode"
                name="courseCode"
                value={formData.courseCode}
                onChange={handleChange}
                placeholder="CSIT342"
                className="w-full"
              />
              <p className="mt-1 text-sm text-gray-500">
                Leave empty to generate automatically
              </p>
            </div>

            <div className="flex justify-end space-x-4 mt-8">
              <Button
                type="button"
                variant="outline"
                onClick={handleCancel}
                className="w-28 bg-white hover:bg-gray-100 text-red-500 border-red-300"
              >
                Cancel
              </Button>
              <Button
                type="submit"
                disabled={loading}
                className="w-28 bg-green-500 hover:bg-green-600 text-white"
              >
                {loading ? (
                  <div className="animate-spin h-5 w-5 border-2 border-white border-t-transparent rounded-full"></div>
                ) : (
                  'Confirm'
                )}
              </Button>
            </div>
          </div>
        </form>
      </div>
    </div>
  )
}
