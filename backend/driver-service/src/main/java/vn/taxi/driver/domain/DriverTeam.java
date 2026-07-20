package vn.taxi.driver.domain;

import jakarta.persistence.*;

import java.util.UUID;

/**
 * Map trực tiếp từ mục "Đội xe - Nhà thầu" trong khảo sát Thiên Đức
 * (5 đội xe cố định: Thiên Đức nội bộ/ngoài, Vinh Hà, HTX Vận tải Thiên Đức, Đội Ali + Quản lý).
 */
@Entity
@Table(name = "driver_teams")
public class DriverTeam {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false, unique = true)
    private String name;

    protected DriverTeam() {
    }

    public DriverTeam(String name) {
        this.name = name;
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
