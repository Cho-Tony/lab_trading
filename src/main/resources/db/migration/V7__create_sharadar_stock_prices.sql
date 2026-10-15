CREATE TABLE sharadar_stock_prices (

                                       stock_id BIGINT NOT NULL,

                                       trade_date DATE NOT NULL,

                                       open_price DECIMAL(20, 8) NULL,

                                       high_price DECIMAL(20, 8) NULL,

                                       low_price DECIMAL(20, 8) NULL,

                                       close_price DECIMAL(20, 8) NULL,

                                       volume DECIMAL(24, 4) NULL,

                                       adjusted_close DECIMAL(20, 8) NULL,

                                       unadjusted_close DECIMAL(20, 8) NULL,

                                       source_last_updated DATE NULL,

                                       created_at TIMESTAMP NOT NULL
                                           DEFAULT CURRENT_TIMESTAMP,

                                       PRIMARY KEY (
                                                    stock_id,
                                                    trade_date
                                           ),

                                       CONSTRAINT fk_sharadar_stock_prices_stock
                                           FOREIGN KEY (stock_id)
                                               REFERENCES stocks (id)
);


CREATE INDEX idx_sharadar_stock_prices_trade_date
    ON sharadar_stock_prices (
                              trade_date
        );