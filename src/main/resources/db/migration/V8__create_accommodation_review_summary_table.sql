CREATE TABLE accommodation_review_summary (
                                              accommodation_id BIGINT NOT NULL,
                                              total_review_count INT NOT NULL DEFAULT 0,
                                              rating_sum BIGINT NOT NULL DEFAULT 0,
                                              average_rating DECIMAL(3,2) NOT NULL DEFAULT 0.00,
                                              created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                              updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

                                              PRIMARY KEY (accommodation_id),

                                              CONSTRAINT fk_accommodation_review_summary_accommodation
                                                  FOREIGN KEY (accommodation_id) REFERENCES accommodation(id)
                                                      ON DELETE CASCADE
);
