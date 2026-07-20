package vn.taxi.customer.domain;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "customers")
public class Customer {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false, unique = true)
    private UUID userId;

    @Column(nullable = false)
    private String fullName;

    @Column(nullable = false, unique = true)
    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CustomerType type = CustomerType.APP;

    /** Bắt buộc khi type = BLACKLIST — phục vụ đối soát khi khách khiếu nại bị chặn. */
    private String blacklistReason;

    @Column(nullable = false)
    private Instant createdAt = Instant.now();

    protected Customer() {
    }

    public Customer(UUID userId, String fullName, String phone) {
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

    public CustomerType getType() {
        return type;
    }

    public void setType(CustomerType type, String blacklistReason) {
        this.type = type;
        this.blacklistReason = type == CustomerType.BLACKLIST ? blacklistReason : null;
    }

    public String getBlacklistReason() {
        return blacklistReason;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
