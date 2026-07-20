package vn.taxi.trip.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.taxi.trip.domain.TripStatusHistory;

import java.util.List;
import java.util.UUID;

public interface TripStatusHistoryRepository extends JpaRepository<TripStatusHistory, UUID> {
    List<TripStatusHistory> findByTripIdOrderByChangedAtAsc(UUID tripId);
}
