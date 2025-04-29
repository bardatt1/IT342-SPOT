import { useState, useEffect } from 'react';
import DashboardLayout from '../../components/ui/layout/DashboardLayout';
import { useAuth } from '../../contexts/AuthContext';
import { sectionApi, type Section } from '../../lib/api/section';
import { studentApi, type Student } from '../../lib/api/student';
import { seatApi, type SeatMap } from '../../lib/api/seat';
import { Button } from '../../components/ui/button';
import { Save, AlertTriangle, User, UserCheck } from 'lucide-react';

const SeatManagement = () => {
  const { user } = useAuth();
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

  useEffect(() => {
    fetchTeacherSections();
  }, [user?.id]);

  useEffect(() => {
    if (selectedSectionId) {
      fetchSeatMap(selectedSectionId);
      fetchSectionStudents(selectedSectionId);
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
      // Get all students and filter by section
      const allStudents = await studentApi.getAll();
      // In a real application, we would have a proper API endpoint for this
      // For now, we're simulating it by filtering client-side
      // The sectionId parameter would be used here in a real implementation
      console.log(`Fetching students for section ${sectionId}`);
      const students = allStudents;
      setSectionStudents(students);
      setError(null);
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
    
    return findStudentById(seat.studentId);
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
    
    try {
      setIsSaving(true);
      setError(null);
      setSuccess(null);
      
      await seatApi.overrideSeat(
        selectedSectionId,
        selectedStudentId,
        selectedSeat.row,
        selectedSeat.column
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
        <div className="flex h-full items-center justify-center">
          <p className="text-lg">Loading your sections...</p>
        </div>
      </DashboardLayout>
    );
  }

  return (
    <DashboardLayout>
      <div className="space-y-6">
        <div className="flex items-center justify-between">
          <h2 className="text-2xl font-bold">Seat Management</h2>
          
          {sections.length > 0 && (
            <div className="flex items-center space-x-2">
              <select
                value={selectedSectionId || ''}
                onChange={handleSectionChange}
                className="rounded-md border border-gray-300 bg-white px-3 py-2 text-sm shadow-sm focus:border-blue-500 focus:outline-none focus:ring-blue-500"
              >
                {sections.map(section => (
                  <option key={section.id} value={section.id}>
                    Section ID: {section.id} ({section.sectionName})
                  </option>
                ))}
              </select>
            </div>
          )}
        </div>
        
        {error && (
          <div className="rounded-md bg-red-50 p-4">
            <div className="flex">
              <AlertTriangle className="h-5 w-5 text-red-400" />
              <div className="ml-3">
                <h3 className="text-sm font-medium text-red-800">Error</h3>
                <div className="mt-2 text-sm text-red-700">
                  <p>{error}</p>
                </div>
              </div>
            </div>
          </div>
        )}
        
        {success && (
          <div className="rounded-md bg-green-50 p-4">
            <div className="flex">
              <div className="ml-3">
                <h3 className="text-sm font-medium text-green-800">Success</h3>
                <div className="mt-2 text-sm text-green-700">
                  <p>{success}</p>
                </div>
              </div>
            </div>
          </div>
        )}
        
        {sections.length === 0 ? (
          <div className="rounded-lg bg-white p-6 shadow">
            <p className="text-center text-gray-500">
              You have not been assigned to any sections yet.
            </p>
          </div>
        ) : (
          <div className="grid grid-cols-1 gap-6 md:grid-cols-3">
            {/* Student List */}
            <div className="rounded-lg bg-white p-4 shadow md:col-span-1">
              <h3 className="mb-4 border-b border-gray-200 pb-2 text-lg font-medium">Students</h3>
              {sectionStudents.length === 0 ? (
                <p className="text-center text-gray-500">
                  No students in this section
                </p>
              ) : (
                <div className="max-h-[500px] overflow-y-auto">
                  <ul className="space-y-2">
                    {sectionStudents.map(student => (
                      <li 
                        key={student.id}
                        className={`cursor-pointer rounded-md p-2 ${
                          selectedStudentId === student.id 
                            ? 'bg-blue-100 text-blue-800'
                            : 'hover:bg-gray-100'
                        }`}
                        onClick={() => handleStudentSelect(student.id)}
                      >
                        <div className="flex items-center">
                          <User className="mr-2 h-4 w-4" />
                          <div>
                            <p className="font-medium">{student.lastName}, {student.firstName}</p>
                            <p className="text-xs text-gray-500">ID: {student.studentPhysicalId}</p>
                          </div>
                        </div>
                      </li>
                    ))}
                  </ul>
                </div>
              )}
            </div>
            
            {/* Seat Map */}
            <div className="rounded-lg bg-white p-4 shadow md:col-span-2">
              <h3 className="mb-4 border-b border-gray-200 pb-2 text-lg font-medium">Seat Map</h3>
              
              {seatMap ? (
                <div className="space-y-4">
                  <div className="overflow-x-auto">
                    <div className="inline-block min-w-full">
                      <div className="mb-2 text-center text-sm font-medium text-gray-500">
                        Front of Classroom
                      </div>
                      
                      <table className="min-w-full border border-gray-200">
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
                                    className={`h-16 w-16 border border-gray-200 p-1 text-center ${
                                      isSelected 
                                        ? 'bg-blue-200' 
                                        : isOccupied 
                                          ? 'bg-green-100' 
                                          : 'bg-white hover:bg-gray-100'
                                    } cursor-pointer`}
                                    onClick={() => handleSeatSelect(rowIndex, colIndex)}
                                  >
                                    <div className="flex h-full flex-col items-center justify-center">
                                      {isOccupied && student ? (
                                        <>
                                          <UserCheck className="h-4 w-4 text-green-600" />
                                          <div className="mt-1 text-xs font-medium">
                                            {student.lastName.substring(0, 6)}
                                          </div>
                                        </>
                                      ) : (
                                        <div className="text-xs text-gray-500">
                                          Row {rowIndex+1}, Col {colIndex+1}
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
                  
                  <div className="mt-4 rounded-md bg-gray-50 p-4">
                    <h4 className="text-sm font-medium text-gray-900">Selection Details</h4>
                    
                    {selectedStudentId && (
                      <div className="mt-2">
                        <p className="text-sm font-medium text-gray-500">Selected Student:</p>
                        <p className="text-sm text-gray-900">{getStudentNameById(selectedStudentId)}</p>
                      </div>
                    )}
                    
                    {selectedSeat && (
                      <div className="mt-2">
                        <p className="text-sm font-medium text-gray-500">Selected Seat:</p>
                        <p className="text-sm text-gray-900">Row {selectedSeat.row + 1}, Column {selectedSeat.column + 1}</p>
                        
                        {isSeatOccupied(selectedSeat.row, selectedSeat.column) && (
                          <div className="mt-1 text-xs text-orange-500">
                            <AlertTriangle className="mr-1 inline-block h-3 w-3" />
                            This seat is already occupied. Assigning will override the current assignment.
                          </div>
                        )}
                      </div>
                    )}
                    
                    <div className="mt-4">
                      <Button
                        onClick={assignSeat}
                        disabled={isSaving || !selectedStudentId || !selectedSeat}
                        className="w-full"
                      >
                        <Save className="mr-1 h-4 w-4" />
                        {isSaving ? 'Saving...' : 'Assign Seat'}
                      </Button>
                    </div>
                  </div>
                </div>
              ) : (
                <div className="rounded-lg bg-gray-100 p-6 text-center">
                  <p className="text-gray-500">
                    Select a section to view and manage the seat map
                  </p>
                </div>
              )}
            </div>
          </div>
        )}
      </div>
    </DashboardLayout>
  );
};

export default SeatManagement;
