package vn.taxi.realtime.event;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

/** Đẩy vị trí xe realtime cho dispatcher-web — bản đồ theo dõi toàn bộ đội xe. */
@Component
public class DriverLocationChangedListener {

    private final SimpMessagingTemplate messagingTemplate;

    public DriverLocationChangedListener(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @KafkaListener(
            topics = "driver.location-changed",
            groupId = "realtime-service",
            containerFactory = "driverLocationChangedListenerFactory"
    )
    public void onDriverLocationChanged(DriverLocationChangedEvent event) {
        messagingTemplate.convertAndSend("/topic/fleet/locations", event);
    }
}
