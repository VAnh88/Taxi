package vn.taxi.trip.event;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import vn.taxi.trip.domain.Trip;

@Component
public class TripEventPublisher {

    public static final String TOPIC = "trip.status-changed";

    private final KafkaTemplate<String, TripStatusChangedEvent> kafkaTemplate;

    public TripEventPublisher(KafkaTemplate<String, TripStatusChangedEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publish(Trip trip) {
        TripStatusChangedEvent event = new TripStatusChangedEvent(
                trip.getId(), trip.getCustomerId(), trip.getDriverId(), trip.getStatus().name()
        );
        kafkaTemplate.send(TOPIC, trip.getId().toString(), event);
    }
}
