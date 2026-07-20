\connect driver_db

CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- ============================================================
-- DRIVER_TEAMS — 5 đội cố định kế thừa hệ cũ
-- ============================================================
CREATE TABLE IF NOT EXISTS driver_teams (
    id    uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    name  varchar(120) NOT NULL,
    CONSTRAINT uq_teams_name UNIQUE (name)
);

-- ============================================================
-- DRIVERS — hồ sơ tài xế; cổng duyệt verification_status (QĐ #2)
-- ============================================================
CREATE TABLE IF NOT EXISTS drivers (
    id                   uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id              uuid         NOT NULL,           -- logic ref -> auth_db.users
    team_id              uuid         REFERENCES driver_teams(id),
    full_name            varchar(120) NOT NULL,
    phone                varchar(15)  NOT NULL,
    status               varchar(20)  NOT NULL DEFAULT 'ACTIVE'
                         CHECK (status IN ('ACTIVE','LOCKED','INACTIVE')),
    verification_status  varchar(20)  NOT NULL DEFAULT 'PENDING'
                         CHECK (verification_status IN ('PENDING','VERIFIED','REJECTED')),
    shift_status         varchar(10)  NOT NULL DEFAULT 'OFF'
                         CHECK (shift_status IN ('ON','OFF')),
    rating               numeric(2,1) NOT NULL DEFAULT 5.0 CHECK (rating >= 1.0 AND rating <= 5.0),
    current_lat          double precision,
    current_lng          double precision,
    location_updated_at  timestamptz,                    -- cũ hơn 5 phút -> loại khỏi dispatch (A6)
    created_at           timestamptz  NOT NULL DEFAULT now(),
    CONSTRAINT uq_drivers_user  UNIQUE (user_id),
    CONSTRAINT uq_drivers_phone UNIQUE (phone)
);
CREATE INDEX IF NOT EXISTS idx_drivers_dispatch
    ON drivers(status, verification_status, shift_status);
COMMENT ON COLUMN drivers.verification_status IS 'Chỉ VERIFIED mới được lên ca / được tìm thấy khi dispatch (QĐ #2)';

-- ============================================================
-- VEHICLE_TYPES — danh mục cỡ xe (seed 4 loại từ hệ cũ)
-- ============================================================
CREATE TABLE IF NOT EXISTS vehicle_types (
    id          uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    code        varchar(40)  NOT NULL,
    name_vi     varchar(120) NOT NULL,
    seat_count  int          NOT NULL CHECK (seat_count > 0),
    CONSTRAINT uq_vehicle_types_code UNIQUE (code)
);

-- ============================================================
-- VEHICLES — 1 tài xế = 1 xe (QĐ #4, UNIQUE driver_id);
-- là bảng riêng nên sau mở nhiều ca không phải đập schema
-- ============================================================
CREATE TABLE IF NOT EXISTS vehicles (
    id               uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    driver_id        uuid         NOT NULL REFERENCES drivers(id),
    vehicle_type_id  uuid         NOT NULL REFERENCES vehicle_types(id),
    plate_number     varchar(20)  NOT NULL,
    brand            varchar(60)  NOT NULL,
    model            varchar(80)  NOT NULL,
    status           varchar(20)  NOT NULL DEFAULT 'ACTIVE'
                     CHECK (status IN ('ACTIVE','INACTIVE')),
    CONSTRAINT uq_vehicles_driver UNIQUE (driver_id),
    CONSTRAINT uq_vehicles_plate  UNIQUE (plate_number)
);

-- ============================================================
-- DRIVER_DOCUMENTS — 4 loại giấy tờ, duyệt riêng từng cái
-- ============================================================
CREATE TABLE IF NOT EXISTS driver_documents (
    id             uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    driver_id      uuid         NOT NULL REFERENCES drivers(id) ON DELETE CASCADE,
    doc_type       varchar(30)  NOT NULL
                   CHECK (doc_type IN ('CCCD','DRIVER_LICENSE','VEHICLE_REGISTRATION','INSURANCE')),
    file_url       varchar(500) NOT NULL,
    verify_status  varchar(20)  NOT NULL DEFAULT 'PENDING'
                   CHECK (verify_status IN ('PENDING','VERIFIED','REJECTED')),
    created_at     timestamptz  NOT NULL DEFAULT now()
);
CREATE INDEX IF NOT EXISTS idx_documents_driver ON driver_documents(driver_id);

-- ============================================================
-- SEED
-- ============================================================
INSERT INTO driver_teams (name) VALUES
    ('Công ty TNHH Thiên Đức (Xe ngoài)'),
    ('Công ty TNHH Thiên Đức (Nội bộ)'),
    ('Công ty TNHH TM&XD Vinh Hà'),
    ('Hợp tác xã Vận tải Thiên Đức'),
    ('Đội quản lý')
ON CONFLICT (name) DO NOTHING;

INSERT INTO vehicle_types (code, name_vi, seat_count) VALUES
    ('XE_4CHO_NHO', 'Xe 4 chỗ nhỏ', 4),
    ('XE_4CHO_LON', 'Xe 4 chỗ lớn (PRE)', 4),
    ('XE_7CHO_NHO', 'Xe 7 chỗ nhỏ', 7),
    ('XE_7CHO_LON', 'Xe 7 chỗ lớn (PRE)', 7)
ON CONFLICT (code) DO NOTHING;
