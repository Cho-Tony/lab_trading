CREATE TABLE stock_prices
(
    id             BIGINT         NOT NULL AUTO_INCREMENT,
    stock_id       BIGINT         NOT NULL,
    trade_date     DATE           NOT NULL,

    open           DECIMAL(19, 6) NOT NULL,
    high           DECIMAL(19, 6) NOT NULL,
    low            DECIMAL(19, 6) NOT NULL,
    close          DECIMAL(19, 6) NOT NULL,
    adjusted_close DECIMAL(19, 6) NULL,

    volume         BIGINT         NOT NULL,

    created_at     DATETIME(6)    NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at     DATETIME(6)    NOT NULL DEFAULT CURRENT_TIMESTAMP(6)
                                           ON UPDATE CURRENT_TIMESTAMP(6),

    PRIMARY KEY (id),

    CONSTRAINT uk_stock_prices_stock_date
        UNIQUE (stock_id, trade_date),

    CONSTRAINT fk_stock_prices_stock
        FOREIGN KEY (stock_id)
            REFERENCES stocks (id)
);