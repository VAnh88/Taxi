\connect notification_db

CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- ============================================================
-- CAMPAIGNS — maker-checker (học hệ cũ): chỉ APPROVED mới gửi
-- ============================================================
CREATE TABLE IF NOT EXISTS campaigns (
    id                     uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    title                  varchar(200) NOT NULL,
    body                   varchar(2000) NOT NULL,
    deep_link              varchar(500),
    voucher_id             uuid,                         -- logic ref -> promotion_db; campaign phát voucher
    audience_type          varchar(20)  NOT NULL
                           CHECK (audience_type IN ('ALL','TIER','VIP','LIST')),
    audience_tier          varchar(10)  CHECK (audience_tier IS NULL OR audience_tier IN ('DONG','BAC','VANG')),
    audience_customer_ids  jsonb,                        -- khi LIST
    approval_status        varchar(20)  NOT NULL DEFAULT 'PENDING'
                           CHECK (approval_status IN ('PENDING','APPROVED','REJECTED')),
    created_by_user_id     uuid         NOT NULL,        -- logic ref -> auth_db
    approved_by_user_id    uuid,                         -- người duyệt ≠ người tạo (ép tầng service)
    scheduled_at           timestamptz,                  -- NULL = gửi ngay sau duyệt
    status                 varchar(20)  NOT NULL DEFAULT 'DRAFT'
                           CHECK (status IN ('DRAFT','SCHEDULED','SENDING','DONE','CANCELLED')),
    created_at             timestamptz  NOT NULL DEFAULT now(),
    CONSTRAINT ck_campaign_tier CHECK (audience_type <> 'TIER' OR audience_tier IS NOT NULL),
    CONSTRAINT ck_campaign_list CHECK (audience_type <> 'LIST' OR audience_customer_ids IS NOT NULL)
);
CREATE INDEX IF NOT EXISTS idx_campaigns_scheduler
    ON campaigns(scheduled_at) WHERE status = 'SCHEDULED';

-- ============================================================
-- NOTIFICATIONS — inbox; retention: đọc 30 ngày / chưa đọc 90 ngày (N5)
-- ============================================================
CREATE TABLE IF NOT EXISTS notifications (
    id                 uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    recipient_user_id  uuid         NOT NULL,            -- logic ref -> auth_db.users
    type               varchar(20)  NOT NULL
                       CHECK (type IN ('TRANSACTIONAL','PROMO','SYSTEM')),
    title              varchar(200) NOT NULL,
    body               varchar(2000) NOT NULL,
    deep_link          varchar(500),
    campaign_id        uuid REFERENCES campaigns(id),    -- NULL = noti theo sự kiện
    is_read            boolean      NOT NULL DEFAULT false,
    read_at            timestamptz,
    created_at         timestamptz  NOT NULL DEFAULT now()
);
CREATE INDEX IF NOT EXISTS idx_notifications_inbox
    ON notifications(recipient_user_id, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_notifications_retention ON notifications(created_at);

-- ============================================================
-- NOTIFICATION_DELIVERIES — log gửi TỪNG KÊNH riêng (N7):
-- kênh lỗi không chặn kênh khác
-- ============================================================
CREATE TABLE IF NOT EXISTS notification_deliveries (
    id               uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    notification_id  uuid        NOT NULL REFERENCES notifications(id) ON DELETE CASCADE,
    channel          varchar(20) NOT NULL CHECK (channel IN ('INBOX','PUSH','ZALO_OA')),
    status           varchar(20) NOT NULL DEFAULT 'PENDING'
                     CHECK (status IN ('PENDING','SENT','FAILED','SKIPPED')),
    error_detail     varchar(500),
    sent_at          timestamptz
);
CREATE INDEX IF NOT EXISTS idx_deliveries_notification ON notification_deliveries(notification_id);

-- ============================================================
-- DEVICE_PUSH_TOKENS — Expo push (2 app RN dùng chung)
-- ============================================================
CREATE TABLE IF NOT EXISTS device_push_tokens (
    id          uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id     uuid         NOT NULL,                   -- logic ref -> auth_db
    expo_token  varchar(200) NOT NULL,
    platform    varchar(10)  NOT NULL CHECK (platform IN ('ANDROID','IOS')),
    updated_at  timestamptz  NOT NULL DEFAULT now(),
    CONSTRAINT uq_push_token UNIQUE (expo_token)
);
CREATE INDEX IF NOT EXISTS idx_push_tokens_user ON device_push_tokens(user_id);

-- ============================================================
-- NOTIFICATION_PREFS — opt-out khuyến mãi (N2) + mute Zalo 30 ngày (N8)
-- ============================================================
CREATE TABLE IF NOT EXISTS notification_prefs (
    user_id           uuid PRIMARY KEY,                  -- logic ref -> auth_db
    promo_enabled     boolean     NOT NULL DEFAULT true,
    zalo_muted_until  timestamptz
);
