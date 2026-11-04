ALTER TABLE sharadar_fundamentals
DROP PRIMARY KEY,
    ADD PRIMARY KEY (
        stock_id,
        dimension,
        calendar_date,
        filing_date
    );