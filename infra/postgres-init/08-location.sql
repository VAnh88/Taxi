\connect location_db

CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- ============================================================
-- MARKETING_CLUSTERS — cụm điểm tiếp thị (hệ cũ: ZOZO VY, Sành Lẩu...)
-- ============================================================
CREATE TABLE IF NOT EXISTS marketing_clusters (
    id    uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    name  varchar(120) NOT NULL,
    CONSTRAINT uq_clusters_name UNIQUE (name)
);

-- ============================================================
-- MARKETING_POINTS — đối tác gọi xe hộ khách (UC16, QĐ #19 bản đơn giản)
-- ============================================================
CREATE TABLE IF NOT EXISTS marketing_points (
    id                     uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    name                   varchar(200) NOT NULL,
    address                varchar(500) NOT NULL,
    lat                    double precision,
    lng                    double precision,
    point_type             varchar(30)  NOT NULL DEFAULT 'NORMAL'
                           CHECK (point_type IN ('NORMAL','WAITING_CLUSTER','LOBBY','PICKUP_HALL')),
    cluster_id             uuid REFERENCES marketing_clusters(id),
    sale_user_id           uuid,                         -- logic ref -> auth_db; nhân viên phụ trách
    referrer_driver_phone  varchar(15),                  -- tài xế giới thiệu điểm
    status                 varchar(20)  NOT NULL DEFAULT 'ACTIVE'
                           CHECK (status IN ('ACTIVE','INACTIVE')),
    created_at             timestamptz  NOT NULL DEFAULT now()
);
CREATE INDEX IF NOT EXISTS idx_points_name ON marketing_points(lower(name));

-- ============================================================
-- MARKETING_POINT_PHONES — SĐT của điểm; sẵn cho nhận diện
-- cuộc gọi đến tự động ở phase tích hợp tổng đài
-- ============================================================
CREATE TABLE IF NOT EXISTS marketing_point_phones (
    id        uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    point_id  uuid        NOT NULL REFERENCES marketing_points(id) ON DELETE CASCADE,
    phone     varchar(15) NOT NULL,
    CONSTRAINT uq_point_phones UNIQUE (phone)
);

-- ============================================================
-- ADDRESS_SHORTCUTS — từ điển gõ tắt (UC17, QĐ #20)
-- ============================================================
CREATE TABLE IF NOT EXISTS address_shortcuts (
    id                  uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    abbreviation        varchar(40)  NOT NULL,           -- LTK, BVT, Cctdls...
    full_name           varchar(500) NOT NULL,           -- địa chỉ chuẩn hiển thị
    lat                 double precision NOT NULL,
    lng                 double precision NOT NULL,
    district            varchar(120),
    created_by_user_id  uuid,                            -- tổng đài viên thêm nhanh tại chỗ
    created_at          timestamptz  NOT NULL DEFAULT now(),
    CONSTRAINT uq_shortcuts_abbr UNIQUE (abbreviation)
);
CREATE INDEX IF NOT EXISTS idx_shortcuts_abbr_lower ON address_shortcuts(lower(abbreviation));
