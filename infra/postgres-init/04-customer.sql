\connect customer_db

CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- ============================================================
-- CUSTOMERS — phân loại (VIP là cờ tay) + hạng tự động (QĐ #5)
-- ============================================================
CREATE TABLE IF NOT EXISTS customers (
    id                uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id           uuid         NOT NULL,             -- logic ref -> auth_db.users
    full_name         varchar(120) NOT NULL,
    phone             varchar(15)  NOT NULL,
    type              varchar(20)  NOT NULL DEFAULT 'APP'
                      CHECK (type IN ('VIP','APP','REGULAR','BLACKLIST')),
    blacklist_reason  varchar(500),
    tier              varchar(10)  NOT NULL DEFAULT 'DONG'
                      CHECK (tier IN ('DONG','BAC','VANG')),
    tier_updated_at   timestamptz,
    created_at        timestamptz  NOT NULL DEFAULT now(),
    CONSTRAINT uq_customers_user  UNIQUE (user_id),
    CONSTRAINT uq_customers_phone UNIQUE (phone),
    CONSTRAINT ck_blacklist_reason CHECK (type <> 'BLACKLIST' OR blacklist_reason IS NOT NULL)
);
COMMENT ON COLUMN customers.tier IS 'Xét theo chi tiêu thực trả 3 tháng rolling: BAC >= 1tr, VANG >= 3tr (ngưỡng ở config). VIP là type gán tay, độc lập tier.';

-- ============================================================
-- SPENDING_ENTRIES — nguồn tính rolling 3 tháng; idempotent theo trip_id
-- ============================================================
CREATE TABLE IF NOT EXISTS spending_entries (
    id            uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    customer_id   uuid        NOT NULL REFERENCES customers(id) ON DELETE CASCADE,
    trip_id       uuid        NOT NULL,                  -- logic ref -> trip_db.trips
    amount        bigint      NOT NULL CHECK (amount >= 0),   -- tiền THỰC TRẢ sau giảm
    completed_at  timestamptz NOT NULL,
    CONSTRAINT uq_spending_trip UNIQUE (trip_id)         -- 1 chuyến ghi đúng 1 lần (S5 retry an toàn)
);
CREATE INDEX IF NOT EXISTS idx_spending_customer_time
    ON spending_entries(customer_id, completed_at);
