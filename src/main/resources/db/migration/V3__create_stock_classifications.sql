CREATE TABLE stock_classifications (

                                       id BIGINT NOT NULL AUTO_INCREMENT,

                                       stock_id BIGINT NOT NULL,

                                       sector VARCHAR(100) NOT NULL,

                                       industry VARCHAR(150) NOT NULL,

                                       effective_from DATE NOT NULL,

                                       effective_to DATE NULL,

                                       source VARCHAR(30) NOT NULL,

                                       created_at DATETIME NOT NULL
                                           DEFAULT CURRENT_TIMESTAMP,

                                       updated_at DATETIME NOT NULL
                                           DEFAULT CURRENT_TIMESTAMP
                                           ON UPDATE CURRENT_TIMESTAMP,

                                       PRIMARY KEY (id),

                                       CONSTRAINT fk_stock_classifications_stock
                                           FOREIGN KEY (stock_id)
                                               REFERENCES stocks(id),

                                       CONSTRAINT uk_stock_classifications_stock_from
                                           UNIQUE (stock_id, effective_from),

                                       CONSTRAINT chk_stock_classifications_period
                                           CHECK (
                                               effective_to IS NULL
                                                   OR effective_to > effective_from
                                               )
);

CREATE INDEX idx_stock_classifications_stock_period
    ON stock_classifications (
                              stock_id,
                              effective_from,
                              effective_to
        );