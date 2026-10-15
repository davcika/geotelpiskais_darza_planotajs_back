-- Iepriekšējais last_done_on, lai "Done" varētu atsaukt (POST /api/tasks/{id}/undo).
-- Glabā tikai vienu soli atpakaļ; pēc atsaukšanas kolonna kļūst NULL.
ALTER TABLE garden_task ADD COLUMN previous_done_on DATE;
