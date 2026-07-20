package vn.taxi.driver.web;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.taxi.driver.repository.VehicleTypeRepository;
import vn.taxi.driver.web.dto.VehicleTypeResponse;

import java.util.List;

@RestController
@RequestMapping("/api/vehicle-types")
public class VehicleTypeController {

    private final VehicleTypeRepository vehicleTypeRepository;

    public VehicleTypeController(VehicleTypeRepository vehicleTypeRepository) {
        this.vehicleTypeRepository = vehicleTypeRepository;
    }

    @GetMapping
    public List<VehicleTypeResponse> list() {
        return vehicleTypeRepository.findAll().stream().map(VehicleTypeResponse::from).toList();
    }
}
