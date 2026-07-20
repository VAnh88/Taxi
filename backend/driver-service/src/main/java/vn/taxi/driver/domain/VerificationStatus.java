package vn.taxi.driver.domain;

/** Cổng duyệt hồ sơ tài xế — chỉ VERIFIED mới được lên ca / nhận cuốc. */
public enum VerificationStatus {
    PENDING,
    VERIFIED,
    REJECTED
}
