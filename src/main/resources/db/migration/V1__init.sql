CREATE TABLE address (
                         id BIGINT NOT NULL AUTO_INCREMENT,
                         latitude DOUBLE,
                         longitude DOUBLE,
                         postal_code INT,
                         created_at TIMESTAMP,
                         city VARCHAR(255),
                         country VARCHAR(255),
                         detail VARCHAR(255),
                         district VARCHAR(255),
                         street VARCHAR(255),
                         PRIMARY KEY (id)
) ENGINE=InnoDB;

CREATE TABLE member (
                        id BIGINT NOT NULL AUTO_INCREMENT,
                        created_at TIMESTAMP,
                        email VARCHAR(255),
                        nickname VARCHAR(255),
                        password VARCHAR(255),
                        thumbnail_image_url VARCHAR(255),
                        role VARCHAR(50),
                        PRIMARY KEY (id)
) ENGINE=InnoDB;

CREATE TABLE occupancy_policy (
                                  id BIGINT NOT NULL AUTO_INCREMENT,
                                  adult_occupancy INT,
                                  child_occupancy INT,
                                  infant_occupancy INT,
                                  max_occupancy INT,
                                  pet_occupancy INT,
                                  created_at TIMESTAMP,
                                  PRIMARY KEY (id)
) ENGINE=InnoDB;

CREATE TABLE accommodation (
                               id BIGINT NOT NULL AUTO_INCREMENT,
                               base_price INT,
                               address_id BIGINT,
                               created_at TIMESTAMP,
                               member_id BIGINT,
                               occupancy_policy_id BIGINT,
                               description VARCHAR(255),
                               name VARCHAR(255),
                               thumbnail_url VARCHAR(255),
                               type VARCHAR(50),
                               PRIMARY KEY (id),
                               CONSTRAINT FK_accommodation_address_id FOREIGN KEY (address_id) REFERENCES address (id),
                               CONSTRAINT FK_accommodation_member_id FOREIGN KEY (member_id) REFERENCES member (id),
                               CONSTRAINT FK_accommodation_occupancy_policy_id FOREIGN KEY (occupancy_policy_id) REFERENCES occupancy_policy (id)
) ENGINE=InnoDB;

CREATE TABLE amenity (
                         id BIGINT NOT NULL AUTO_INCREMENT,
                         count INT,
                         created_at TIMESTAMP,
                         name VARCHAR(255),
                         PRIMARY KEY (id)
) ENGINE=InnoDB;

CREATE TABLE accommodation_amenity (
                                       id BIGINT NOT NULL AUTO_INCREMENT,
                                       accommodation_id BIGINT,
                                       amenity_id BIGINT,
                                       created_at TIMESTAMP,
                                       PRIMARY KEY (id),
                                       CONSTRAINT FK_accommodation_amenity_accommodation_id FOREIGN KEY (accommodation_id) REFERENCES accommodation (id),
                                       CONSTRAINT FK_accommodation_amenity_amenity_id FOREIGN KEY (amenity_id) REFERENCES amenity (id)
) ENGINE=InnoDB;

CREATE TABLE discount_policy (
                                 id BIGINT NOT NULL AUTO_INCREMENT,
                                 discount_rate DOUBLE,
                                 is_active BIT,
                                 max_apply_price INT,
                                 min_payment_price INT,
                                 created_at TIMESTAMP,
                                 end_date DATETIME(6),
                                 start_date DATETIME(6),
                                 description VARCHAR(255),
                                 name VARCHAR(255),
                                 discount_type VARCHAR(50),
                                 promotion_type VARCHAR(50),
                                 PRIMARY KEY (id)
) ENGINE=InnoDB;

CREATE TABLE accommodation_discount_policy (
                                               id BIGINT NOT NULL AUTO_INCREMENT,
                                               accommodation_id BIGINT,
                                               created_at TIMESTAMP,
                                               discount_policy_id BIGINT,
                                               PRIMARY KEY (id),
                                               CONSTRAINT FK_accommodation_discount_policy_accommodation_id FOREIGN KEY (accommodation_id) REFERENCES accommodation (id),
                                               CONSTRAINT FK_accommodation_discount_policy_discount_policy_id FOREIGN KEY (discount_policy_id) REFERENCES discount_policy (id)
) ENGINE=InnoDB;

CREATE TABLE accommodation_image (
                                     id BIGINT NOT NULL AUTO_INCREMENT,
                                     accommodation_id BIGINT,
                                     created_at TIMESTAMP,
                                     image_url VARCHAR(255),
                                     PRIMARY KEY (id),
                                     CONSTRAINT FK_accommodation_image_accommodation_id FOREIGN KEY (accommodation_id) REFERENCES accommodation (id)
) ENGINE=InnoDB;

CREATE TABLE reservation (
                             id BIGINT NOT NULL AUTO_INCREMENT,
                             total_price INT,
                             accommodation_id BIGINT,
                             check_in DATETIME(6),
                             check_out DATETIME(6),
                             created_at TIMESTAMP,
                             member_id BIGINT,
                             message VARCHAR(255),
                             PRIMARY KEY (id),
                             CONSTRAINT FK_reservation_accommodation_id FOREIGN KEY (accommodation_id) REFERENCES accommodation (id),
                             CONSTRAINT FK_reservation_member_id FOREIGN KEY (member_id) REFERENCES member (id)
) ENGINE=InnoDB;

CREATE TABLE payment (
                         id BIGINT NOT NULL AUTO_INCREMENT,
                         total_price INT,
                         created_at TIMESTAMP,
                         member_id BIGINT,
                         paid_at DATETIME(6),
                         reservation_id BIGINT,
                         transaction_id BIGINT,
                         cancel_reason VARCHAR(255),
                         payment_gateway VARCHAR(50),
                         payment_method VARCHAR(50),
                         status VARCHAR(50),
                         PRIMARY KEY (id),
                         CONSTRAINT FK_payment_member_id FOREIGN KEY (member_id) REFERENCES member (id),
                         CONSTRAINT FK_payment_reservation_id FOREIGN KEY (reservation_id) REFERENCES reservation (id)
) ENGINE=InnoDB;

CREATE TABLE price_policy (
                              id BIGINT NOT NULL AUTO_INCREMENT,
                              end_date DATE,
                              price INT,
                              start_date DATE,
                              accommodation_id BIGINT,
                              created_at TIMESTAMP,
                              season VARCHAR(255),
                              PRIMARY KEY (id),
                              CONSTRAINT FK_price_policy_accommodation_id FOREIGN KEY (accommodation_id) REFERENCES accommodation (id)
) ENGINE=InnoDB;

CREATE TABLE review (
                        id BIGINT NOT NULL AUTO_INCREMENT,
                        rating DOUBLE,
                        accommodation_id BIGINT,
                        created_at TIMESTAMP,
                        member_id BIGINT,
                        content VARCHAR(255),
                        PRIMARY KEY (id),
                        CONSTRAINT FK_review_accommodation_id FOREIGN KEY (accommodation_id) REFERENCES accommodation (id),
                        CONSTRAINT FK_review_member_id FOREIGN KEY (member_id) REFERENCES member (id)
) ENGINE=InnoDB;

CREATE TABLE review_image (
                              id BIGINT NOT NULL AUTO_INCREMENT,
                              created_at TIMESTAMP,
                              review_id BIGINT,
                              image_url VARCHAR(255),
                              PRIMARY KEY (id),
                              CONSTRAINT FK_review_image_review_id FOREIGN KEY (review_id) REFERENCES review (id)
) ENGINE=InnoDB;

CREATE TABLE wishlist (
                          id BIGINT NOT NULL AUTO_INCREMENT,
                          name VARCHAR(255) NOT NULL,
                          created_at TIMESTAMP,
                          member_id BIGINT,
                          PRIMARY KEY (id),
                          CONSTRAINT FK_wishlist_member_id FOREIGN KEY (member_id) REFERENCES member (id)
) ENGINE=InnoDB;

CREATE TABLE wishlist_accommodation (
                          id BIGINT NOT NULL AUTO_INCREMENT,
                          memo VARCHAR(1024) NOT NULL,
                          created_at TIMESTAMP,
                          wishlist_id BIGINT,
                          accommodation_id BIGINT,
                          PRIMARY KEY (id),
                          CONSTRAINT FK_wishlist_accommodation_accommodation_id FOREIGN KEY (accommodation_id) REFERENCES accommodation (id),
                          CONSTRAINT FK_wishlist_accommodation_wishlist_id FOREIGN KEY (wishlist_id) REFERENCES wishlist (id)
) ENGINE=InnoDB;
