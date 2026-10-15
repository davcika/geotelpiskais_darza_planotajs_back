-- Dārza darbi un atgādinājumi (pļaušana, apgriešana, balsināšana u.c.).
-- kind nosaka, kuri lauki ir aizpildīti: ONCE → due_date, EVERY_N_DAYS → interval_days,
-- YEARLY → logs month_day_from..month_day_to formātā 'MM-DD'.
CREATE TABLE garden_task (
    id             BIGSERIAL PRIMARY KEY,
    garden_id      BIGINT       NOT NULL REFERENCES garden (id) ON DELETE CASCADE,
    object_id      BIGINT       REFERENCES garden_object (id) ON DELETE CASCADE,
    title          VARCHAR(100) NOT NULL,
    note           VARCHAR(300),
    kind           VARCHAR(20)  NOT NULL CHECK (kind IN ('ONCE', 'EVERY_N_DAYS', 'YEARLY')),
    due_date       DATE,
    interval_days  INT,
    month_day_from VARCHAR(5),
    month_day_to   VARCHAR(5),
    last_done_on   DATE,
    enabled        BOOLEAN      NOT NULL DEFAULT TRUE,
    is_default     BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at     TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT garden_task_once CHECK (kind <> 'ONCE' OR (due_date IS NOT NULL
        AND interval_days IS NULL AND month_day_from IS NULL AND month_day_to IS NULL)),
    CONSTRAINT garden_task_every CHECK (kind <> 'EVERY_N_DAYS' OR (interval_days BETWEEN 1 AND 3650
        AND due_date IS NULL AND month_day_from IS NULL AND month_day_to IS NULL)),
    CONSTRAINT garden_task_yearly CHECK (kind <> 'YEARLY' OR (due_date IS NULL AND interval_days IS NULL
        AND month_day_from ~ '^(0[1-9]|1[0-2])-(0[1-9]|[12][0-9]|3[01])$'
        AND month_day_to ~ '^(0[1-9]|1[0-2])-(0[1-9]|[12][0-9]|3[01])$'))
);

CREATE INDEX idx_garden_task_garden ON garden_task (garden_id);
