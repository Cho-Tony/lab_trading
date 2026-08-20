CREATE TABLE stocks
(
    id         BIGINT       NOT NULL AUTO_INCREMENT,
    symbol     VARCHAR(32)  NOT NULL,
    name       VARCHAR(100) NOT NULL,
    exchange   VARCHAR(30)  NOT NULL,
    market     VARCHAR(30)  NULL,
    currency   CHAR(3)      NOT NULL,
    active     BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6)
                                      ON UPDATE CURRENT_TIMESTAMP(6),

    PRIMARY KEY (id),

    CONSTRAINT uk_stocks_symbol_exchange
        UNIQUE (symbol, exchange)
);