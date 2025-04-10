import { useState, useEffect } from 'react'
import { Button } from '@/components/ui/button'
import { useToast } from '@/components/ui/use-toast'
import { Seat, SeatPlan, seatPlanApi } from '@/services/api/seatplans'
import { useNavigate, useLocation } from 'react-router-dom'
import { Loader2, Plus, Save } from 'lucide-react'

export default function ManageSeatsPage() {
  const { toast } = useToast()
  const navigate = useNavigate()
  const location = useLocation()
  const searchParams = new URLSearchParams(location.search)
  const courseId = searchParams.get('courseId')
  
  // States
  const [loading, setLoading] = useState(true)
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState('')
  const [seatPlans, setSeatPlans] = useState<SeatPlan[]>([])
  const [activeSeatPlan, setActiveSeatPlan] = useState<SeatPlan | null>(null)
  const [seats, setSeats] = useState<Seat[]>([])
  const [showNewSeatPlanForm, setShowNewSeatPlanForm] = useState(false)
  const [newSeatPlanName, setNewSeatPlanName] = useState('')
  const [rowCount, setRowCount] = useState(5)
  const [colCount, setColCount] = useState(6)
  
  // Fetch seat plans when component mounts
  useEffect(() => {
    const fetchSeatPlans = async () => {
      if (!courseId) {
        setError('No course selected')
        setLoading(false)
        return
      }

      try {
        setLoading(true)
        const response = await seatPlanApi.getSeatPlans(parseInt(courseId))
        const seatPlanData = response.data || []
        setSeatPlans(seatPlanData)
        
        // Set active seat plan if exists
        if (Array.isArray(seatPlanData) && seatPlanData.length > 0) {
          const active = seatPlanData.find((sp: SeatPlan) => sp.isActive)
          if (active) {
            setActiveSeatPlan(active)
            setSeats(active.seats || [])
          }
        }
      } catch (err: any) {
        console.error('Error fetching seat plans:', err)
        setError('Failed to load seat plans')
        toast({
          title: 'Error',
          description: 'Could not load seat plans. Please try again.',
          variant: 'destructive'
        })
      } finally {
        setLoading(false)
      }
    }

    fetchSeatPlans()
  }, [courseId, toast])

  const handleAssignStudent = async (seatId: number) => {
    if (!activeSeatPlan || !courseId) return
    
    const studentName = prompt('Enter student name:')
    if (studentName) {
      try {
        setSaving(true)
        // Mock API call since we don't have student IDs yet
        // In a real app, we'd have a student selection dropdown
        const studentId = Math.floor(Math.random() * 1000) // Simulated student ID
        
        await seatPlanApi.assignStudentToSeat(
          activeSeatPlan.id, 
          seatId, 
          studentId, 
          parseInt(courseId)
        )
        
        // Update local state
        setSeats(seats.map(seat => 
          seat.id === seatId ? { ...seat, studentName } : seat
        ))
        
        toast({
          title: 'Success',
          description: `${studentName} assigned to seat successfully.`
        })
      } catch (err: any) {
        toast({
          title: 'Error',
          description: 'Failed to assign student to seat.',
          variant: 'destructive'
        })
      } finally {
        setSaving(false)
      }
    }
  }
  
  const handleRemoveStudent = async (seatId: number) => {
    if (!activeSeatPlan || !courseId) return
    
    if (confirm('Remove student from this seat?')) {
      try {
        setSaving(true)
        await seatPlanApi.removeStudentFromSeat(
          activeSeatPlan.id, 
          seatId, 
          parseInt(courseId)
        )
        
        // Update local state
        setSeats(seats.map(seat => 
          seat.id === seatId ? { ...seat, studentName: undefined, studentId: undefined } : seat
        ))
        
        toast({
          title: 'Success',
          description: 'Student removed from seat.'
        })
      } catch (err: any) {
        toast({
          title: 'Error',
          description: 'Failed to remove student from seat.',
          variant: 'destructive'
        })
      } finally {
        setSaving(false)
      }
    }
  }
  
  const handleSelectSeatPlan = async (seatPlanId: number) => {
    if (!courseId) return
    
    try {
      setLoading(true)
      const response = await seatPlanApi.getSeatPlan(
        seatPlanId, 
        parseInt(courseId)
      )
      setActiveSeatPlan(response.data)
      setSeats(response.data.seats || [])
    } catch (err: any) {
      toast({
        title: 'Error',
        description: 'Failed to load seat plan.',
        variant: 'destructive'
      })
    } finally {
      setLoading(false)
    }
  }
  
  const createNewSeatPlan = async () => {
    if (!courseId || !newSeatPlanName) return
    
    try {
      setSaving(true)
      const response = await seatPlanApi.createSeatPlan(
        parseInt(courseId),
        newSeatPlanName,
        rowCount,
        colCount
      )
      
      // Add the new seat plan to the list
      setSeatPlans([...seatPlans, response.data])
      
      // Set as active
      setActiveSeatPlan(response.data)
      setSeats(response.data.seats || [])
      
      // Reset form
      setShowNewSeatPlanForm(false)
      setNewSeatPlanName('')
      
      toast({
        title: 'Success',
        description: 'New seat plan created successfully.'
      })
    } catch (err: any) {
      toast({
        title: 'Error',
        description: 'Failed to create new seat plan.',
        variant: 'destructive'
      })
    } finally {
      setSaving(false)
    }
  }
  
  const handleSetActive = async (seatPlanId: number) => {
    if (!courseId) return
    
    try {
      setSaving(true)
      await seatPlanApi.setActiveSeatPlan(
        seatPlanId, 
        parseInt(courseId)
      )
      
      // Update all seat plans to reflect the change, make sure seatPlans is an array
      if (Array.isArray(seatPlans)) {
        setSeatPlans(seatPlans.map(sp => ({
          ...sp,
          isActive: sp.id === seatPlanId
        })))
      }
      
      // Also update the active seat plan if it exists
      if (activeSeatPlan) {
        setActiveSeatPlan({
          ...activeSeatPlan,
          isActive: activeSeatPlan.id === seatPlanId
        })
      }
      
      toast({
        title: 'Success',
        description: 'Seat plan set as active.'
      })
    } catch (err: any) {
      toast({
        title: 'Error',
        description: 'Failed to set seat plan as active.',
        variant: 'destructive'
      })
    } finally {
      setSaving(false)
    }
  }

  // Add the updateSeatLabel function
  const updateSeatLabel = async (seatId: number, label: string) => {
    if (!activeSeatPlan || !courseId) return
    
    try {
      setSaving(true)
      await seatPlanApi.updateSeatLabel(
        activeSeatPlan.id, 
        seatId, 
        label, 
        parseInt(courseId)
      )
      
      // Update local state
      setSeats(seats.map(seat => 
        seat.id === seatId ? { ...seat, label } : seat
      ))
      
      toast({
        title: 'Success',
        description: 'Seat label updated.'
      })
    } catch (err: any) {
      toast({
        title: 'Error',
        description: 'Failed to update seat label.',
        variant: 'destructive'
      })
    } finally {
      setSaving(false)
    }
  }

  if (loading) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <div className="flex flex-col items-center space-y-4">
          <Loader2 className="h-12 w-12 animate-spin text-primary" />
          <p className="text-lg text-gray-500">Loading seat plans...</p>
        </div>
      </div>
    )
  }

  if (error && !courseId) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <div className="bg-white p-8 rounded-lg shadow-md max-w-md w-full">
          <h2 className="text-2xl font-bold text-red-500 mb-4">Error</h2>
          <p className="text-gray-700 mb-6">Please select a course to manage seat plans.</p>
          <Button onClick={() => navigate('/home')}>Return to Dashboard</Button>
        </div>
      </div>
    )
  }

  return (
    <div className="min-h-screen bg-gray-50 py-10">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="mb-8">
          <h1 className="text-3xl font-bold text-gray-900">Seat Plan Management</h1>
          <p className="mt-2 text-sm text-gray-600">
            Manage seating arrangements for your classes
          </p>
        </div>

        <div className="bg-white rounded-lg shadow overflow-hidden">
          {/* Seat plan selector */}
          <div className="border-b border-gray-200 bg-gray-50 p-4">
            <div className="flex flex-wrap items-center gap-4">
              <span className="text-sm font-medium text-gray-700">Select Seat Plan:</span>
              <div className="flex flex-wrap gap-2">
                {Array.isArray(seatPlans) && seatPlans.length > 0 ? (
                  seatPlans.map((seatPlan) => (
                    <button
                      key={seatPlan.id}
                      className={`px-3 py-1 text-sm rounded-full ${seatPlan.id === activeSeatPlan?.id
                        ? 'bg-primary text-white'
                        : 'bg-gray-100 text-gray-700 hover:bg-gray-200'
                      }`}
                      onClick={() => handleSelectSeatPlan(seatPlan.id)}
                    >
                      {seatPlan.name}
                      {seatPlan.isActive && (
                        <span className="ml-1 text-xs bg-green-500 text-white px-1 rounded">Active</span>
                      )}
                    </button>
                  ))
                ) : (
                  <p className="text-sm text-gray-500">No seat plans available</p>
                )}
                <button
                  className="px-3 py-1 text-sm rounded-full bg-gray-100 text-gray-700 hover:bg-gray-200 flex items-center"
                  onClick={() => setShowNewSeatPlanForm(true)}
                >
                  <Plus size={14} className="mr-1" /> New
                </button>
              </div>
            </div>

            {/* New seat plan form */}
            {showNewSeatPlanForm && (
              <div className="mt-4 p-4 bg-gray-100 rounded-md">
                <h3 className="text-sm font-medium mb-3">Create New Seat Plan</h3>
                <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                  <div>
                    <label className="block text-xs font-medium text-gray-700 mb-1">Name</label>
                    <input
                      type="text"
                      value={newSeatPlanName}
                      onChange={(e) => setNewSeatPlanName(e.target.value)}
                      className="w-full px-3 py-2 border rounded-md text-sm"
                      placeholder="Enter name"
                    />
                  </div>
                  <div>
                    <label className="block text-xs font-medium text-gray-700 mb-1">Rows</label>
                    <input
                      type="number"
                      value={rowCount}
                      onChange={(e) => setRowCount(parseInt(e.target.value) || 1)}
                      min="1"
                      max="10"
                      className="w-full px-3 py-2 border rounded-md text-sm"
                    />
                  </div>
                  <div>
                    <label className="block text-xs font-medium text-gray-700 mb-1">Columns</label>
                    <input
                      type="number"
                      value={colCount}
                      onChange={(e) => setColCount(parseInt(e.target.value) || 1)}
                      min="1"
                      max="10"
                      className="w-full px-3 py-2 border rounded-md text-sm"
                    />
                  </div>
                </div>
                <div className="mt-4 flex gap-2">
                  <Button onClick={createNewSeatPlan} disabled={saving || !newSeatPlanName}>
                    {saving ? <Loader2 className="mr-2 h-4 w-4 animate-spin" /> : <Save className="mr-2 h-4 w-4" />}
                    Create
                  </Button>
                  <Button variant="outline" onClick={() => setShowNewSeatPlanForm(false)}>
                    Cancel
                  </Button>
                </div>
              </div>
            )}
          </div>

          {activeSeatPlan ? (
            <div className="p-6">
              <div className="mb-4 flex justify-between items-center">
                <h2 className="text-xl font-semibold text-gray-800">
                  {activeSeatPlan.name}
                  {!activeSeatPlan.isActive && (
                    <Button 
                      variant="outline" 
                      size="sm" 
                      className="ml-2"
                      onClick={() => handleSetActive(activeSeatPlan.id)}
                    >
                      Set as Active
                    </Button>
                  )}
                </h2>
              </div>

              <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-6 gap-4">
                {seats.map((seat) => (
                  <div
                    key={seat.id}
                    className="aspect-square border rounded-lg flex flex-col items-center justify-center p-2 hover:bg-gray-50 relative"
                  >
                    {/* Seat label - make it clickable to edit */}
                    <div 
                      className="absolute top-1 left-1 text-xs text-gray-400 cursor-pointer hover:text-primary"
                      onClick={() => {
                        const newLabel = prompt('Enter new seat label:', seat.label);
                        if (newLabel && newLabel !== seat.label) {
                          updateSeatLabel(seat.id, newLabel);
                        }
                      }}
                      title="Click to edit label"
                    >
                      {seat.label || `R${seat.row+1}C${seat.column+1}`}
                    </div>
                    
                    {/* Student info */}
                    {seat.studentName ? (
                      <div className="text-center">
                        <div className="w-12 h-12 bg-primary/10 rounded-full flex items-center justify-center mb-2">
                          {seat.studentName.charAt(0)}
                        </div>
                        <p className="text-sm font-medium truncate max-w-full">{seat.studentName}</p>
                        <button
                          className="mt-2 text-xs text-red-500 hover:text-red-700"
                          onClick={() => handleRemoveStudent(seat.id)}
                        >
                          Remove
                        </button>
                      </div>
                    ) : (
                      <div 
                        className="flex flex-col items-center justify-center cursor-pointer w-full h-full"
                        onClick={() => handleAssignStudent(seat.id)}
                      >
                        <div className="text-gray-400 text-sm">Empty</div>
                        <div className="text-primary text-xs mt-1">Click to assign</div>
                      </div>
                    )}
                  </div>
                ))}
              </div>
            </div>
          ) : (
            <div className="p-6 text-center">
              <p className="text-gray-500 my-8">
                {seatPlans.length > 0 
                  ? 'Select a seat plan from above to manage it'
                  : 'No seat plans found. Create a new one to get started.'}
              </p>
            </div>
          )}
        </div>
      </div>
    </div>
  )
}
