-- V14__Create_failed_indexing_events_table.sql

CREATE TABLE failed_indexing_events (
                                        id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                        event_type VARCHAR(100) NOT NULL,
                                        event_data TEXT NOT NULL,
                                        error_message TEXT,
                                        status VARCHAR(20) NOT NULL DEFAULT 'FAILED',
                                        retry_count INT NOT NULL DEFAULT 0,
                                        failed_at DATETIME(6) NOT NULL,
                                        next_retry_at DATETIME(6) NOT NULL,
                                        last_retry_at DATETIME(6),
                                        created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6)
) ENGINE=InnoDB;

CREATE INDEX idx_failed_indexing_events_status_next_retry
    ON failed_indexing_events (status, next_retry_at);

CREATE INDEX idx_failed_indexing_events_status_retry_count
    ON failed_indexing_events (status, retry_count);

CREATE INDEX idx_failed_indexing_events_event_type
    ON failed_indexing_events (event_type);

CREATE INDEX idx_failed_indexing_events_failed_at
    ON failed_indexing_events (failed_at);
