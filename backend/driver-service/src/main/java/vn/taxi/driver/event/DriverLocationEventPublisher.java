package vn.taxi.driver.event;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import vn.taxi.driver.domain.Driver;

@Component
public class DriverLocationEventPublisher {

    public static final String TOPIC = "driver.location-changed";

    private final KafkaTemplate<String, DriverLocationChangedEvent> kafkaTemplate;

    public DriverLocationEventPublisher(KafkaTemplate<String, DriverLocationChangedEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publish(Driver driver) {
        DriverLocationChangedEvent event = new DriverLocationChangedEvent(
                driver.getId(), driver.getCurrentLat(), driver.getCurrentLng(),
                driver.getShiftStatus().name(), driver.getStatus().name()
        );
        kafkaTemplate.send(TOPIC, driver.getId().toString(), event);
    }
}
