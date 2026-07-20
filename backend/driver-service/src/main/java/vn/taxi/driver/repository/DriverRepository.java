package vn.taxi.driver.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.taxi.driver.domain.Driver;
import vn.taxi.driver.domain.DriverStatus;
import vn.taxi.driver.domain.ShiftStatus;
import vn.taxi.driver.domain.VerificationStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DriverRepository extends JpaRepository<Driver, UUID> {
    Optional<Driver> findByUserId(UUID userId);

    /** Chỉ tài xế đã duyệt hồ sơ (VERIFIED) mới được tìm thấy khi dispatch tự động. */
    List<Driver> findByStatusAndVerificationStatusAndShiftStatusAndCurrentLatIsNotNull(
            DriverStatus status, VerificationStatus verificationStatus, ShiftStatus shiftStatus);
}
