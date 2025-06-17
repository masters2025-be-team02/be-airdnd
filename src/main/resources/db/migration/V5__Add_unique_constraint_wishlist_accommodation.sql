-- 위시리스트-숙소 조합에 대한 UNIQUE 제약조건 추가
ALTER TABLE wishlist_accommodation
    ADD CONSTRAINT uk_wishlist_accommodation_unique
        UNIQUE (wishlist_id, accommodation_id);
