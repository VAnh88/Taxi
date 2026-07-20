package vn.taxi.driver.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.taxi.driver.domain.DriverDocument;

import java.util.List;
import java.util.UUID;

public interface DriverDocumentRepository extends JpaRepository<DriverDocument, UUID> {
    List<DriverDocument> findByDriverId(UUID driverId);
}
