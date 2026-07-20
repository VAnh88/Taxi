package vn.taxi.driver.domain;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "driver_documents")
public class DriverDocument {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "driver_id", nullable = false)
    private Driver driver;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DocumentType docType;

    @Column(nullable = false)
    private String fileUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VerificationStatus verifyStatus = VerificationStatus.PENDING;

    @Column(nullable = false)
    private Instant createdAt = Instant.now();

    protected DriverDocument() {
    }

    public DriverDocument(Driver driver, DocumentType docType, String fileUrl) {
        this.driver = driver;
        this.docType = docType;
        this.fileUrl = fileUrl;
    }

    public UUID getId() {
        return id;
    }

    public Driver getDriver() {
        return driver;
    }

    public DocumentType getDocType() {
        return docType;
    }

    public String getFileUrl() {
        return fileUrl;
    }

    public VerificationStatus getVerifyStatus() {
        return verifyStatus;
    }

    public void setVerifyStatus(VerificationStatus verifyStatus) {
        this.verifyStatus = verifyStatus;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
