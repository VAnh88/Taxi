package vn.taxi.driver.domain;

import jakarta.persistence.*;

import java.util.UUID;

/** Danh mục cỡ/loại xe — map từ khảo sát Thiên Đức mục "Taxi Type" (XE 4 CHỖ NHỎ, XE 4 CHỖ LỚN...). */
@Entity
@Table(name = "vehicle_types")
public class VehicleType {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false, unique = true)
    private String code;

    @Column(nullable = false)
    private String nameVi;

    @Column(nullable = false)
    private int seatCount;

    protected VehicleType() {
    }

    public VehicleType(String code, String nameVi, int seatCount) {
        this.code = code;
        this.nameVi = nameVi;
        this.seatCount = seatCount;
    }

    public UUID getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public String getNameVi() {
        return nameVi;
    }

    public int getSeatCount() {
        return seatCount;
    }
}
