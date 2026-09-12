CREATE TABLE financial_statements
(
    id BIGINT AUTO_INCREMENT PRIMARY KEY,

    stock_id BIGINT NOT NULL,

    period_end_date DATE NOT NULL,
    filed_date DATE NOT NULL,

    fiscal_year INT NOT NULL,
    fiscal_quarter VARCHAR(10) NOT NULL,

    revenue DECIMAL(38, 6),
    operating_income DECIMAL(38, 6),
    net_income DECIMAL(38, 6),

    total_assets DECIMAL(38, 6),
    total_equity DECIMAL(38, 6),
    total_debt DECIMAL(38, 6),
    cash DECIMAL(38, 6),

    operating_cash_flow DECIMAL(38, 6),
    capital_expenditure DECIMAL(38, 6),

    shares_outstanding DECIMAL(38, 6),

    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL
                                 DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_financial_statements_stock
        FOREIGN KEY (stock_id)
            REFERENCES stocks (id),

    CONSTRAINT uk_financial_statements_stock_period_filed
        UNIQUE (
                stock_id,
                period_end_date,
                filed_date
            )
);


CREATE INDEX idx_financial_statements_stock_filed
    ON financial_statements (
                             stock_id,
                             filed_date
        );


CREATE INDEX idx_financial_statements_stock_period
    ON financial_statements (
                             stock_id,
                             period_end_date
        );


CREATE INDEX idx_financial_statements_stock_fiscal
    ON financial_statements (
                             stock_id,
                             fiscal_year,
                             fiscal_quarter
        );