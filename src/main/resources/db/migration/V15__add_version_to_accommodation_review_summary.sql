-- V15_add_version_to_accommodation_review_summary.sql

ALTER TABLE accommodation_review_summary
ADD COLUMN version BIGINT NOT NULL DEFAULT 0;
