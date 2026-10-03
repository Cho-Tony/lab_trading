ALTER TABLE stocks
    ADD COLUMN data_source VARCHAR(32) NULL,
    ADD COLUMN source_security_id VARCHAR(64) NULL,
    ADD COLUMN security_type VARCHAR(64) NULL,
    ADD COLUMN listing_start_date DATE NULL,
    ADD COLUMN listing_end_date DATE NULL,
    ADD COLUMN delisting_reason VARCHAR(255) NULL;


CREATE INDEX idx_stocks_listing_window
    ON stocks (
               listing_start_date,
               listing_end_date
        );


CREATE INDEX idx_stocks_source_security
    ON stocks (
               data_source,
               source_security_id
        );