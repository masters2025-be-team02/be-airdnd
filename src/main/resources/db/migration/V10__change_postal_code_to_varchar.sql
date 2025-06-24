-- V10__change_postal_code_to_varchar.sql

ALTER TABLE address
    MODIFY COLUMN postal_code VARCHAR(12);
