package vn.taxi.driver.event;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import vn.taxi.driver.domain.Driver;
import vn.taxi.driver.repository.DriverRepository;

@Component
public class UserCreatedEventListener {

    private final DriverRepository driverRepository;

    public UserCreatedEventListener(DriverRepository driverRepository) {
        this.driverRepository = driverRepository;
    }

    @KafkaListener(topics = "user.created", groupId = "driver-service")
    public void onUserCreated(UserCreatedEvent event) {
        if (!"DRIVER".equalsIgnoreCase(event.role())) {
            return;
        }
        if (driverRepository.findByUserId(event.userId()).isPresent()) {
            return;
        }
        Driver driver = new Driver(event.userId(), event.username(), event.phone());
        driverRepository.save(driver);
    }
}
