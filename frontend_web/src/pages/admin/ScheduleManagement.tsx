import { useState, useEffect } from 'react';
import DashboardLayout from '../../components/ui/layout/DashboardLayout';
import { scheduleApi, type Schedule, type ScheduleCreateDto, type ScheduleUpdateDto, daysOfWeek, scheduleTypes } from '../../lib/api/schedule';
import { sectionApi, type Section } from '../../lib/api/section';
import { Button } from '../../components/ui/button';
import { Plus, Pencil, Trash2, Calendar, Clock, Home } from 'lucide-react';

const ScheduleManagement = () => {
  const [schedules, setSchedules] = useState<Schedule[]>([]);
  const [sections, setSections] = useState<Section[]>([]);
  const [selectedSectionId, setSelectedSectionId] = useState<number | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  
  // Form state for adding/editing schedules
  const [showForm, setShowForm] = useState(false);
  const [formType, setFormType] = useState<'add' | 'edit'>('add');
  const [selectedScheduleId, setSelectedScheduleId] = useState<number | null>(null);
  
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
      <div className="p-6">
        <div className="flex justify-between items-center mb-6">
          <h1 className="text-2xl font-bold">Schedule Management</h1>
          <Button onClick={handleAddNew} className="flex items-center">
            <Plus className="mr-2 h-4 w-4" /> Add Schedule
          </Button>
        </div>
        
        {error && (
          <div className="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded mb-4">
            {error}
            <button 
              className="float-right"
              onClick={() => setError(null)}
            >
              &times;
            </button>
          </div>
        )}
        
        {/* Section selection */}
        <div className="mb-6">
          <label htmlFor="section" className="block text-sm font-medium mb-2">
            Select Section
          </label>
          <select
            id="section"
            className="w-full max-w-md px-3 py-2 border rounded-md"
            value={selectedSectionId || ''}
            onChange={handleSectionChange}
            disabled={isLoading}
          >
            <option value="">Select a section</option>
            {sections.map(section => (
              <option key={section.id} value={section.id}>
                {section.sectionName} - {section.courseId}
              </option>
            ))}
          </select>
        </div>
        
        {/* Schedule Form */}
        {showForm && (
          <div className="bg-white p-6 rounded-md shadow-md mb-6">
            <h2 className="text-xl font-semibold mb-4">
              {formType === 'add' ? 'Add New Schedule' : 'Edit Schedule'}
            </h2>
            
            <form onSubmit={handleSubmit} className="space-y-4">
              {/* Section field (readonly when editing) */}
              <div>
                <label htmlFor="formSection" className="block text-sm font-medium mb-1">
                  Section
                </label>
                <select
                  id="formSection"
                  name="sectionId"
                  value={formData.sectionId || ''}
                  onChange={handleInputChange}
                  className="w-full px-3 py-2 border rounded-md"
                  disabled={formType === 'edit'} // Disable for edit mode
                  required
                >
                  <option value="">Select a section</option>
                  {sections.map(section => (
                    <option key={section.id} value={section.id}>
                      {section.sectionName} - {section.courseId}
                    </option>
                  ))}
                </select>
              </div>
              
              {/* Day of week */}
              <div>
                <label htmlFor="dayOfWeek" className="block text-sm font-medium mb-1">
                  Day of Week
                </label>
                <div className="flex items-center">
                  <Calendar className="mr-2 h-4 w-4 text-gray-500" />
                  <select
                    id="dayOfWeek"
                    name="dayOfWeek"
                    value={formData.dayOfWeek}
                    onChange={handleInputChange}
                    className="w-full px-3 py-2 border rounded-md"
                    required
                  >
                    {daysOfWeek.map(day => (
                      <option key={day.value} value={day.value}>
                        {day.label}
                      </option>
                    ))}
                  </select>
                </div>
              </div>
              
              {/* Time Start */}
              <div>
                <label htmlFor="timeStart" className="block text-sm font-medium mb-1">
                  Start Time
                </label>
                <div className="flex items-center">
                  <Clock className="mr-2 h-4 w-4 text-gray-500" />
                  <input
                    type="time"
                    id="timeStart"
                    name="timeStart"
                    value={formData.timeStart}
                    onChange={handleInputChange}
                    className="w-full px-3 py-2 border rounded-md"
                    required
                  />
                </div>
              </div>
              
              {/* Time End */}
              <div>
                <label htmlFor="timeEnd" className="block text-sm font-medium mb-1">
                  End Time
                </label>
                <div className="flex items-center">
                  <Clock className="mr-2 h-4 w-4 text-gray-500" />
                  <input
                    type="time"
                    id="timeEnd"
                    name="timeEnd"
                    value={formData.timeEnd}
                    onChange={handleInputChange}
                    className="w-full px-3 py-2 border rounded-md"
                    required
                  />
                </div>
              </div>
              
              {/* Room */}
              <div>
                <label htmlFor="room" className="block text-sm font-medium mb-1">
                  Room
                </label>
                <div className="flex items-center">
                  <Home className="mr-2 h-4 w-4 text-gray-500" />
                  <input
                    type="text"
                    id="room"
                    name="room"
                    value={formData.room}
                    onChange={handleInputChange}
                    className="w-full px-3 py-2 border rounded-md"
                    placeholder="Room number or name"
                    required
                  />
                </div>
              </div>
              
              {/* Schedule Type */}
              <div>
                <label htmlFor="scheduleType" className="block text-sm font-medium mb-1">
                  Schedule Type
                </label>
                <select
                  id="scheduleType"
                  name="scheduleType"
                  value={formData.scheduleType}
                  onChange={handleInputChange}
                  className="w-full px-3 py-2 border rounded-md"
                  required
                >
                  {scheduleTypes.map(type => (
                    <option key={type.value} value={type.value}>
                      {type.label}
                    </option>
                  ))}
                </select>
              </div>
              
              {/* Form Actions */}
              <div className="flex justify-end space-x-2 pt-4">
                <Button 
                  type="button" 
                  variant="outline" 
                  onClick={resetForm}
                >
                  Cancel
                </Button>
                <Button type="submit">
                  {formType === 'add' ? 'Add Schedule' : 'Update Schedule'}
                </Button>
              </div>
            </form>
          </div>
        )}
        
        {/* Schedules List */}
        <div className="bg-white rounded-md shadow-md overflow-hidden">
          <table className="min-w-full divide-y divide-gray-200">
            <thead className="bg-gray-50">
              <tr>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Day
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Time
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Room
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Type
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Section
                </th>
                <th className="px-6 py-3 text-right text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Actions
                </th>
              </tr>
            </thead>
            <tbody className="bg-white divide-y divide-gray-200">
              {isLoading ? (
                <tr>
                  <td colSpan={6} className="px-6 py-4 text-center">
                    Loading schedules...
                  </td>
                </tr>
              ) : schedules.length === 0 ? (
                <tr>
                  <td colSpan={6} className="px-6 py-4 text-center">
                    No schedules found. {selectedSectionId ? 'Add a new schedule for this section.' : 'Please select a section.'}
                  </td>
                </tr>
              ) : (
                schedules.map(schedule => (
                  <tr key={schedule.id} className="hover:bg-gray-50">
                    <td className="px-6 py-4 whitespace-nowrap">
                      {getDayName(schedule.dayOfWeek)}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap">
                      {schedule.timeStart} - {schedule.timeEnd}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap">
                      {schedule.room}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap">
                      {getScheduleTypeName(schedule.scheduleType)}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap">
                      {schedule.section.sectionName}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-right">
                      <button
                        onClick={() => handleEdit(schedule.id)}
                        className="text-blue-600 hover:text-blue-900 mr-3"
                      >
                        <Pencil className="h-4 w-4" />
                      </button>
                      <button
                        onClick={() => handleDelete(schedule.id)}
                        className="text-red-600 hover:text-red-900"
                      >
                        <Trash2 className="h-4 w-4" />
                      </button>
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
      </div>
    </DashboardLayout>
  );
};

export default ScheduleManagement;
