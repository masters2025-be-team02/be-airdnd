ALTER TABLE reservation
    ADD column reservation_status VARCHAR (255) NOT NULL;

ALTER TABLE reserved_dates
    ADD column reservation_status VARCHAR (255) NOT NULL;
