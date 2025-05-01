import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import DashboardLayout from '../../components/ui/layout/DashboardLayout';
import { scheduleApi, type Schedule, type ScheduleCreateDto, type ScheduleUpdateDto, daysOfWeek, scheduleTypes } from '../../lib/api/schedule';
import { sectionApi, type Section } from '../../lib/api/section';

// UI Components
import { Button } from '../../components/ui/button';
import { Input } from '../../components/ui/input';
import { Label } from '../../components/ui/label';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '../../components/ui/card';
import { Alert, AlertDescription, AlertTitle } from '../../components/ui/alert';
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '../../components/ui/table';
import { Select, SelectContent, SelectGroup, SelectItem, SelectLabel, SelectTrigger, SelectValue } from '../../components/ui/select';
import { Badge } from '../../components/ui/badge';
import { Tabs, TabsList, TabsTrigger } from '../../components/ui/tabs';

// Icons
import { 
  Plus, 
  Pencil, 
  Trash2, 
  Calendar, 
  Clock, 
  Home, 
  CalendarDays, 
  Timer, 
  AlertTriangle, 
  X, 
  Check,
  BookOpen,
  Building2,
  ListPlus,
  ClipboardList
} from 'lucide-react';

const ScheduleManagement = () => {
  const navigate = useNavigate();
  const [schedules, setSchedules] = useState<Schedule[]>([]);
  const [sections, setSections] = useState<Section[]>([]);
  const [selectedSectionId, setSelectedSectionId] = useState<number | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  
  // Form state for adding/editing schedules
  const [showForm, setShowForm] = useState(false);
  const [formType, setFormType] = useState<'add' | 'edit'>('add');
  const [selectedScheduleId, setSelectedScheduleId] = useState<number | null>(null);
  
  // Handle tab change
  const handleTabChange = (value: string) => {
    if (value === "sections") {
      navigate('/admin/sections');
    }
  };
  
  const [formData, setFormData] = useState<{
    sectionId: number | null;
    dayOfWeek: number;
    timeStart: string;
    timeEnd: string;
    room: string;
    scheduleType: string;
  }>({
    sectionId: null,
    dayOfWeek: 1,
    timeStart: '08:00',
    timeEnd: '09:00',
    room: '',
    scheduleType: 'LEC'
  });

  useEffect(() => {
    fetchSections();
  }, []);

  useEffect(() => {
    if (selectedSectionId) {
      fetchSchedules(selectedSectionId);
    }
  }, [selectedSectionId]);

  const fetchSections = async () => {
    try {
      setIsLoading(true);
      const data = await sectionApi.getAllSections();
      setSections(data);
      
      // Select the first section by default if available
      if (data.length > 0 && !selectedSectionId) {
        setSelectedSectionId(data[0].id);
      }
    } catch (error) {
      console.error('Error fetching sections:', error);
      setError('Failed to load sections. Please try again later.');
    } finally {
      setIsLoading(false);
    }
  };

  const fetchSchedules = async (sectionId: number) => {
    try {
      setIsLoading(true);
      const data = await scheduleApi.getBySectionId(sectionId);
      setSchedules(data);
    } catch (error) {
      console.error('Error fetching schedules:', error);
      setError('Failed to load schedules. Please try again later.');
    } finally {
      setIsLoading(false);
    }
  };

  const resetForm = () => {
    setShowForm(false);
    setFormType('add');
    setSelectedScheduleId(null);
    setFormData({
      sectionId: selectedSectionId,
      dayOfWeek: 1,
      timeStart: '08:00',
      timeEnd: '09:00',
      room: '',
      scheduleType: 'LEC'
    });
  };

  const handleSectionChange = (e: React.ChangeEvent<HTMLSelectElement>) => {
    const sectionId = parseInt(e.target.value);
    setSelectedSectionId(sectionId);
    
    // Update the form data if it's shown
    if (showForm && formType === 'add') {
      setFormData({
        ...formData,
        sectionId
      });
    }
  };

  const handleAddNew = () => {
    setShowForm(true);
    setFormType('add');
    setFormData({
      ...formData,
      sectionId: selectedSectionId
    });
  };

  const handleEdit = (id: number) => {
    setShowForm(true);
    setFormType('edit');
    setSelectedScheduleId(id);
    
    // Populate form data based on selected schedule
    const schedule = schedules.find(s => s.id === id);
    if (schedule) {
      setFormData({
        sectionId: schedule.section.id,
        dayOfWeek: schedule.dayOfWeek,
        timeStart: schedule.timeStart,
        timeEnd: schedule.timeEnd,
        room: schedule.room,
        scheduleType: schedule.scheduleType
      });
    }
  };

  const handleDelete = async (id: number) => {
    if (!window.confirm('Are you sure you want to delete this schedule?')) {
      return;
    }
    
    try {
      await scheduleApi.delete(id);
      setSchedules(schedules.filter(s => s.id !== id));
    } catch (error) {
      console.error('Error deleting schedule:', error);
      setError('Failed to delete schedule. Please try again later.');
    }
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    
    if (!formData.sectionId) {
      setError('Please select a section');
      return;
    }
    
    try {
      if (formType === 'edit' && selectedScheduleId) {
        const scheduleUpdateData: ScheduleUpdateDto = {
          dayOfWeek: formData.dayOfWeek,
          timeStart: formData.timeStart,
          timeEnd: formData.timeEnd,
          room: formData.room,
          scheduleType: formData.scheduleType
        };
        
        const updatedSchedule = await scheduleApi.update(selectedScheduleId, scheduleUpdateData);
        if (updatedSchedule) {
          setSchedules(schedules.map(s => s.id === selectedScheduleId ? updatedSchedule : s));
        }
      } else {
        const scheduleCreateData: ScheduleCreateDto = {
          sectionId: formData.sectionId,
          dayOfWeek: formData.dayOfWeek,
          timeStart: formData.timeStart,
          timeEnd: formData.timeEnd,
          room: formData.room,
          scheduleType: formData.scheduleType
        };
        
        const newSchedule = await scheduleApi.create(scheduleCreateData);
        if (newSchedule) {
          setSchedules([...schedules, newSchedule]);
        }
      }
      
      resetForm();
    } catch (error) {
      console.error('Error saving schedule:', error);
      setError('Failed to save schedule. Please try again later.');
    }
  };

  const handleInputChange = (e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>) => {
    const { name, value } = e.target;
    
    setFormData({
      ...formData,
      [name]: name === 'dayOfWeek' ? parseInt(value) : value
    });
  };
  
  const getDayName = (dayOfWeek: number) => {
    const day = daysOfWeek.find(d => d.value === dayOfWeek);
    return day ? day.label : 'Unknown';
  };
  
  const getScheduleTypeName = (type: string) => {
    const scheduleType = scheduleTypes.find(t => t.value === type);
    return scheduleType ? scheduleType.label : type;
  };

  return (
    <DashboardLayout>
      <div className="space-y-6 p-6">
        <div className="flex flex-col justify-between sm:flex-row sm:items-center border-b border-[#215f47]/10 pb-4">
          <div>
            <h2 className="text-2xl font-bold text-[#215f47] flex items-center gap-2">
              <CalendarDays className="h-6 w-6" />
              Sections & Schedules
            </h2>
            <p className="text-gray-500 mt-1">Manage academic sections and their class schedules</p>
          </div>
          <div>
            <Button 
              onClick={handleAddNew} 
              className="bg-[#215f47] hover:bg-[#215f47]/90 text-white"
              disabled={!selectedSectionId}
            >
              <Plus className="mr-2 h-4 w-4" />
              Add Schedule
            </Button>
          </div>
        </div>
        
        {/* Tabs for switching between sections and schedules */}
        <Tabs defaultValue="schedules" className="w-full" onValueChange={handleTabChange}>
          <TabsList className="mb-4 grid w-full grid-cols-2 bg-[#f8f9fa]">
            <TabsTrigger 
              value="sections" 
              className="data-[state=active]:bg-[#215f47] data-[state=active]:text-white"
            >
              <BookOpen className="mr-2 h-4 w-4" />
              Sections Management
            </TabsTrigger>
            <TabsTrigger 
              value="schedules" 
              className="data-[state=active]:bg-[#215f47] data-[state=active]:text-white"
            >
              <CalendarDays className="mr-2 h-4 w-4" />
              Schedules Management
            </TabsTrigger>
          </TabsList>
        </Tabs>
        
        {error && (
          <Alert variant="destructive" className="border-red-500/20 mb-6">
            <div className="flex justify-between w-full">
              <div className="flex items-start gap-2">
                <AlertTriangle className="h-4 w-4 mt-0.5" />
                <div>
                  <AlertTitle>Error</AlertTitle>
                  <AlertDescription>{error}</AlertDescription>
                </div>
              </div>
              <Button 
                variant="ghost" 
                size="icon" 
                className="h-6 w-6 rounded-full" 
                onClick={() => setError(null)}
              >
                <X className="h-4 w-4" />
              </Button>
            </div>
          </Alert>
        )}
        
        {/* Section selection */}
        <Card className="border-[#215f47]/20 shadow-sm mb-6">
          <CardHeader className="pb-3">
            <CardTitle className="text-base text-[#215f47]">Select Section</CardTitle>
            <CardDescription>Choose a section to view or add schedules</CardDescription>
          </CardHeader>
          <CardContent>
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div className="space-y-1.5 flex-1">
                <Label htmlFor="section" className="text-[#215f47]">
                  <div className="flex items-center gap-1.5">
                    <BookOpen className="h-3.5 w-3.5" />
                    Active Sections
                  </div>
                </Label>
                <Select
                  value={selectedSectionId?.toString() || ''}
                  onValueChange={(value) => handleSectionChange({ target: { value } } as any)}
                  disabled={isLoading}
                >
                  <SelectTrigger className="border-[#215f47]/20 focus-visible:ring-[#215f47]/20 focus-visible:ring-offset-1 focus-visible:ring-offset-[#215f47]/90 w-full">
                    <SelectValue placeholder="Select a section" />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectGroup>
                      <SelectLabel>Sections</SelectLabel>
                      {sections.length === 0 ? (
                        <SelectItem value="empty" disabled>
                          No sections available
                        </SelectItem>
                      ) : (
                        sections.map(section => (
                          <SelectItem key={section.id} value={section.id.toString()} className="cursor-pointer">
                            <span className="flex items-center gap-2">
                              <Badge variant="outline" className="bg-[#215f47]/5 text-[#215f47] border-[#215f47]/20 font-medium"> 
                                {section.course?.courseCode || 'N/A'}
                              </Badge>
                              {section.sectionName}
                            </span>
                          </SelectItem>
                        ))
                      )}
                    </SelectGroup>
                  </SelectContent>
                </Select>
              </div>
              
              {selectedSectionId && (
                <div className="flex flex-col justify-center space-y-1 rounded-md bg-[#215f47]/5 p-3 border border-[#215f47]/10">
                  <div className="flex items-center text-[#215f47] font-medium text-sm mb-0.5">
                    <ClipboardList className="h-3.5 w-3.5 mr-1.5" />
                    Selected Section Details
                  </div>
                  {sections.find(s => s.id === selectedSectionId) ? (
                    <div className="text-sm text-gray-600">
                      <p><span className="font-medium">Name:</span> {sections.find(s => s.id === selectedSectionId)?.sectionName}</p>
                      <p><span className="font-medium">Course:</span> {sections.find(s => s.id === selectedSectionId)?.course?.courseName || 'N/A'}</p>
                    </div>
                  ) : (
                    <p className="text-sm text-gray-500 italic">No details available</p>
                  )}
                </div>
              )}
            </div>
          </CardContent>
        </Card>
        
        {/* Schedule Form */}
        {showForm && (
          <Card className="border-[#215f47]/20 shadow-sm mb-6">
            <CardHeader className="pb-3">
              <CardTitle className="text-[#215f47] flex items-center gap-2">
                {formType === 'add' ? <ListPlus className="h-5 w-5" /> : <Pencil className="h-5 w-5" />}
                {formType === 'add' ? 'Add New Schedule' : 'Edit Schedule'}
              </CardTitle>
              <CardDescription>
                {formType === 'add' ? 'Create a new schedule for a section' : 'Modify existing schedule details'}
              </CardDescription>
            </CardHeader>
            
            <CardContent>
              <form onSubmit={handleSubmit} className="space-y-5">
                {/* Section field (readonly when editing) */}
                <div className="space-y-1.5">
                  <Label htmlFor="formSection" className="text-[#215f47]">
                    <div className="flex items-center gap-1.5">
                      <BookOpen className="h-3.5 w-3.5" />
                      Section
                    </div>
                  </Label>
                  <Select
                    value={formData.sectionId?.toString() || ''}
                    onValueChange={(value) => setFormData(prev => ({ ...prev, sectionId: parseInt(value) }))}
                    disabled={formType === 'edit'}
                  >
                    <SelectTrigger className="border-[#215f47]/20 focus-visible:ring-[#215f47]/20 focus-visible:ring-offset-1 focus-visible:ring-offset-[#215f47]/90 w-full">
                      <SelectValue placeholder="Select a section" />
                    </SelectTrigger>
                    <SelectContent>
                      <SelectGroup>
                        <SelectLabel>Sections</SelectLabel>
                        {sections.length === 0 ? (
                          <SelectItem value="empty" disabled>
                            No sections available
                          </SelectItem>
                        ) : (
                          sections.map(section => (
                            <SelectItem key={section.id} value={section.id.toString()} className="cursor-pointer">
                              <span className="flex items-center gap-2">
                                <Badge variant="outline" className="bg-[#215f47]/5 text-[#215f47] border-[#215f47]/20 font-medium"> 
                                  {section.course?.courseCode || 'N/A'}
                                </Badge>
                                {section.sectionName}
                              </span>
                            </SelectItem>
                          ))
                        )}
                      </SelectGroup>
                    </SelectContent>
                  </Select>
                </div>
                
                <div className="grid grid-cols-1 md:grid-cols-2 gap-5">
                  {/* Day of week */}
                  <div className="space-y-1.5">
                    <Label htmlFor="dayOfWeek" className="text-[#215f47]">
                      <div className="flex items-center gap-1.5">
                        <Calendar className="h-3.5 w-3.5" />
                        Day of Week
                      </div>
                    </Label>
                    <Select
                      value={formData.dayOfWeek.toString()}
                      onValueChange={(value) => setFormData(prev => ({ ...prev, dayOfWeek: parseInt(value) }))}
                    >
                      <SelectTrigger className="border-[#215f47]/20 focus-visible:ring-[#215f47]/20 focus-visible:ring-offset-1 focus-visible:ring-offset-[#215f47]/90 w-full">
                        <SelectValue />
                      </SelectTrigger>
                      <SelectContent>
                        <SelectGroup>
                          {daysOfWeek.map(day => (
                            <SelectItem key={day.value} value={day.value.toString()}>
                              {day.label}
                            </SelectItem>
                          ))}
                        </SelectGroup>
                      </SelectContent>
                    </Select>
                  </div>
                  
                  {/* Schedule Type */}
                  <div className="space-y-1.5">
                    <Label htmlFor="scheduleType" className="text-[#215f47]">
                      <div className="flex items-center gap-1.5">
                        <ClipboardList className="h-3.5 w-3.5" />
                        Schedule Type
                      </div>
                    </Label>
                    <Select
                      value={formData.scheduleType}
                      onValueChange={(value) => setFormData(prev => ({ ...prev, scheduleType: value }))}
                    >
                      <SelectTrigger className="border-[#215f47]/20 focus-visible:ring-[#215f47]/20 focus-visible:ring-offset-1 focus-visible:ring-offset-[#215f47]/90 w-full">
                        <SelectValue />
                      </SelectTrigger>
                      <SelectContent>
                        <SelectGroup>
                          {scheduleTypes.map(type => (
                            <SelectItem key={type.value} value={type.value}>
                              {type.label}
                            </SelectItem>
                          ))}
                        </SelectGroup>
                      </SelectContent>
                    </Select>
                  </div>
                </div>
                
                <div className="grid grid-cols-1 md:grid-cols-2 gap-5">
                  {/* Time Start */}
                  <div className="space-y-1.5">
                    <Label htmlFor="timeStart" className="text-[#215f47]">
                      <div className="flex items-center gap-1.5">
                        <Timer className="h-3.5 w-3.5" />
                        Start Time
                      </div>
                    </Label>
                    <div className="relative">
                      <Input
                        type="time"
                        id="timeStart"
                        name="timeStart"
                        value={formData.timeStart}
                        onChange={handleInputChange}
                        className="border-[#215f47]/20 focus-visible:ring-[#215f47]/20 focus-visible:ring-offset-1 focus-visible:ring-offset-[#215f47]/90 pl-9"
                        required
                      />
                      <Clock className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-[#215f47]/60" />
                    </div>
                  </div>
                  
                  {/* Time End */}
                  <div className="space-y-1.5">
                    <Label htmlFor="timeEnd" className="text-[#215f47]">
                      <div className="flex items-center gap-1.5">
                        <Timer className="h-3.5 w-3.5" />
                        End Time
                      </div>
                    </Label>
                    <div className="relative">
                      <Input
                        type="time"
                        id="timeEnd"
                        name="timeEnd"
                        value={formData.timeEnd}
                        onChange={handleInputChange}
                        className="border-[#215f47]/20 focus-visible:ring-[#215f47]/20 focus-visible:ring-offset-1 focus-visible:ring-offset-[#215f47]/90 pl-9"
                        required
                      />
                      <Clock className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-[#215f47]/60" />
                    </div>
                  </div>
                </div>
                
                {/* Room */}
                <div className="space-y-1.5">
                  <Label htmlFor="room" className="text-[#215f47]">
                    <div className="flex items-center gap-1.5">
                      <Building2 className="h-3.5 w-3.5" />
                      Room Location
                    </div>
                  </Label>
                  <div className="relative">
                    <Input
                      type="text"
                      id="room"
                      name="room"
                      value={formData.room}
                      onChange={handleInputChange}
                      className="border-[#215f47]/20 focus-visible:ring-[#215f47]/20 focus-visible:ring-offset-1 focus-visible:ring-offset-[#215f47]/90 pl-9"
                      placeholder="Room number or name (e.g. 101, Lab A)"
                      required
                    />
                    <Home className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-[#215f47]/60" />
                  </div>
                </div>
                
                {/* Form Actions */}
                <div className="flex justify-end space-x-3 pt-2">
                  <Button 
                    variant="outline" 
                    type="button" 
                    onClick={resetForm}
                    className="border-[#215f47]/20 text-[#215f47] hover:bg-[#215f47]/5 hover:text-[#215f47] flex items-center"
                  >
                    <X className="mr-2 h-4 w-4" />
                    Cancel
                  </Button>
                  <Button 
                    type="submit"
                    className="bg-[#215f47] hover:bg-[#215f47]/90 flex items-center"
                  >
                    {formType === 'add' ? (
                      <>
                        <ListPlus className="mr-2 h-4 w-4" />
                        Add Schedule
                      </>
                    ) : (
                      <>
                        <Check className="mr-2 h-4 w-4" />
                        Update Schedule
                      </>
                    )}
                  </Button>
                </div>
              </form>
            </CardContent>
          </Card>
        )}
        
        {/* Schedules List */}
        <Card className="border-[#215f47]/20 shadow-sm">
          <CardHeader className="pb-3">
            <CardTitle className="text-base text-[#215f47]">Schedule List</CardTitle>
            <CardDescription>
              {selectedSectionId 
                ? `Schedules for ${sections.find(s => s.id === selectedSectionId)?.sectionName || 'selected section'}` 
                : 'Select a section to view schedules'}
            </CardDescription>
          </CardHeader>

          <CardContent>
            {isLoading ? (
              <div className="flex h-40 items-center justify-center">
                <div className="flex flex-col items-center space-y-4 text-center">
                  <div className="animate-spin text-[#215f47]">
                    <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                      <path d="M21 12a9 9 0 1 1-6.219-8.56"/>
                    </svg>
                  </div>
                  <p className="text-gray-500">Loading schedules...</p>
                </div>
              </div>
            ) : schedules.length === 0 ? (
              <div className="flex flex-col items-center justify-center py-12 text-center">
                <AlertTriangle className="h-12 w-12 text-amber-500/70 mb-4" />
                <h3 className="text-lg font-medium text-gray-700 mb-1">No Schedules Found</h3>
                <p className="text-gray-500 max-w-sm mb-6">
                  {selectedSectionId 
                    ? 'There are no schedules for this section. Click the "Add Schedule" button to create one.' 
                    : 'Please select a section to view or add schedules.'}
                </p>
                {selectedSectionId && (
                  <Button 
                    onClick={handleAddNew} 
                    className="bg-[#215f47] hover:bg-[#215f47]/90 flex items-center"
                  >
                    <Plus className="mr-2 h-4 w-4" />
                    Add Schedule
                  </Button>
                )}
              </div>
            ) : (
              <div className="overflow-x-auto">
                <Table>
                  <TableHeader className="bg-[#215f47]/5">
                    <TableRow>
                      <TableHead className="text-[#215f47] font-medium">
                        Day
                      </TableHead>
                      <TableHead className="text-[#215f47] font-medium">
                        Time
                      </TableHead>
                      <TableHead className="text-[#215f47] font-medium">
                        Room
                      </TableHead>
                      <TableHead className="text-[#215f47] font-medium">
                        Type
                      </TableHead>
                      <TableHead className="text-[#215f47] font-medium">
                        Section
                      </TableHead>
                      <TableHead className="text-[#215f47] font-medium">
                        Course
                      </TableHead>
                      <TableHead className="text-[#215f47] font-medium text-right">
                        Actions
                      </TableHead>
                    </TableRow>
                  </TableHeader>
                  <TableBody>
                    {schedules.map(schedule => (
                      <TableRow key={schedule.id} className="hover:bg-[#215f47]/5">
                        <TableCell className="font-medium">
                          <Badge variant="outline" className="bg-[#215f47]/5 text-[#215f47] border-[#215f47]/20 font-medium">
                            {getDayName(schedule.dayOfWeek)}
                          </Badge>
                        </TableCell>
                        <TableCell>
                          <div className="flex items-center">
                            <Clock className="h-3.5 w-3.5 mr-1.5 text-[#215f47]/60" />
                            <span>{schedule.timeStart} - {schedule.timeEnd}</span>
                          </div>
                        </TableCell>
                        <TableCell>
                          <div className="flex items-center">
                            <Home className="h-3.5 w-3.5 mr-1.5 text-[#215f47]/60" />
                            <span>{schedule.room}</span>
                          </div>
                        </TableCell>
                        <TableCell>
                          <Badge className="bg-[#215f47]/10 text-[#215f47] hover:bg-[#215f47]/20 border-0">
                            {getScheduleTypeName(schedule.scheduleType)}
                          </Badge>
                        </TableCell>
                        <TableCell className="font-medium">
                          {sections.find(s => s.id === schedule.section.id)?.sectionName || schedule.section.sectionName || 'N/A'}
                        </TableCell>
                        <TableCell>
                          <Badge variant="outline" className="bg-[#215f47]/5 text-[#215f47] border-[#215f47]/20">
                            {sections.find(s => s.id === schedule.section.id)?.course?.courseCode || 'No course'}
                          </Badge>
                        </TableCell>
                        <TableCell className="text-right space-x-1">
                          <Button
                            variant="ghost"
                            size="icon"
                            onClick={() => handleEdit(schedule.id)}
                            className="h-8 w-8 text-[#215f47] hover:bg-[#215f47]/10 hover:text-[#215f47]"
                          >
                            <Pencil className="h-4 w-4" />
                          </Button>
                          <Button
                            variant="ghost"
                            size="icon"
                            onClick={() => handleDelete(schedule.id)}
                            className="h-8 w-8 text-red-500 hover:bg-red-50 hover:text-red-600"
                          >
                            <Trash2 className="h-4 w-4" />
                          </Button>
                        </TableCell>
                      </TableRow>
                    ))}
                  </TableBody>
                </Table>
              </div>
            )}
          </CardContent>
        </Card>
      </div>
    </DashboardLayout>
  );
};

export default ScheduleManagement;
