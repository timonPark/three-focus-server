ALTER TABLE schedules ADD COLUMN date DATE;
UPDATE schedules s SET date = (SELECT t.date FROM todos t WHERE t.id = s.todo_id);
ALTER TABLE schedules ALTER COLUMN date SET NOT NULL;
ALTER TABLE schedules ADD COLUMN end_time TIME;
