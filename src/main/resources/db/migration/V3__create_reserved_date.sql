CREATE TABLE reserved_dates (
                                id BIGINT NOT NULL AUTO_INCREMENT,
                                accommodation_id BIGINT NOT NULL,
                                reserved_at DATE NOT NULL,
                                created_at TIMESTAMP,
                                PRIMARY KEY (id),
                                CONSTRAINT FK_reserved_date_accommodation_id FOREIGN KEY (accommodation_id) REFERENCES accommodation (id)
);

CREATE INDEX idx_reserved_dates_accommodation_reserved_at
    ON reserved_dates (accommodation_id, reserved_at);
