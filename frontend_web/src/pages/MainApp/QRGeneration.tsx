import { useState, useEffect } from 'react'
import { Button } from '@/components/ui/button'
import { useToast } from '@/components/ui/use-toast'
import { courseApi, Session } from '@/services/api/courses'
import { sessionApi } from '@/services/api/sessions'
import { useLocation, useNavigate } from 'react-router-dom'
import { QrCode, RefreshCw, Download, PlayCircle, StopCircle, Loader2, AlertCircle } from 'lucide-react'

const formatDate = (dateString: string) => {
  const date = new Date(dateString)
  return date.toLocaleDateString('en-US', {
    weekday: 'long',
    year: 'numeric',
    month: 'long',
    day: 'numeric'
  })
}

const formatTime = (dateString: string) => {
  const date = new Date(dateString)
  return date.toLocaleTimeString('en-US', {
    hour: 'numeric',
    minute: '2-digit',
    hour12: true
  })
}

export default function QRGenerationPage() {
  const { toast } = useToast()
  const navigate = useNavigate()
  const location = useLocation()
  const searchParams = new URLSearchParams(location.search)
  const sessionId = searchParams.get('sessionId')

  // States
  const [sessions, setSessions] = useState<Session[]>([])
  const [selectedSession, setSelectedSession] = useState<Session | null>(null)
  const [qrCode, setQrCode] = useState('')
  const [isGenerating, setIsGenerating] = useState(false)
  const [isSessionActive, setIsSessionActive] = useState(false)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  // Fetch sessions when component mounts
  useEffect(() => {
    const fetchSessions = async () => {
      try {
        setLoading(true)
        const response = await courseApi.getUpcomingSessions()
        setSessions(response.data || [])

        // If sessionId is provided in the URL, select that session
        if (sessionId) {
          const session = response.data.find(s => s.id.toString() === sessionId)
          if (session) {
            setSelectedSession(session)
            setIsSessionActive(session.status === 'ACTIVE')
            if (session.status === 'ACTIVE') {
              generateQRCode(parseInt(sessionId))
            }
          }
        }
      } catch (err: any) {
        setError(err.message || 'Failed to load sessions')
        toast({
          title: 'Error',
          description: 'Could not load your sessions. Please try again.',
          variant: 'destructive'
        })
      } finally {
        setLoading(false)
      }
    }

    fetchSessions()
  }, [sessionId, toast])

  // Generate QR code for a session
  const generateQRCode = async (sessionId: number) => {
    try {
      setIsGenerating(true)
      const response = await sessionApi.generateQRCode(sessionId)
      setQrCode(response.data.qrCode)
    } catch (err: any) {
      toast({
        title: 'Error',
        description: 'Failed to generate QR code. Please try again.',
        variant: 'destructive'
      })
    } finally {
      setIsGenerating(false)
    }
  }

  // Start a session
  const startSession = async () => {
    if (!selectedSession) return

    try {
      const response = await sessionApi.startSession(selectedSession.id)
      setQrCode(response.data.qrCode)
      setIsSessionActive(true)
      toast({
        title: 'Success',
        description: 'Session started successfully!',
      })
    } catch (err: any) {
      toast({
        title: 'Error',
        description: err.message || 'Failed to start session. Please try again.',
        variant: 'destructive'
      })
    }
  }

  // End a session
  const endSession = async () => {
    if (!selectedSession) return

    try {
      await sessionApi.endSession(selectedSession.id)
      setIsSessionActive(false)
      setQrCode('')
      toast({
        title: 'Success',
        description: 'Session ended successfully!',
      })
      // Navigate to attendance page for this session
      navigate(`/attendance?sessionId=${selectedSession.id}`)
    } catch (err: any) {
      toast({
        title: 'Error',
        description: err.message || 'Failed to end session. Please try again.',
        variant: 'destructive'
      })
    }
  }

  // Refresh QR code
  const refreshQRCode = () => {
    if (!selectedSession) return
    generateQRCode(selectedSession.id)
  }

  // Download QR code as image
  const downloadQRCode = () => {
    if (!qrCode) return

    // Create a link element
    const link = document.createElement('a')
    link.href = qrCode
    link.download = `qr-code-session-${selectedSession?.id || 'unknown'}.png`
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
  }

  return (
    <div>
      <div className="mb-8">
        <h1 className="text-3xl font-bold text-gray-900">QR Code Generation</h1>
        <p className="mt-2 text-gray-600">
          Generate QR codes for attendance tracking
        </p>
      </div>

      {loading ? (
        <div className="bg-white rounded-xl shadow-sm p-6 flex justify-center items-center min-h-[300px]">
          <div className="flex flex-col items-center">
            <Loader2 className="h-8 w-8 animate-spin text-primary mb-2" />
            <span className="text-gray-500">Loading sessions...</span>
          </div>
        </div>
      ) : error ? (
        <div className="bg-white rounded-xl shadow-sm p-6 flex items-center justify-center min-h-[300px] text-red-500">
          <AlertCircle className="mr-2 h-5 w-5" />
          <span>{error}</span>
        </div>
      ) : (
        <div className="grid grid-cols-1 lg:grid-cols-2 gap-8">
          {/* Session Selection */}
          <div className="bg-white shadow-sm rounded-xl p-6">
            <h2 className="text-lg font-semibold text-gray-900 mb-4">Select Session</h2>
            {sessions.length === 0 ? (
              <div className="text-center py-12 text-gray-500">
                <p>No upcoming sessions available.</p>
                <Button className="mt-4" onClick={() => navigate('/home')}>
                  Back to Dashboard
                </Button>
              </div>
            ) : (
              <div className="space-y-4 max-h-[400px] overflow-y-auto pr-2">
                {sessions.map((session) => (
                  <div
                    key={session.id}
                    className={`p-4 rounded-lg border cursor-pointer transition-colors duration-200 ${
                      selectedSession?.id === session.id
                        ? 'border-primary bg-primary/5'
                        : 'border-gray-200 hover:border-primary/50'
                    }`}
                    onClick={() => {
                      setSelectedSession(session)
                      setIsSessionActive(session.status === 'ACTIVE')
                      if (session.status === 'ACTIVE') {
                        generateQRCode(session.id)
                      } else {
                        setQrCode('')
                      }
                    }}
                  >
                    <div className="flex items-start justify-between">
                      <div>
                        <h3 className="font-medium text-gray-900">
                          {session.course?.courseCode}
                        </h3>
                        <p className="text-sm text-gray-500 mt-1">{session.course?.name}</p>
                        <div className="mt-2 text-xs text-gray-500">
                          <p>Date: {session.startTime ? formatDate(session.startTime) : 'Not set'}</p>
                          <p>Time: {session.startTime ? formatTime(session.startTime) : 'Not set'} - {session.endTime ? formatTime(session.endTime) : 'Not set'}</p>
                        </div>
                      </div>
                      <span className={
                        `px-2 py-1 inline-flex text-xs leading-4 font-semibold rounded-full ${
                          session.status === 'ACTIVE' ? 'bg-green-100 text-green-800' :
                          session.status === 'SCHEDULED' ? 'bg-blue-100 text-blue-800' :
                          session.status === 'COMPLETED' ? 'bg-gray-100 text-gray-800' :
                          'bg-red-100 text-red-800'
                        }`
                      }>
                        {session.status}
                      </span>
                    </div>
                  </div>
                ))}
              </div>
            )}
          </div>

          {/* QR Code Display */}
          <div className="bg-white shadow-sm rounded-xl p-6">
            <h2 className="text-lg font-semibold text-gray-900 mb-4">QR Code</h2>
            {selectedSession ? (
              <div className="text-center">
                {isGenerating ? (
                  <div className="bg-gray-100 rounded-lg p-8 inline-block mb-4 w-48 h-48 flex items-center justify-center">
                    <Loader2 className="h-8 w-8 animate-spin text-primary" />
                  </div>
                ) : qrCode ? (
                  <div className="mb-4">
                    <img src={qrCode} alt="QR Code" className="inline-block max-w-[200px] max-h-[200px]" />
                  </div>
                ) : (
                  <div className="bg-gray-100 rounded-lg p-8 inline-block mb-4 w-48 h-48 flex items-center justify-center">
                    {isSessionActive ? (
                      <div className="flex flex-col items-center text-gray-500">
                        <RefreshCw className="h-8 w-8 mb-2" />
                        <Button size="sm" onClick={refreshQRCode}>Generate QR Code</Button>
                      </div>
                    ) : (
                      <div className="flex flex-col items-center text-gray-500">
                        <QrCode className="h-8 w-8 mb-2" />
                        <p>Start session to generate QR</p>
                      </div>
                    )}
                  </div>
                )}
                <div className="space-y-4">
                  <div className="text-sm text-gray-600">
                    <p className="font-medium">{selectedSession.course?.courseCode} - {selectedSession.course?.name}</p>
                    <p>Status: <span className={`font-medium ${isSessionActive ? 'text-green-600' : 'text-blue-600'}`}>{isSessionActive ? 'Active' : 'Scheduled'}</span></p>
                    <p>Date: {selectedSession.startTime ? formatDate(selectedSession.startTime) : 'Not set'}</p>
                    <p>Time: {selectedSession.startTime ? formatTime(selectedSession.startTime) : 'Not set'} - {selectedSession.endTime ? formatTime(selectedSession.endTime) : 'Not set'}</p>
                  </div>
                  <div className="flex justify-center space-x-3">
                    {isSessionActive ? (
                      <>
                        {qrCode && (
                          <>
                            <Button size="sm" onClick={downloadQRCode} className="flex items-center">
                              <Download className="mr-1 h-4 w-4" />Download
                            </Button>
                            <Button size="sm" onClick={refreshQRCode} variant="outline" className="flex items-center">
                              <RefreshCw className="mr-1 h-4 w-4" />Refresh
                            </Button>
                          </>
                        )}
                        <Button size="sm" variant="destructive" onClick={endSession} className="flex items-center">
                          <StopCircle className="mr-1 h-4 w-4" />End Session
                        </Button>
                      </>
                    ) : (
                      <Button size="sm" onClick={startSession} className="flex items-center">
                        <PlayCircle className="mr-1 h-4 w-4" />Start Session
                      </Button>
                    )}
                  </div>
                </div>
              </div>
            ) : (
              <div className="text-center py-12">
                <QrCode className="h-12 w-12 text-gray-300 mx-auto mb-4" />
                <p className="text-gray-500">Select a session to generate QR code</p>
              </div>
            )}
          </div>
        </div>
      )}

      {/* Instructions */}
      <div className="mt-8 bg-white shadow-sm rounded-xl p-6">
        <h2 className="text-lg font-semibold text-gray-900 mb-4">Instructions</h2>
        <div className="prose prose-sm text-gray-500 max-w-none">
          <ol className="list-decimal list-inside space-y-2">
            <li>Select an upcoming session from the list</li>
            <li>Click the <strong>Start Session</strong> button to activate the session</li>
            <li>A QR code will be generated that students can scan to mark their attendance</li>
            <li>The QR code automatically refreshes periodically for security</li>
            <li>You can manually refresh the QR code by clicking the <strong>Refresh</strong> button</li>
            <li>When the class is over, click <strong>End Session</strong> to finalize attendance</li>
            <li>After ending the session, you'll be redirected to the attendance report</li>
          </ol>
        </div>
      </div>
    </div>
  )
}
