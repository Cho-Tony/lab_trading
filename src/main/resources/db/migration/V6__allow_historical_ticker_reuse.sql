ALTER TABLE stocks
DROP INDEX uk_stocks_symbol_exchange;


CREATE INDEX idx_stocks_symbol_exchange
    ON stocks (
               symbol,
               exchange
        );