ALTER TABLE reservation
    ADD column status VARCHAR (255) NOT NULL;

ALTER TABLE reserved_dates
    ADD column status VARCHAR (255) NOT NULL;
