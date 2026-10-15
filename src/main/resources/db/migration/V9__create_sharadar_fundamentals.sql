CREATE TABLE sharadar_fundamentals (

                                       stock_id BIGINT NOT NULL,

                                       dimension VARCHAR(3) NOT NULL,

                                       calendar_date DATE NOT NULL,

                                       filing_date DATE NOT NULL,

                                       report_period DATE NULL,

                                       fiscal_period VARCHAR(16) NULL,

                                       source_last_updated DATE NULL,


    -- Balance sheet

                                       assets DECIMAL(38, 12) NULL,

                                       current_assets DECIMAL(38, 12) NULL,

                                       liabilities DECIMAL(38, 12) NULL,

                                       current_liabilities DECIMAL(38, 12) NULL,

                                       equity DECIMAL(38, 12) NULL,

                                       debt DECIMAL(38, 12) NULL,

                                       current_debt DECIMAL(38, 12) NULL,

                                       noncurrent_debt DECIMAL(38, 12) NULL,

                                       cash_and_equivalents DECIMAL(38, 12) NULL,

                                       inventory DECIMAL(38, 12) NULL,

                                       receivables DECIMAL(38, 12) NULL,

                                       payables DECIMAL(38, 12) NULL,

                                       investments DECIMAL(38, 12) NULL,

                                       net_ppe DECIMAL(38, 12) NULL,

                                       intangibles DECIMAL(38, 12) NULL,

                                       working_capital DECIMAL(38, 12) NULL,


    -- Income statement

                                       revenue DECIMAL(38, 12) NULL,

                                       cost_of_revenue DECIMAL(38, 12) NULL,

                                       gross_profit DECIMAL(38, 12) NULL,

                                       operating_expenses DECIMAL(38, 12) NULL,

                                       operating_income DECIMAL(38, 12) NULL,

                                       ebit DECIMAL(38, 12) NULL,

                                       ebitda DECIMAL(38, 12) NULL,

                                       net_income DECIMAL(38, 12) NULL,

                                       net_income_common DECIMAL(38, 12) NULL,

                                       research_and_development DECIMAL(38, 12) NULL,

                                       selling_general_admin DECIMAL(38, 12) NULL,

                                       stock_based_compensation DECIMAL(38, 12) NULL,

                                       interest_expense DECIMAL(38, 12) NULL,

                                       tax_expense DECIMAL(38, 12) NULL,


    -- Cash flow

                                       operating_cash_flow DECIMAL(38, 12) NULL,

                                       capital_expenditure DECIMAL(38, 12) NULL,

                                       free_cash_flow DECIMAL(38, 12) NULL,

                                       investing_cash_flow DECIMAL(38, 12) NULL,

                                       financing_cash_flow DECIMAL(38, 12) NULL,

                                       net_cash_flow DECIMAL(38, 12) NULL,


    -- Shares / per-share data

                                       shares_basic DECIMAL(38, 12) NULL,

                                       shares_weighted_average DECIMAL(38, 12) NULL,

                                       shares_weighted_diluted DECIMAL(38, 12) NULL,

                                       share_factor DECIMAL(38, 12) NULL,

                                       eps DECIMAL(38, 12) NULL,

                                       diluted_eps DECIMAL(38, 12) NULL,

                                       sales_per_share DECIMAL(38, 12) NULL,

                                       free_cash_flow_per_share DECIMAL(38, 12) NULL,

                                       book_value_per_share DECIMAL(38, 12) NULL,

                                       tangible_book_value_per_share DECIMAL(38, 12) NULL,

                                       dividends_per_share DECIMAL(38, 12) NULL,


    -- Valuation

                                       market_cap DECIMAL(38, 12) NULL,

                                       enterprise_value DECIMAL(38, 12) NULL,

                                       price DECIMAL(38, 12) NULL,

                                       pe DECIMAL(38, 12) NULL,

                                       ps DECIMAL(38, 12) NULL,

                                       pb DECIMAL(38, 12) NULL,

                                       ev_to_ebit DECIMAL(38, 12) NULL,

                                       ev_to_ebitda DECIMAL(38, 12) NULL,


    -- Profitability / quality ratios

                                       gross_margin DECIMAL(38, 12) NULL,

                                       ebitda_margin DECIMAL(38, 12) NULL,

                                       net_margin DECIMAL(38, 12) NULL,

                                       roa DECIMAL(38, 12) NULL,

                                       roe DECIMAL(38, 12) NULL,

                                       roic DECIMAL(38, 12) NULL,

                                       current_ratio DECIMAL(38, 12) NULL,

                                       debt_to_equity DECIMAL(38, 12) NULL,


    -- USD-normalized values

                                       fx_usd DECIMAL(38, 12) NULL,

                                       revenue_usd DECIMAL(38, 12) NULL,

                                       net_income_common_usd DECIMAL(38, 12) NULL,

                                       equity_usd DECIMAL(38, 12) NULL,

                                       debt_usd DECIMAL(38, 12) NULL,

                                       cash_and_equivalents_usd DECIMAL(38, 12) NULL,

                                       ebit_usd DECIMAL(38, 12) NULL,

                                       ebitda_usd DECIMAL(38, 12) NULL,


                                       created_at TIMESTAMP NOT NULL
                                           DEFAULT CURRENT_TIMESTAMP,


                                       PRIMARY KEY (
                                                    stock_id,
                                                    dimension,
                                                    calendar_date
                                           ),


                                       CONSTRAINT fk_sharadar_fundamentals_stock
                                           FOREIGN KEY (stock_id)
                                               REFERENCES stocks (id)
);


CREATE INDEX idx_sharadar_fundamentals_pit
    ON sharadar_fundamentals (
                              stock_id,
                              dimension,
                              filing_date,
                              calendar_date
        );


CREATE INDEX idx_sharadar_fundamentals_filing_date
    ON sharadar_fundamentals (
                              dimension,
                              filing_date
        );


CREATE INDEX idx_sharadar_fundamentals_calendar_date
    ON sharadar_fundamentals (
                              calendar_date
        );