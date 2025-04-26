-- Update teacher-section relationship from OneToOne to OneToMany

-- No schema change is needed as the relationship is maintained via the teacher_id foreign key in sections table
-- We just need to remove any unique constraint that might enforce the one-to-one relationship

-- Using straightforward MySQL syntax to drop an index if it exists
-- This works with MySQL 5.7+ syntax using DROP INDEX IF EXISTS
DROP INDEX IF EXISTS uk_teacher_id ON sections;

-- Also try to drop any other potential unique constraints on teacher_id
-- The index name might be different in some implementations
DROP INDEX IF EXISTS teacher_id ON sections;
DROP INDEX IF EXISTS uk_sections_teacher_id ON sections;

-- This is the specific constraint name from the error message
DROP INDEX IF EXISTS UK_3mn09valqrfogicdih2262mqm ON sections;

-- Comment: One teacher can teach multiple sections - this relationship is now one-to-many
