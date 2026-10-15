ALTER TABLE sharadar_stock_prices
    MODIFY COLUMN open_price DECIMAL(38, 16) NULL,
    MODIFY COLUMN high_price DECIMAL(38, 16) NULL,
    MODIFY COLUMN low_price DECIMAL(38, 16) NULL,
    MODIFY COLUMN close_price DECIMAL(38, 16) NULL,
    MODIFY COLUMN adjusted_close DECIMAL(38, 16) NULL,
    MODIFY COLUMN unadjusted_close DECIMAL(38, 16) NULL;