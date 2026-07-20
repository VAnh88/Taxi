package vn.taxi.auth.event;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class UserEventPublisher {

    public static final String TOPIC = "user.created";

    private final KafkaTemplate<String, UserCreatedEvent> kafkaTemplate;

    public UserEventPublisher(KafkaTemplate<String, UserCreatedEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publish(UserCreatedEvent event) {
        kafkaTemplate.send(TOPIC, event.userId().toString(), event);
    }
}
