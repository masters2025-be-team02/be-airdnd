-- payment 테이블 열 이름 변경 및 새로운 컬럼 추가/삭제
ALTER TABLE payment
    -- 기존 id → payment_id 로 이름 변경 + 자료형 변경
    CHANGE COLUMN id payment_id BINARY(16) NOT NULL,

    -- toss_payment_key, toss_order_id 컬럼 추가
    ADD COLUMN toss_payment_key VARCHAR (255) NOT NULL UNIQUE,
    ADD COLUMN toss_order_id VARCHAR (255) NOT NULL,

    -- total_price → total_amount로 이름 변경 + 자료형 변경
    CHANGE COLUMN total_price total_amount BIGINT NOT NULL,

    -- paid_at, transaction_id, cancel_reason, payment_gateway 제거
    DROP COLUMN paid_at,
    DROP COLUMN transaction_id,
    DROP COLUMN cancel_reason,
    DROP COLUMN payment_gateway,

    -- created_at → requested_at 으로 이름 변경 + DATETIME(6)
    CHANGE COLUMN created_at requested_at DATETIME(6) NOT NULL,

    -- payment_method 컬럼: ENUM → VARCHAR + CHECK 제약
    MODIFY COLUMN payment_method VARCHAR(50)
    CHECK (payment_method IN (
    'CREDIT_CARD',
    'DEBIT_CARD',
    'PAYPAL',
    'APPLE_PAY',
    'BANK_TRANSFER',
    'VIRTUAL_ACCOUNT',
    'POINTS',
    'COUPON'
    )),

    -- status 컬럼: ENUM → VARCHAR + CHECK 제약
    MODIFY COLUMN status VARCHAR(50)
    CHECK (status IN (
      'PENDING',
      'COMPLETED',
      'FAILED',
      'CANCELLED',
      'REFUNDED',
      'PARTIALLY_REFUNDED',
      'EXPIRED'
    ))
