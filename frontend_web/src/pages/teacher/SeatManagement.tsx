import { useState, useEffect } from 'react';
import DashboardLayout from '../../components/ui/layout/DashboardLayout';
import { useAuth } from '../../contexts/AuthContext';
import { useSearchParams } from 'react-router-dom';
import { sectionApi, type Section } from '../../lib/api/section';
import { studentApi, type Student } from '../../lib/api/student';
import { seatApi, type SeatMap } from '../../lib/api/seat';
import { Button } from '../../components/ui/button';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '../../components/ui/card';
import { Alert, AlertDescription, AlertTitle } from '../../components/ui/alert';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '../../components/ui/select';
import { Badge } from '../../components/ui/badge';
import { Save, AlertTriangle, User, UserCheck, Check, Grid, MapPin, Users } from 'lucide-react';
import FirstLoginModal from '../../components/ui/FirstLoginModal';

const SeatManagement = () => {
  const { user } = useAuth();
  const [searchParams] = useSearchParams();
  const [sections, setSections] = useState<Section[]>([]);
  const [selectedSectionId, setSelectedSectionId] = useState<number | null>(null);
  const [seatMap, setSeatMap] = useState<SeatMap | null>(null);
  const [sectionStudents, setSectionStudents] = useState<Student[]>([]);
  const [selectedStudentId, setSelectedStudentId] = useState<number | null>(null);
  const [selectedSeat, setSelectedSeat] = useState<{row: number, column: number} | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [isSaving, setIsSaving] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState<string | null>(null);
  const [showFirstLoginModal, setShowFirstLoginModal] = useState(false);

  // Check for temporary password when user data is loaded
  useEffect(() => {
    // Check if this is a temporary account based on email pattern
    const isTemporaryAccount = 
      // Check if email ends with @edu-spot.me
      user?.email?.endsWith('@spot-edu.me') ||
      // Or if the backend explicitly flags it
      user?.hasTemporaryPassword;
    
    if (isTemporaryAccount) {
      setShowFirstLoginModal(true);
      
      // Store that we've shown the modal so it won't show again after closing
      // This is just for local testing until the backend sets the proper flag
      localStorage.setItem('firstTimeLogin', 'false');
    }
  }, [user]);

  useEffect(() => {
    fetchTeacherSections();
  }, [user?.id]);

  // Get section ID from URL if present
  useEffect(() => {
    const sectionIdParam = searchParams.get('sectionId');
    if (sectionIdParam && sections.length > 0) {
      const parsedId = parseInt(sectionIdParam, 10);
      // Check if the section exists and belongs to this teacher
      const sectionExists = sections.some(s => s.id === parsedId);
      if (sectionExists) {
        setSelectedSectionId(parsedId);
      }
    }
  }, [sections, searchParams]);

  useEffect(() => {
    if (selectedSectionId) {
      // First load students, then load seat map to ensure student data is available
      const loadData = async () => {
        await fetchSectionStudents(selectedSectionId);
        await fetchSeatMap(selectedSectionId);
      };
      loadData();
    }
  }, [selectedSectionId]);

  const fetchTeacherSections = async () => {
    if (!user?.id) return;
    
    try {
      setIsLoading(true);
      const allSections = await sectionApi.getAllSections();
      
      // Extract teacher ID properly from the nested teacher object
      const teacherSections = allSections.filter(section => {
        // In the backend DTO, teacher is a nested object with id
        const sectionTeacherId = section.teacher?.id || 
                               (section.teacher ? section.teacher.id : null);
                               
        console.log(`Section ${section.id} has teacher:`, section.teacher, 
                  `- Extracted teacherId:`, sectionTeacherId);
                  
        return sectionTeacherId === user.id;
      });
      
      console.log(`Found ${teacherSections.length} sections for teacher ${user.id}`);
      setSections(teacherSections);
      
      // Select first section by default if available
      if (teacherSections.length > 0 && !selectedSectionId) {
        setSelectedSectionId(teacherSections[0].id);
      }
    } catch (error) {
      console.error('Error fetching sections:', error);
      setError('Failed to load your sections. Please try again later.');
    } finally {
      setIsLoading(false);
    }
  };

  const fetchSeatMap = async (sectionId: number) => {
    try {
      setIsLoading(true);
      const map = await seatApi.getSeatMap(sectionId);
      setSeatMap(map);
      setError(null);
    } catch (error) {
      console.error('Error fetching seat map:', error);
      setError('Failed to load seat map. Please try again later.');
      setSeatMap(null);
    } finally {
      setIsLoading(false);
    }
  };

  const fetchSectionStudents = async (sectionId: number) => {
    try {
      setIsLoading(true);
      console.log(`Fetching students for section ${sectionId}`);
      
      // Use our teacher-friendly API endpoint that doesn't require admin permissions
      const students = await studentApi.getBySection(sectionId);
      console.log('Fetched students for section:', students);
      
      if (students && students.length > 0) {
        setSectionStudents(students);
        setError(null);
      } else {
        console.warn('No students found for this section or data format is unexpected');
        
        // Attempt to get student data from admin API as fallback (if available)
        try {
          const allStudents = await studentApi.getAll();
          console.log('Fetched all students as fallback:', allStudents);
          setSectionStudents(allStudents);
        } catch (adminError) {
          console.warn('Failed to fetch students using admin API:', adminError);
          setSectionStudents([]);
        }
      }
    } catch (error) {
      console.error('Error fetching students:', error);
      setError('Failed to load students. Please try again later.');
      setSectionStudents([]);
    } finally {
      setIsLoading(false);
    }
  };

  const handleSectionChange = (e: React.ChangeEvent<HTMLSelectElement>) => {
    const sectionId = parseInt(e.target.value);
    setSelectedSectionId(sectionId);
    setSelectedSeat(null);
    setSelectedStudentId(null);
  };

  const handleStudentSelect = (studentId: number) => {
    setSelectedStudentId(studentId);
    // Clear selected seat when a new student is selected
    setSelectedSeat(null);
  };

  const handleSeatSelect = (row: number, column: number) => {
    setSelectedSeat({ row, column });
  };

  const findStudentById = (studentId: number): Student | undefined => {
    return sectionStudents.find(student => student.id === studentId);
  };

  const findStudentBySeat = (row: number, column: number): Student | undefined => {
    if (!seatMap) return undefined;
    
    const seat = seatMap.seats.find(s => s.row === row && s.column === column);
    if (!seat) return undefined;
    
    // The seat object contains the complete student object rather than just the ID
    if (seat.student) {
      // Return the complete student object directly from the seat
      return seat.student as Student;
    } else if (seat.studentId) {
      // Fallback: Try to find by ID in the sectionStudents array if available
      return findStudentById(seat.studentId);
    }
    
    return undefined;
  };

  const isSeatOccupied = (row: number, column: number): boolean => {
    if (!seatMap) return false;
    return seatMap.seats.some(s => s.row === row && s.column === column);
  };

  const getStudentNameById = (studentId: number): string => {
    const student = findStudentById(studentId);
    if (!student) return 'Unknown Student';
    return `${student.lastName}, ${student.firstName}`;
  };

  const assignSeat = async () => {
    if (!selectedSectionId || !selectedStudentId || !selectedSeat) {
      setError('Please select a student and a seat');
      return;
    }
    
    if (!user?.id) {
      setError('Teacher ID not available. Please log in again.');
      return;
    }
    
    try {
      setIsSaving(true);
      setError(null);
      setSuccess(null);
      
      await seatApi.overrideSeat(
        selectedSectionId,
        selectedStudentId,
        selectedSeat.row,
        selectedSeat.column,
        user.id // Pass the current user's ID as the teacher ID
      );
      
      // Refresh seat map
      await fetchSeatMap(selectedSectionId);
      
      setSuccess('Seat assigned successfully');
      setSelectedSeat(null);
      setSelectedStudentId(null);
    } catch (error) {
      console.error('Error assigning seat:', error);
      setError('Failed to assign seat. Please try again later.');
    } finally {
      setIsSaving(false);
    }
  };

  if (isLoading && sections.length === 0) {
    return (
      <DashboardLayout>
        <div className="flex h-full items-center justify-center p-6">
          <div className="flex flex-col items-center space-y-4 text-center">
            <div className="animate-spin text-[#215f47]">
              <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                <path d="M21 12a9 9 0 1 1-6.219-8.56"/>
              </svg>
            </div>
            <p className="text-lg font-medium text-gray-700">Loading your sections...</p>
          </div>
        </div>
      </DashboardLayout>
    );
  }

  return (
    <DashboardLayout>
      {showFirstLoginModal && (
        <FirstLoginModal onClose={() => setShowFirstLoginModal(false)} />
      )}
      <div className="space-y-6 p-6">
        <div className="flex flex-col sm:flex-row sm:items-center justify-between mb-6">
          <div>
            <h2 className="text-2xl font-bold text-[#215f47] flex items-center gap-2">
              <Grid className="h-6 w-6" />
              Seat Management
            </h2>
            <p className="text-gray-500 mt-1">Assign and manage student seating arrangements</p>
          </div>
          
          {sections.length > 0 && (
            <div className="mt-4 sm:mt-0 min-w-[200px]">
              <Select
                value={selectedSectionId?.toString() || ''}
                onValueChange={(value) => handleSectionChange({ target: { value } } as any)}
              >
                <SelectTrigger className="border-[#215f47]/20 focus:ring-[#215f47]/20 focus:border-[#215f47]">
                  <SelectValue placeholder="Select a section" />
                </SelectTrigger>
                <SelectContent>
                  {sections.map(section => (
                    <SelectItem key={section.id} value={section.id.toString()}>
                      {section.course?.courseCode} - {section.sectionName}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>
          )}
        </div>
        
        {error && (
          <Alert variant="destructive" className="border-red-500/20 mb-6">
            <AlertTriangle className="h-4 w-4" />
            <AlertTitle>Error</AlertTitle>
            <AlertDescription>{error}</AlertDescription>
          </Alert>
        )}
        
        {success && (
          <Alert className="bg-green-50 border-green-500/20 text-green-800 mb-6">
            <Check className="h-4 w-4 text-green-500" />
            <AlertTitle className="text-green-800">Success</AlertTitle>
            <AlertDescription className="text-green-700">{success}</AlertDescription>
          </Alert>
        )}
        
        {sections.length === 0 ? (
          <Card className="border-[#215f47]/20 shadow-sm">
            <CardContent className="py-12 flex flex-col items-center justify-center text-center">
              <Users className="h-12 w-12 text-[#215f47]/30 mb-4" />
              <p className="text-gray-500 max-w-sm">
                You have not been assigned to any sections yet. Please contact an administrator if you believe this is an error.
              </p>
            </CardContent>
          </Card>
        ) : (
          <div className="grid grid-cols-1 gap-6 lg:grid-cols-3">
            {/* Student List */}
            <Card className="border-[#215f47]/20 shadow-sm lg:col-span-1">
              <CardHeader className="pb-3">
                <CardTitle className="text-lg font-medium text-[#215f47] flex items-center gap-2">
                  <Users className="h-5 w-5" />
                  Students
                </CardTitle>
                <CardDescription>
                  Select a student to assign a seat
                </CardDescription>
              </CardHeader>
              <CardContent>
                {sectionStudents.length === 0 ? (
                  <div className="py-8 text-center text-gray-500 border border-dashed border-[#215f47]/20 rounded-md bg-[#215f47]/5">
                    <Users className="w-10 h-10 text-[#215f47]/40 mx-auto mb-3" />
                    <p className="text-sm text-gray-600">No students in this section</p>
                  </div>
                ) : (
                  <div className="max-h-[500px] overflow-y-auto pr-1">
                    <ul className="space-y-1">
                      {sectionStudents.map(student => (
                        <li 
                          key={student.id}
                          className={`cursor-pointer rounded-md p-2.5 transition-colors ${
                            selectedStudentId === student.id 
                              ? 'bg-[#215f47]/10 text-[#215f47] border-l-2 border-[#215f47]'
                              : 'hover:bg-gray-100'
                          }`}
                          onClick={() => handleStudentSelect(student.id)}
                        >
                          <div className="flex items-center">
                            <div className={`mr-3 flex h-8 w-8 items-center justify-center rounded-full ${selectedStudentId === student.id ? 'bg-[#215f47]/10 text-[#215f47]' : 'bg-gray-100 text-gray-500'}`}>
                              <User className="h-4 w-4" />
                            </div>
                            <div>
                              <p className="font-medium">{student.lastName}, {student.firstName}</p>
                              <div className="flex items-center mt-1">
                                <Badge variant="outline" className="text-xs bg-gray-50">
                                  {student.studentPhysicalId}
                                </Badge>
                                {student.year && (
                                  <Badge variant="outline" className="text-xs ml-1 bg-[#215f47]/5 text-[#215f47]">
                                    Year {student.year}
                                  </Badge>
                                )}
                              </div>
                            </div>
                          </div>
                        </li>
                      ))}
                    </ul>
                  </div>
                )}
              </CardContent>
            </Card>
            
            {/* Seat Map */}
            <Card className="border-[#215f47]/20 shadow-sm lg:col-span-2">
              <CardHeader className="pb-3">
                <CardTitle className="text-lg font-medium text-[#215f47] flex items-center gap-2">
                  <Grid className="h-5 w-5" />
                  Seat Map
                </CardTitle>
                <CardDescription>
                  Arrange students in the classroom seating plan
                </CardDescription>
              </CardHeader>
              <CardContent>
                {seatMap ? (
                  <div className="space-y-6">
                    <div className="overflow-x-auto pb-2">
                      <div className="inline-block min-w-full">
                        <div className="mb-3 py-2 text-center border-b border-[#215f47]/10">
                          <Badge variant="outline" className="bg-[#215f47]/5 text-[#215f47] font-normal">
                            Front of Classroom
                          </Badge>
                        </div>
                        
                        <table className="min-w-full border border-[#215f47]/10 rounded-md overflow-hidden">
                          <tbody>
                            {Array.from({ length: seatMap.rows }).map((_, rowIndex) => (
                              <tr key={rowIndex}>
                                {Array.from({ length: seatMap.columns }).map((_, colIndex) => {
                                  const isOccupied = isSeatOccupied(rowIndex, colIndex);
                                  const isSelected = selectedSeat?.row === rowIndex && selectedSeat?.column === colIndex;
                                  const student = findStudentBySeat(rowIndex, colIndex);
                                  
                                  return (
                                    <td 
                                      key={`${rowIndex}-${colIndex}`}
                                      className={`h-16 w-16 border border-[#215f47]/10 p-1 text-center transition-colors ${
                                        isSelected 
                                          ? 'bg-[#215f47]/20 border border-[#215f47]/40' 
                                          : isOccupied 
                                            ? 'bg-[#215f47]/10' 
                                            : 'bg-white hover:bg-gray-50'
                                      } cursor-pointer`}
                                      onClick={() => handleSeatSelect(rowIndex, colIndex)}
                                    >
                                      <div className="flex h-full flex-col items-center justify-center rounded-sm">
                                        {isOccupied && student ? (
                                          <>
                                            <div className="mb-1 h-6 w-6 rounded-full bg-[#215f47]/20 flex items-center justify-center">
                                              <UserCheck className="h-4 w-4 text-[#215f47]" />
                                            </div>
                                            <div className="text-xs font-medium text-[#215f47]/80">
                                              {student.lastName.substring(0, 6)}
                                            </div>
                                          </>
                                        ) : (
                                          <div className="text-xs text-gray-500 flex flex-col items-center">
                                            <MapPin className="h-3 w-3 mb-1 text-gray-400" />
                                            <span>R{rowIndex+1}, C{colIndex+1}</span>
                                          </div>
                                        )}
                                      </div>
                                    </td>
                                  );
                                })}
                              </tr>
                            ))}
                          </tbody>
                        </table>
                      </div>
                    </div>
                    
                    <Card className="border-[#215f47]/10 bg-[#215f47]/5">
                      <CardHeader className="pb-2 pt-4">
                        <CardTitle className="text-sm font-medium text-[#215f47]">Selection Details</CardTitle>
                      </CardHeader>
                      <CardContent className="pb-4">
                        <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                          {selectedStudentId && (
                            <div className="bg-white p-3 rounded-md border border-[#215f47]/10 shadow-sm">
                              <p className="text-xs font-medium text-[#215f47]/70">Selected Student</p>
                              <p className="text-sm font-semibold mt-1 flex items-center">
                                <User className="h-4 w-4 mr-1.5 text-[#215f47]/70" />
                                {getStudentNameById(selectedStudentId)}
                              </p>
                            </div>
                          )}
                          
                          {selectedSeat && (
                            <div className="bg-white p-3 rounded-md border border-[#215f47]/10 shadow-sm">
                              <p className="text-xs font-medium text-[#215f47]/70">Selected Seat</p>
                              <p className="text-sm font-semibold mt-1 flex items-center">
                                <MapPin className="h-4 w-4 mr-1.5 text-[#215f47]/70" />
                                Row {selectedSeat.row + 1}, Column {selectedSeat.column + 1}
                              </p>
                              
                              {isSeatOccupied(selectedSeat.row, selectedSeat.column) && (() => {
                                const occupyingStudent = findStudentBySeat(selectedSeat.row, selectedSeat.column);
                                return (
                                  <div className="mt-2 text-xs text-amber-600 bg-amber-50 p-1.5 rounded-md border border-amber-100 flex items-start">
                                    <AlertTriangle className="mr-1.5 h-3.5 w-3.5 mt-0.5 flex-shrink-0" />
                                    <span>
                                      This seat is already occupied by <span className="font-semibold">
                                        {occupyingStudent ? `${occupyingStudent.lastName}, ${occupyingStudent.firstName}` : 'Unknown Student'}
                                      </span>. Assigning will override the current assignment.
                                    </span>
                                  </div>
                                );
                              })()}
                            </div>
                          )}
                        </div>
                        
                        <div className="mt-4">
                          <Button
                            onClick={assignSeat}
                            disabled={isSaving || !selectedStudentId || !selectedSeat}
                            className="w-full bg-[#215f47] hover:bg-[#215f47]/90 text-white"
                          >
                            {isSaving ? (
                              <>
                                <div className="animate-spin mr-2 h-4 w-4 border-t-2 border-b-2 border-white rounded-full"></div>
                                Saving...
                              </>
                            ) : (
                              <>
                                <Save className="mr-2 h-4 w-4" />
                                Assign Seat
                              </>
                            )}
                          </Button>
                        </div>
                      </CardContent>
                    </Card>
                  </div>
                ) : (
                  <div className="py-12 text-center text-gray-500 border border-dashed border-[#215f47]/20 rounded-md bg-[#215f47]/5">
                    <Grid className="w-12 h-12 text-[#215f47]/40 mx-auto mb-3" />
                    <p className="text-sm text-gray-600">Select a section to view the seat map</p>
                    <p className="text-xs text-gray-500 mt-1">Once selected, you can assign students to seats</p>
                  </div>
                )}
              </CardContent>
            </Card>
          </div>
        )}
      </div>
    </DashboardLayout>
  );
};

export default SeatManagement;
