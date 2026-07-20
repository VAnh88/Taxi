package vn.taxi.driver.domain;

import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(name = "vehicles")
public class Vehicle {

    @Id
    @GeneratedValue
    private UUID id;

    /** unique: cố định 1 tài xế = 1 xe (quyết định nghiệp vụ đã chốt). */
    @OneToOne(optional = false)
    @JoinColumn(name = "driver_id", nullable = false, unique = true)
    private Driver driver;

    @ManyToOne(optional = false)
    @JoinColumn(name = "vehicle_type_id", nullable = false)
    private VehicleType vehicleType;

    @Column(nullable = false, unique = true)
    private String plateNumber;

    @Column(nullable = false)
    private String brand;

    @Column(nullable = false)
    private String model;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VehicleStatus status = VehicleStatus.ACTIVE;

    protected Vehicle() {
    }

    public Vehicle(Driver driver, VehicleType vehicleType, String plateNumber, String brand, String model) {
        this.driver = driver;
        this.vehicleType = vehicleType;
        this.plateNumber = plateNumber;
        this.brand = brand;
        this.model = model;
    }

    public UUID getId() {
        return id;
    }

    public Driver getDriver() {
        return driver;
    }

    public VehicleType getVehicleType() {
        return vehicleType;
    }

    public String getPlateNumber() {
        return plateNumber;
    }

    public String getBrand() {
        return brand;
    }

    public String getModel() {
        return model;
    }

    public VehicleStatus getStatus() {
        return status;
    }

    public void setStatus(VehicleStatus status) {
        this.status = status;
    }
}
