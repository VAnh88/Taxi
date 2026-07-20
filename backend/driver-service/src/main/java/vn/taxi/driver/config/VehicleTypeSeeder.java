package vn.taxi.driver.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import vn.taxi.driver.domain.VehicleType;
import vn.taxi.driver.repository.VehicleTypeRepository;

/** Seed danh mục loại xe từ khảo sát Thiên Đức — chỉ chạy khi bảng còn trống. */
@Component
public class VehicleTypeSeeder implements CommandLineRunner {

    private final VehicleTypeRepository vehicleTypeRepository;

    public VehicleTypeSeeder(VehicleTypeRepository vehicleTypeRepository) {
        this.vehicleTypeRepository = vehicleTypeRepository;
    }

    @Override
    public void run(String... args) {
        if (vehicleTypeRepository.count() > 0) {
            return;
        }
        vehicleTypeRepository.save(new VehicleType("XE_4CHO_NHO", "Xe 4 chỗ nhỏ", 4));
        vehicleTypeRepository.save(new VehicleType("XE_4CHO_LON", "Xe 4 chỗ lớn (PRE)", 4));
        vehicleTypeRepository.save(new VehicleType("XE_7CHO_NHO", "Xe 7 chỗ nhỏ", 7));
        vehicleTypeRepository.save(new VehicleType("XE_7CHO_LON", "Xe 7 chỗ lớn (PRE)", 7));
    }
}
