package vn.taxi.customer.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.taxi.customer.domain.Customer;

import java.util.Optional;
import java.util.UUID;

public interface CustomerRepository extends JpaRepository<Customer, UUID> {
    Optional<Customer> findByUserId(UUID userId);
}
