package vn.taxi.trip.domain;

import jakarta.persistence.*;

import java.util.UUID;

/** Danh mục lý do hủy chuyến — map từ khảo sát Thiên Đức mục "Các lý do hủy chuyến". */
@Entity
@Table(name = "cancel_reasons")
public class CancelReason {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false)
    private String nameVi;

    private String nameEn;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CancelReasonActor appliesTo;

    @Column(nullable = false)
    private boolean active = true;

    protected CancelReason() {
    }

    public CancelReason(String nameVi, String nameEn, CancelReasonActor appliesTo) {
        this.nameVi = nameVi;
        this.nameEn = nameEn;
        this.appliesTo = appliesTo;
    }

    public UUID getId() {
        return id;
    }

    public String getNameVi() {
        return nameVi;
    }

    public String getNameEn() {
        return nameEn;
    }

    public CancelReasonActor getAppliesTo() {
        return appliesTo;
    }

    public boolean isActive() {
        return active;
    }
}
