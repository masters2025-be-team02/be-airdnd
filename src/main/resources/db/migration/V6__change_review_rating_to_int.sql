-- Review 테이블의 rating 컬럼 타입을 DOUBLE에서 INT로 변경
ALTER TABLE review MODIFY COLUMN rating INT;
