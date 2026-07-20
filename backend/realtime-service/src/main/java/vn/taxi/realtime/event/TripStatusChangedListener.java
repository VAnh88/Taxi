package vn.taxi.realtime.event;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
public class TripStatusChangedListener {

    private final SimpMessagingTemplate messagingTemplate;

    public TripStatusChangedListener(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @KafkaListener(
            topics = "trip.status-changed",
            groupId = "realtime-service",
            containerFactory = "tripStatusChangedListenerFactory"
    )
    public void onTripStatusChanged(TripStatusChangedEvent event) {
        // Khách hàng + tài xế đang theo dõi chuyến subscribe kênh này.
        messagingTemplate.convertAndSend("/topic/trip/" + event.tripId(), event);

        // Khi vừa gán tài xế, đẩy thông báo "cuốc mới" riêng cho tài xế đó.
        if ("DRIVER_ASSIGNED".equals(event.status()) && event.driverId() != null) {
            messagingTemplate.convertAndSend("/topic/driver/" + event.driverId() + "/incoming-trip", event);
        }

        // Bảng theo dõi tổng của dispatcher-web — không cần biết trước tripId.
        messagingTemplate.convertAndSend("/topic/dispatch/trip-updates", event);
    }
}
