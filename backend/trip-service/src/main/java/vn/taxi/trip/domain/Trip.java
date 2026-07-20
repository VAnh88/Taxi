package vn.taxi.trip.domain;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "trips")
public class Trip {

    @Id
    @GeneratedValue
    private UUID id;

    /** Null khi khách vãng lai gọi tổng đài chưa có tài khoản — dùng callerPhone/callerName thay thế. */
    private UUID customerId;

    /** Bắt buộc khi customerId null (trip do DISPATCHER/ADMIN tạo hộ khách gọi điện). */
    private String callerPhone;
    private String callerName;

    private UUID driverId;

    @Column(nullable = false)
    private String pickupAddress;
    @Column(nullable = false)
    private Double pickupLat;
    @Column(nullable = false)
    private Double pickupLng;

    @Column(nullable = false)
    private String dropoffAddress;
    @Column(nullable = false)
    private Double dropoffLat;
    @Column(nullable = false)
    private Double dropoffLng;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TripStatus status = TripStatus.REQUESTED;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SourceChannel sourceChannel;

    private Long price;
    private Double distanceKm;

    private UUID cancelReasonId;
    private String cancelNote;

    private Short customerRating;
    private String customerRatingComment;

    @Column(nullable = false)
    private Instant requestedAt = Instant.now();
    private Instant assignedAt;
    private Instant arrivingAt;
    private Instant onboardAt;
    private Instant completedAt;
    private Instant cancelledAt;

    protected Trip() {
    }

    public Trip(UUID customerId, String callerPhone, String callerName,
                String pickupAddress, Double pickupLat, Double pickupLng,
                String dropoffAddress, Double dropoffLat, Double dropoffLng, SourceChannel sourceChannel) {
        this.customerId = customerId;
        this.callerPhone = callerPhone;
        this.callerName = callerName;
        this.pickupAddress = pickupAddress;
        this.pickupLat = pickupLat;
        this.pickupLng = pickupLng;
        this.dropoffAddress = dropoffAddress;
        this.dropoffLat = dropoffLat;
        this.dropoffLng = dropoffLng;
        this.sourceChannel = sourceChannel;
    }

    public UUID getId() {
        return id;
    }

    public UUID getCustomerId() {
        return customerId;
    }

    public String getCallerPhone() {
        return callerPhone;
    }

    public String getCallerName() {
        return callerName;
    }

    public UUID getDriverId() {
        return driverId;
    }

    public void assignDriver(UUID driverId) {
        this.driverId = driverId;
        this.status = TripStatus.DRIVER_ASSIGNED;
        this.assignedAt = Instant.now();
    }

    public String getPickupAddress() {
        return pickupAddress;
    }

    public Double getPickupLat() {
        return pickupLat;
    }

    public Double getPickupLng() {
        return pickupLng;
    }

    public String getDropoffAddress() {
        return dropoffAddress;
    }

    public Double getDropoffLat() {
        return dropoffLat;
    }

    public Double getDropoffLng() {
        return dropoffLng;
    }

    public TripStatus getStatus() {
        return status;
    }

    public void setStatus(TripStatus status) {
        this.status = status;
        Instant now = Instant.now();
        switch (status) {
            case DRIVER_ARRIVING -> arrivingAt = now;
            case CUSTOMER_ONBOARD -> onboardAt = now;
            case COMPLETED -> completedAt = now;
            case CANCELLED_BY_CUSTOMER, CANCELLED_BY_DRIVER, CANCELLED_BY_DISPATCHER -> cancelledAt = now;
            default -> { /* no timestamp tracked */ }
        }
    }

    /** Hủy chuyến — bắt buộc lý do (quyết định nghiệp vụ đã chốt: không giới hạn số lần, nhưng luôn phải ghi lý do). */
    public void cancel(TripStatus cancelStatus, UUID cancelReasonId, String cancelNote) {
        setStatus(cancelStatus);
        this.cancelReasonId = cancelReasonId;
        this.cancelNote = cancelNote;
    }

    public UUID getCancelReasonId() {
        return cancelReasonId;
    }

    public String getCancelNote() {
        return cancelNote;
    }

    public void rate(short rating, String comment) {
        this.customerRating = rating;
        this.customerRatingComment = comment;
    }

    public Short getCustomerRating() {
        return customerRating;
    }

    public String getCustomerRatingComment() {
        return customerRatingComment;
    }

    public Double getDistanceKm() {
        return distanceKm;
    }

    public void setDistanceKm(Double distanceKm) {
        this.distanceKm = distanceKm;
    }

    public Instant getCancelledAt() {
        return cancelledAt;
    }

    public SourceChannel getSourceChannel() {
        return sourceChannel;
    }

    public Long getPrice() {
        return price;
    }

    public void setPrice(Long price) {
        this.price = price;
    }

    public Instant getRequestedAt() {
        return requestedAt;
    }

    public Instant getAssignedAt() {
        return assignedAt;
    }

    public Instant getArrivingAt() {
        return arrivingAt;
    }

    public Instant getOnboardAt() {
        return onboardAt;
    }

    public Instant getCompletedAt() {
        return completedAt;
    }
}
