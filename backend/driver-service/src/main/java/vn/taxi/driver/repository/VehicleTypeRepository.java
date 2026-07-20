package vn.taxi.driver.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.taxi.driver.domain.VehicleType;

import java.util.Optional;
import java.util.UUID;

public interface VehicleTypeRepository extends JpaRepository<VehicleType, UUID> {
    Optional<VehicleType> findByCode(String code);
}
