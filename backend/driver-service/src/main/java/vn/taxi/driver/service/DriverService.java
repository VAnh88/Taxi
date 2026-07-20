package vn.taxi.driver.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import vn.taxi.driver.domain.Driver;
import vn.taxi.driver.domain.DriverStatus;
import vn.taxi.driver.domain.ShiftStatus;
import vn.taxi.driver.domain.VerificationStatus;
import vn.taxi.driver.event.DriverLocationEventPublisher;
import vn.taxi.driver.repository.DriverRepository;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
public class DriverService {

    private final DriverRepository driverRepository;
    private final DriverLocationEventPublisher driverLocationEventPublisher;

    public DriverService(DriverRepository driverRepository, DriverLocationEventPublisher driverLocationEventPublisher) {
        this.driverRepository = driverRepository;
        this.driverLocationEventPublisher = driverLocationEventPublisher;
    }

    public Driver getById(UUID id) {
        return driverRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy tài xế"));
    }

    public Driver getByUserId(UUID userId) {
        return driverRepository.findByUserId(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy hồ sơ tài xế"));
    }

    public List<Driver> findAll() {
        return driverRepository.findAll();
    }

    public Driver updateShift(UUID driverId, ShiftStatus shiftStatus, Double lat, Double lng) {
        Driver driver = getById(driverId);
        if (shiftStatus == ShiftStatus.ON && driver.getVerificationStatus() != VerificationStatus.VERIFIED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Hồ sơ tài xế chưa được duyệt (verificationStatus=" + driver.getVerificationStatus() + "), không thể lên ca");
        }
        driver.setShiftStatus(shiftStatus);
        if (lat != null && lng != null) {
            driver.updateLocation(lat, lng);
        }
        driver = driverRepository.save(driver);
        driverLocationEventPublisher.publish(driver);
        return driver;
    }

    /** Admin duyệt/từ chối hồ sơ tài xế — cổng bắt buộc trước khi tài xế được lên ca nhận cuốc. */
    public Driver updateVerification(UUID driverId, VerificationStatus verificationStatus) {
        Driver driver = getById(driverId);
        driver.setVerificationStatus(verificationStatus);
        return driverRepository.save(driver);
    }

    /** Tìm tối đa {@code limit} tài xế ACTIVE, đã duyệt hồ sơ, đang lên ca, trong bán kính {@code radiusKm}, gần nhất trước. */
    public List<Driver> findAvailableNear(double lat, double lng, double radiusKm, int limit) {
        return driverRepository.findByStatusAndVerificationStatusAndShiftStatusAndCurrentLatIsNotNull(
                        DriverStatus.ACTIVE, VerificationStatus.VERIFIED, ShiftStatus.ON)
                .stream()
                .filter(d -> GeoUtil.distanceKm(lat, lng, d.getCurrentLat(), d.getCurrentLng()) <= radiusKm)
                .sorted(Comparator.comparingDouble(d -> GeoUtil.distanceKm(lat, lng, d.getCurrentLat(), d.getCurrentLng())))
                .limit(limit)
                .toList();
    }
}
