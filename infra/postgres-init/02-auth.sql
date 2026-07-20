\connect auth_db

CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- ============================================================
-- USERS — tài khoản 4 vai trò (QĐ: CUSTOMER mới phải verify OTP)
-- ============================================================
CREATE TABLE IF NOT EXISTS users (
    id              uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    username        varchar(50)  NOT NULL,
    password_hash   varchar(100) NOT NULL,              -- BCrypt
    phone           varchar(15)  NOT NULL,
    email           varchar(255),                       -- NOT NULL với CUSTOMER mới (ép ở tầng service); nullable cho tài khoản nội bộ/cũ
    role            varchar(20)  NOT NULL CHECK (role IN ('ADMIN','DISPATCHER','DRIVER','CUSTOMER')),
    status          varchar(30)  NOT NULL DEFAULT 'ACTIVE'
                    CHECK (status IN ('PENDING_VERIFICATION','ACTIVE','INACTIVE','LOCKED')),
    is_tester       boolean      NOT NULL DEFAULT false, -- dùng voucher tester_only
    created_at      timestamptz  NOT NULL DEFAULT now(),
    CONSTRAINT uq_users_username UNIQUE (username),
    CONSTRAINT uq_users_phone    UNIQUE (phone),
    CONSTRAINT uq_users_email    UNIQUE (email)
);
COMMENT ON TABLE users IS 'Tài khoản đăng nhập. CUSTOMER đăng ký mới: status=PENDING_VERIFICATION tới khi verify OTP email.';

-- ============================================================
-- OTP_CODES — luật O1-O5: 5 phút hiệu lực, sai 5 lần khóa 15 phút,
-- gửi lại cách 60s và tối đa 5 lần/giờ; mã mới vô hiệu mã cũ (xóa/ghi đè)
-- ============================================================
CREATE TABLE IF NOT EXISTS otp_codes (
    id                   uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id              uuid         NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    code_hash            varchar(100) NOT NULL,          -- băm, không lưu plain
    purpose              varchar(30)  NOT NULL DEFAULT 'REGISTER'
                         CHECK (purpose IN ('REGISTER','RESET_PASSWORD')),
    expires_at           timestamptz  NOT NULL,          -- tạo + 5 phút
    failed_attempts      int          NOT NULL DEFAULT 0 CHECK (failed_attempts >= 0),
    locked_until         timestamptz,                    -- set khi failed_attempts >= 5
    last_sent_at         timestamptz  NOT NULL DEFAULT now(),
    sent_count           int          NOT NULL DEFAULT 1 CHECK (sent_count >= 0),
    sent_count_reset_at  timestamptz  NOT NULL DEFAULT now()
);
CREATE INDEX IF NOT EXISTS idx_otp_user ON otp_codes(user_id);
