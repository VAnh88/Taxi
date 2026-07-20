package vn.taxi.trip.domain;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

/** Log mọi lần đổi trạng thái — audit khiếu nại, báo cáo thời gian xử lý từng bước. */
@Entity
@Table(name = "trip_status_history")
public class TripStatusHistory {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false)
    private UUID tripId;

    @Enumerated(EnumType.STRING)
    private TripStatus fromStatus;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TripStatus toStatus;

    /** Null khi hệ thống tự đổi (vd. auto-assign lúc tạo trip). */
    private UUID changedByUserId;

    @Column(nullable = false)
    private Instant changedAt = Instant.now();

    protected TripStatusHistory() {
    }

    public TripStatusHistory(UUID tripId, TripStatus fromStatus, TripStatus toStatus, UUID changedByUserId) {
        this.tripId = tripId;
        this.fromStatus = fromStatus;
        this.toStatus = toStatus;
        this.changedByUserId = changedByUserId;
    }

    public UUID getId() {
        return id;
    }

    public UUID getTripId() {
        return tripId;
    }

    public TripStatus getFromStatus() {
        return fromStatus;
    }

    public TripStatus getToStatus() {
        return toStatus;
    }

    public UUID getChangedByUserId() {
        return changedByUserId;
    }

    public Instant getChangedAt() {
        return changedAt;
    }
}
