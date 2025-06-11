-- amenity 테이블에서 count 컬럼 제거
ALTER TABLE amenity
DROP COLUMN count;

-- accommodation_amenity 테이블에 count 컬럼 추가
ALTER TABLE accommodation_amenity
    ADD COLUMN count INT;
