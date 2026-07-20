package vn.taxi.driver.domain;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "drivers")
public class Driver {

    @Id
    @GeneratedValue
    private UUID id;

    /** ID user tương ứng bên auth-service (không có FK vật lý — khác database). */
    @Column(nullable = false, unique = true)
    private UUID userId;

    @Column(nullable = false)
    private String fullName;

    @Column(nullable = false, unique = true)
    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DriverStatus status = DriverStatus.ACTIVE;

    /** Cổng duyệt hồ sơ — chỉ VERIFIED mới được lên ca / được tìm thấy khi dispatch. */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VerificationStatus verificationStatus = VerificationStatus.PENDING;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ShiftStatus shiftStatus = ShiftStatus.OFF;

    @Column(nullable = false)
    private Double rating = 5.0;

    @ManyToOne
    @JoinColumn(name = "team_id")
    private DriverTeam team;

    private Double currentLat;
    private Double currentLng;
    private Instant locationUpdatedAt;

    @Column(nullable = false)
    private Instant createdAt = Instant.now();

    protected Driver() {
    }

    public Driver(UUID userId, String fullName, String phone) {
        this.userId = userId;
        this.fullName = fullName;
        this.phone = phone;
    }

    public UUID getId() {
        return id;
    }

    public UUID getUserId() {
        return userId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getPhone() {
        return phone;
    }

    public DriverStatus getStatus() {
        return status;
    }

    public void setStatus(DriverStatus status) {
        this.status = status;
    }

    public VerificationStatus getVerificationStatus() {
        return verificationStatus;
    }

    public void setVerificationStatus(VerificationStatus verificationStatus) {
        this.verificationStatus = verificationStatus;
    }

    public ShiftStatus getShiftStatus() {
        return shiftStatus;
    }

    public void setShiftStatus(ShiftStatus shiftStatus) {
        this.shiftStatus = shiftStatus;
    }

    public Double getRating() {
        return rating;
    }

    public DriverTeam getTeam() {
        return team;
    }

    public void setTeam(DriverTeam team) {
        this.team = team;
    }

    public Double getCurrentLat() {
        return currentLat;
    }

    public Double getCurrentLng() {
        return currentLng;
    }

    public void updateLocation(Double lat, Double lng) {
        this.currentLat = lat;
        this.currentLng = lng;
        this.locationUpdatedAt = Instant.now();
    }

    public Instant getLocationUpdatedAt() {
        return locationUpdatedAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
