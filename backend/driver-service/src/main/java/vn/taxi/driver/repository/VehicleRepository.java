package vn.taxi.driver.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.taxi.driver.domain.Vehicle;

import java.util.Optional;
import java.util.UUID;

public interface VehicleRepository extends JpaRepository<Vehicle, UUID> {
    /** 1 tài xế tối đa 1 xe (quyết định nghiệp vụ đã chốt). */
    Optional<Vehicle> findByDriverId(UUID driverId);

    boolean existsByDriverId(UUID driverId);
}
