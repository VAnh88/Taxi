package vn.taxi.driver.web;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import vn.taxi.driver.domain.Driver;
import vn.taxi.driver.domain.ShiftStatus;
import vn.taxi.driver.domain.VerificationStatus;
import vn.taxi.driver.service.DriverService;
import vn.taxi.driver.web.dto.DriverResponse;
import vn.taxi.driver.web.dto.UpdateShiftRequest;
import vn.taxi.driver.web.dto.UpdateVerificationRequest;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/drivers")
public class DriverController {

    private final DriverService driverService;

    public DriverController(DriverService driverService) {
        this.driverService = driverService;
    }

    @GetMapping
    public List<DriverResponse> list() {
        return driverService.findAll().stream().map(DriverResponse::from).toList();
    }

    @GetMapping("/{id}")
    public DriverResponse get(@PathVariable UUID id) {
        return DriverResponse.from(driverService.getById(id));
    }

    /** Tài xế tự tra hồ sơ của mình qua header X-User-Id (gateway gắn từ JWT). */
    @GetMapping("/me")
    public DriverResponse me(@RequestHeader("X-User-Id") String userId) {
        return DriverResponse.from(driverService.getByUserId(UUID.fromString(userId)));
    }

    @PatchMapping("/{id}/shift")
    public DriverResponse updateShift(@PathVariable UUID id, @Valid @RequestBody UpdateShiftRequest request) {
        Driver driver = driverService.updateShift(id, ShiftStatus.valueOf(request.shiftStatus().toUpperCase()),
                request.lat(), request.lng());
        return DriverResponse.from(driver);
    }

    /** Admin duyệt/từ chối hồ sơ tài xế — bắt buộc VERIFIED trước khi tài xế được lên ca. */
    @PatchMapping("/{id}/verification")
    public DriverResponse updateVerification(@PathVariable UUID id, @Valid @RequestBody UpdateVerificationRequest request) {
        Driver driver = driverService.updateVerification(id, VerificationStatus.valueOf(request.verificationStatus().toUpperCase()));
        return DriverResponse.from(driver);
    }

    /** Dùng bởi trip-service (qua Feign) để tìm tài xế gần nhất khi dispatch cuốc mới. */
    @GetMapping("/available")
    public List<DriverResponse> available(@RequestParam double lat,
                                           @RequestParam double lng,
                                           @RequestParam(defaultValue = "5") double radiusKm,
                                           @RequestParam(defaultValue = "10") int limit) {
        return driverService.findAvailableNear(lat, lng, radiusKm, limit).stream()
                .map(DriverResponse::from).toList();
    }
}
