\connect trip_db

CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- ============================================================
-- CANCEL_REASONS — danh mục lý do hủy theo phía (seed 3 từ hệ cũ)
-- ============================================================
CREATE TABLE IF NOT EXISTS cancel_reasons (
    id          uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    name_vi     varchar(200) NOT NULL,
    name_en     varchar(200),
    applies_to  varchar(20)  NOT NULL DEFAULT 'ALL'
                CHECK (applies_to IN ('CUSTOMER','DRIVER','DISPATCHER','ALL')),
    active      boolean      NOT NULL DEFAULT true,
    CONSTRAINT uq_cancel_reasons_name UNIQUE (name_vi)
);

-- ============================================================
-- TRIPS — bảng lõi, 10 trạng thái (QĐ #17 thêm SCHEDULED)
-- ============================================================
CREATE TABLE IF NOT EXISTS trips (
    id                       uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    customer_id              uuid,                        -- logic ref -> customer_db; NULL = khách vãng lai
    caller_phone             varchar(15),                 -- bắt buộc khi customer_id NULL
    caller_name              varchar(120),
    driver_id                uuid,                        -- logic ref -> driver_db
    request_id               varchar(64),                 -- idempotency chống bấm đúp (A5)
    pickup_address           varchar(500) NOT NULL,
    pickup_lat               double precision NOT NULL,
    pickup_lng               double precision NOT NULL,
    dropoff_address          varchar(500) NOT NULL,
    dropoff_lat              double precision NOT NULL,
    dropoff_lng              double precision NOT NULL,
    status                   varchar(30)  NOT NULL DEFAULT 'REQUESTED'
                             CHECK (status IN ('SCHEDULED','REQUESTED','DRIVER_ASSIGNED','DRIVER_ARRIVING',
                                               'CUSTOMER_ONBOARD','COMPLETED','NO_DRIVER_AVAILABLE',
                                               'CANCELLED_BY_CUSTOMER','CANCELLED_BY_DRIVER','CANCELLED_BY_DISPATCHER')),
    source_channel           varchar(30)  NOT NULL
                             CHECK (source_channel IN ('CUSTOMER_APP','DISPATCHER','WALK_IN','MARKETING_POINT')),
    marketing_point_id       uuid,                        -- logic ref -> location_db (UC16 bản đơn giản)
    scheduled_at             timestamptz,                 -- hẹn giờ; worker kích hoạt trước 15 phút
    estimated_price          bigint CHECK (estimated_price IS NULL OR estimated_price >= 0),
    voucher_reservation_id   uuid,                        -- logic ref -> promotion_db
    voucher_code             varchar(40),                 -- hiển thị
    discount_amount          bigint NOT NULL DEFAULT 0 CHECK (discount_amount >= 0),  -- CHỐT CỨNG lúc đặt (QĐ #6)
    price                    bigint CHECK (price IS NULL OR price >= 0),              -- giá cuối, không âm (B4)
    distance_km              double precision CHECK (distance_km IS NULL OR distance_km >= 0),
    cancel_reason_id         uuid REFERENCES cancel_reasons(id),
    cancel_note              varchar(500),
    customer_rating          smallint CHECK (customer_rating IS NULL OR (customer_rating BETWEEN 1 AND 5)),
    customer_rating_comment  varchar(1000),
    requested_at             timestamptz NOT NULL DEFAULT now(),
    assigned_at              timestamptz,
    arriving_at              timestamptz,
    onboard_at               timestamptz,
    completed_at             timestamptz,
    cancelled_at             timestamptz,
    CONSTRAINT uq_trips_request UNIQUE (request_id),
    CONSTRAINT ck_trips_has_customer CHECK (customer_id IS NOT NULL OR caller_phone IS NOT NULL),
    CONSTRAINT ck_trips_cancel_reason CHECK (
        status NOT IN ('CANCELLED_BY_CUSTOMER','CANCELLED_BY_DRIVER','CANCELLED_BY_DISPATCHER')
        OR cancel_reason_id IS NOT NULL)                  -- hủy bắt buộc lý do (QĐ #3)
);
CREATE INDEX IF NOT EXISTS idx_trips_status_time  ON trips(status, requested_at DESC);
CREATE INDEX IF NOT EXISTS idx_trips_customer     ON trips(customer_id);
CREATE INDEX IF NOT EXISTS idx_trips_driver       ON trips(driver_id);
CREATE INDEX IF NOT EXISTS idx_trips_scheduled    ON trips(scheduled_at) WHERE status = 'SCHEDULED';

-- ============================================================
-- TRIP_STATUS_HISTORY — audit mọi lần đổi trạng thái
-- ============================================================
CREATE TABLE IF NOT EXISTS trip_status_history (
    id                  uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    trip_id             uuid        NOT NULL REFERENCES trips(id) ON DELETE CASCADE,
    from_status         varchar(30),
    to_status           varchar(30) NOT NULL,
    changed_by_user_id  uuid,                            -- NULL = hệ thống tự động
    note                varchar(500),                    -- BẮT BUỘC với force-complete/force-cancel (ép tầng service)
    changed_at          timestamptz NOT NULL DEFAULT now()
);
CREATE INDEX IF NOT EXISTS idx_history_trip ON trip_status_history(trip_id, changed_at);

-- ============================================================
-- DISPATCH_ATTEMPTS — lịch sử offer từng xe (tuần tự, QĐ #13-#15)
-- Không re-offer xe đã bỏ trong cùng chuyến (UNIQUE trip+driver)
-- REJECTED + TIMEOUT = "bỏ cuốc" (dữ liệu thô cho KPI sau MVP)
-- ============================================================
CREATE TABLE IF NOT EXISTS dispatch_attempts (
    id            uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    trip_id       uuid        NOT NULL REFERENCES trips(id) ON DELETE CASCADE,
    driver_id     uuid        NOT NULL,                  -- logic ref -> driver_db
    round         int         NOT NULL CHECK (round >= 1),
    offered_at    timestamptz NOT NULL DEFAULT now(),
    responded_at  timestamptz,
    response      varchar(20) CHECK (response IN ('ACCEPTED','REJECTED','TIMEOUT')),
    CONSTRAINT uq_attempt_trip_driver UNIQUE (trip_id, driver_id)
);
CREATE INDEX IF NOT EXISTS idx_attempts_driver ON dispatch_attempts(driver_id, offered_at);

-- ============================================================
-- SEED
-- ============================================================
INSERT INTO cancel_reasons (name_vi, name_en, applies_to) VALUES
    ('Khách hàng đã có xe khác đón', 'Customer already has another ride', 'ALL'),
    ('Không liên lạc được với khách', 'Cannot reach customer', 'DRIVER'),
    ('Xa điểm, không đón khách', 'Too far, cannot pick up', 'DRIVER')
ON CONFLICT (name_vi) DO NOTHING;
