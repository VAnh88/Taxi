package vn.taxi.auth.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.taxi.auth.domain.User;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByUsername(String username);
    boolean existsByUsername(String username);
    boolean existsByPhone(String phone);
}
