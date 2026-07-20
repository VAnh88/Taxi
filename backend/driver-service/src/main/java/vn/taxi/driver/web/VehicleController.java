package vn.taxi.driver.web;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import vn.taxi.driver.domain.Driver;
import vn.taxi.driver.domain.Vehicle;
import vn.taxi.driver.domain.VehicleType;
import vn.taxi.driver.repository.VehicleRepository;
import vn.taxi.driver.repository.VehicleTypeRepository;
import vn.taxi.driver.service.DriverService;
import vn.taxi.driver.web.dto.CreateVehicleRequest;
import vn.taxi.driver.web.dto.VehicleResponse;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/drivers/{driverId}/vehicles")
public class VehicleController {

    private final VehicleRepository vehicleRepository;
    private final VehicleTypeRepository vehicleTypeRepository;
    private final DriverService driverService;

    public VehicleController(VehicleRepository vehicleRepository, VehicleTypeRepository vehicleTypeRepository,
                              DriverService driverService) {
        this.vehicleRepository = vehicleRepository;
        this.vehicleTypeRepository = vehicleTypeRepository;
        this.driverService = driverService;
    }

    @GetMapping
    public List<VehicleResponse> list(@PathVariable UUID driverId) {
        return vehicleRepository.findByDriverId(driverId).map(VehicleResponse::from).map(List::of).orElseGet(List::of);
    }

    /** 1 tài xế tối đa 1 xe (quyết định nghiệp vụ đã chốt) — từ chối nếu đã có xe. */
    @PostMapping
    public ResponseEntity<VehicleResponse> create(@PathVariable UUID driverId,
                                                    @Valid @RequestBody CreateVehicleRequest request) {
        if (vehicleRepository.existsByDriverId(driverId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Tài xế đã có xe — mỗi tài xế chỉ được gắn 1 xe");
        }
        Driver driver = driverService.getById(driverId);
        VehicleType vehicleType = vehicleTypeRepository.findByCode(request.vehicleTypeCode())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Không tìm thấy loại xe " + request.vehicleTypeCode()));
        Vehicle vehicle = new Vehicle(driver, vehicleType, request.plateNumber(), request.brand(), request.model());
        vehicle = vehicleRepository.save(vehicle);
        return ResponseEntity.status(HttpStatus.CREATED).body(VehicleResponse.from(vehicle));
    }
}
