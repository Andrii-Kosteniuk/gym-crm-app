ALTER TABLE trainings
    ALTER COLUMN duration_minutes
        TYPE BIGINT
        USING duration_minutes::BIGINT;