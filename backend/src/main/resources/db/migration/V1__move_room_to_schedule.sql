-- V1__move_room_to_schedule.sql
--
-- Migration script to move room field from section table to schedule table
-- and add sectionName field to sections table
-- This reflects the code changes where:
-- 1. room is now a property of Schedule instead of Section
-- 2. sectionName is now a required property of Section

-- First add the room column to the schedules table
ALTER TABLE schedules ADD COLUMN room VARCHAR(50);

-- Copy room data from sections to corresponding schedules
-- This will ensure that existing room data is not lost during migration
UPDATE schedules s
SET s.room = (
    SELECT sec.room
    FROM sections sec
    WHERE sec.id = s.section_id
);

-- Make room NOT NULL after data is migrated
ALTER TABLE schedules MODIFY COLUMN room VARCHAR(50) NOT NULL;

-- Remove the room column from the sections table
ALTER TABLE sections DROP COLUMN room;

-- Add sectionName column to the sections table
ALTER TABLE sections ADD COLUMN section_name VARCHAR(100) NOT NULL DEFAULT 'Section';

-- Set a default value based on course name for existing sections
UPDATE sections s
SET s.section_name = CONCAT((SELECT c.course_name FROM courses c WHERE c.id = s.course_id), ' - Section ', s.id)
WHERE s.section_name = 'Section';

-- Remove the default constraint after data is migrated
ALTER TABLE sections MODIFY COLUMN section_name VARCHAR(100) NOT NULL;

-- Add unique constraint to ensure sections cannot have duplicate courseId and sectionName
ALTER TABLE sections ADD CONSTRAINT uk_course_section_name UNIQUE (course_id, section_name);
