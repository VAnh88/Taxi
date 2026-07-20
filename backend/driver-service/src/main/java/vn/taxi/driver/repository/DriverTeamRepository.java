package vn.taxi.driver.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.taxi.driver.domain.DriverTeam;

import java.util.UUID;

public interface DriverTeamRepository extends JpaRepository<DriverTeam, UUID> {
}
