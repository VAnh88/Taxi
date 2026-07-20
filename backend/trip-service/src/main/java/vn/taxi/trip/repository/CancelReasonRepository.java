package vn.taxi.trip.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.taxi.trip.domain.CancelReason;

import java.util.UUID;

public interface CancelReasonRepository extends JpaRepository<CancelReason, UUID> {
}
