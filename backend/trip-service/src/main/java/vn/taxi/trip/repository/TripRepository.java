package vn.taxi.trip.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import vn.taxi.trip.domain.Trip;

import java.util.List;
import java.util.UUID;

public interface TripRepository extends JpaRepository<Trip, UUID> {
    List<Trip> findByCustomerId(UUID customerId);
    List<Trip> findByDriverId(UUID driverId);
    List<Trip> findAllByOrderByRequestedAtDesc(Pageable pageable);
}
