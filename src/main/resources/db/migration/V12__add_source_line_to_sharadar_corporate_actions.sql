ALTER TABLE sharadar_corporate_actions
    ADD COLUMN source_line BIGINT NOT NULL AFTER id,
    ADD CONSTRAINT uq_sharadar_actions_source_line
        UNIQUE (source_line);