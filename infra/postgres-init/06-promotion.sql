\connect promotion_db

CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- ============================================================
-- VOUCHERS — mô hình kế thừa "Mã khuyến mãi" hệ cũ + QĐ #6/#16
-- ============================================================
CREATE TABLE IF NOT EXISTS vouchers (
    id                  uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    code                varchar(40)  NOT NULL,           -- in hoa, ép tầng service
    name                varchar(200) NOT NULL,
    description         varchar(1000),
    discount_type       varchar(20)  NOT NULL
                        CHECK (discount_type IN ('PERCENT','PERCENT_CAPPED','FIXED')),
    discount_value      bigint       NOT NULL CHECK (discount_value > 0),  -- % (1-100) hoặc VNĐ
    discount_cap        bigint       CHECK (discount_cap IS NULL OR discount_cap > 0),
    total_quota         int          NOT NULL CHECK (total_quota > 0),
    total_used          int          NOT NULL DEFAULT 0 CHECK (total_used >= 0),
    daily_quota         int          CHECK (daily_quota IS NULL OR daily_quota > 0),
    per_customer_quota  int          NOT NULL DEFAULT 1 CHECK (per_customer_quota > 0),
    min_trip_amount     bigint       CHECK (min_trip_amount IS NULL OR min_trip_amount > 0),
    min_tier            varchar(10)  CHECK (min_tier IS NULL OR min_tier IN ('DONG','BAC','VANG')),
    hidden              boolean      NOT NULL DEFAULT false,  -- mã ẩn phát offline, không hiện ví
    tester_only         boolean      NOT NULL DEFAULT false,
    status              varchar(20)  NOT NULL DEFAULT 'ACTIVE'
                        CHECK (status IN ('ACTIVE','INACTIVE')),
    valid_from          timestamptz  NOT NULL,
    valid_to            timestamptz  NOT NULL,
    created_at          timestamptz  NOT NULL DEFAULT now(),
    CONSTRAINT uq_vouchers_code UNIQUE (code),
    CONSTRAINT ck_vouchers_valid_window CHECK (valid_from < valid_to),
    CONSTRAINT ck_vouchers_used_quota   CHECK (total_used <= total_quota),
    CONSTRAINT ck_vouchers_percent_range CHECK (
        discount_type = 'FIXED' OR (discount_value BETWEEN 1 AND 100)),
    CONSTRAINT ck_vouchers_cap CHECK (
        (discount_type = 'PERCENT_CAPPED') = (discount_cap IS NOT NULL))
);
COMMENT ON COLUMN vouchers.total_used IS 'Trừ atomic: UPDATE .. SET total_used=total_used+1 WHERE total_used < total_quota (case V7 race)';

-- ============================================================
-- VOUCHER_DAILY_COUNTERS — định mức ngày; hoàn đúng ngày đã trừ (V10)
-- ============================================================
CREATE TABLE IF NOT EXISTS voucher_daily_counters (
    voucher_id  uuid NOT NULL REFERENCES vouchers(id) ON DELETE CASCADE,
    usage_date  date NOT NULL,
    used_count  int  NOT NULL DEFAULT 0 CHECK (used_count >= 0),
    PRIMARY KEY (voucher_id, usage_date)
);

-- ============================================================
-- VOUCHER_GRANTS — phát đích danh qua campaign (V12)
-- ============================================================
CREATE TABLE IF NOT EXISTS voucher_grants (
    id           uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    voucher_id   uuid        NOT NULL REFERENCES vouchers(id) ON DELETE CASCADE,
    customer_id  uuid        NOT NULL,                   -- logic ref -> customer_db
    campaign_id  uuid,                                   -- logic ref -> notification_db
    granted_at   timestamptz NOT NULL DEFAULT now(),
    CONSTRAINT uq_grants_voucher_customer UNIQUE (voucher_id, customer_id)
);
CREATE INDEX IF NOT EXISTS idx_grants_customer ON voucher_grants(customer_id);

-- ============================================================
-- VOUCHER_RESERVATIONS — vòng đời 1 lượt dùng:
-- RESERVED (chốt discount_amount) -> COMMITTED / RELEASED / EXPIRED
-- ============================================================
CREATE TABLE IF NOT EXISTS voucher_reservations (
    id               uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    voucher_id       uuid        NOT NULL REFERENCES vouchers(id),
    customer_id      uuid        NOT NULL,               -- logic ref -> customer_db
    trip_id          uuid,                               -- logic ref -> trip_db; NULL tới khi attach
    discount_amount  bigint      NOT NULL CHECK (discount_amount >= 0),  -- CHỐT CỨNG lúc reserve (QĐ #6)
    status           varchar(20) NOT NULL DEFAULT 'RESERVED'
                     CHECK (status IN ('RESERVED','COMMITTED','RELEASED','EXPIRED')),
    reserved_at      timestamptz NOT NULL DEFAULT now(),
    expires_at       timestamptz,                        -- +15 phút; NULL với chuyến hẹn giờ
    resolved_at      timestamptz
);
CREATE INDEX IF NOT EXISTS idx_reservations_voucher_customer
    ON voucher_reservations(voucher_id, customer_id);
CREATE INDEX IF NOT EXISTS idx_reservations_trip ON voucher_reservations(trip_id);
CREATE INDEX IF NOT EXISTS idx_reservations_expiring
    ON voucher_reservations(expires_at) WHERE status = 'RESERVED';
