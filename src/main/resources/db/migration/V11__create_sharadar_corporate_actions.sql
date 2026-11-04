CREATE TABLE sharadar_corporate_actions (

                                            id BIGINT NOT NULL AUTO_INCREMENT,

                                            stock_id BIGINT NULL,

                                            action_date DATE NOT NULL,

                                            action_type VARCHAR(64) NOT NULL,

                                            ticker VARCHAR(32) NULL,

                                            security_name VARCHAR(255) NULL,

                                            action_value DECIMAL(38, 16) NULL,

                                            contra_ticker VARCHAR(32) NULL,

                                            contra_name VARCHAR(255) NULL,

                                            created_at TIMESTAMP NOT NULL
                                                DEFAULT CURRENT_TIMESTAMP,


                                            PRIMARY KEY (id),


                                            CONSTRAINT fk_sharadar_corporate_actions_stock
                                                FOREIGN KEY (stock_id)
                                                    REFERENCES stocks (id)
);


CREATE INDEX idx_sharadar_actions_stock_date
    ON sharadar_corporate_actions (
                                   stock_id,
                                   action_date
        );


CREATE INDEX idx_sharadar_actions_ticker_date
    ON sharadar_corporate_actions (
                                   ticker,
                                   action_date
        );


CREATE INDEX idx_sharadar_actions_type_date
    ON sharadar_corporate_actions (
                                   action_type,
                                   action_date
        );


CREATE INDEX idx_sharadar_actions_contra_ticker
    ON sharadar_corporate_actions (
                                   contra_ticker
        );