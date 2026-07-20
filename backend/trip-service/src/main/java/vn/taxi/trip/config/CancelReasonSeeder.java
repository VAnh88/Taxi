package vn.taxi.trip.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import vn.taxi.trip.domain.CancelReason;
import vn.taxi.trip.domain.CancelReasonActor;
import vn.taxi.trip.repository.CancelReasonRepository;

/** Seed từ khảo sát Thiên Đức mục "Các lý do hủy chuyến" — chỉ chạy khi bảng còn trống. */
@Component
public class CancelReasonSeeder implements CommandLineRunner {

    private final CancelReasonRepository cancelReasonRepository;

    public CancelReasonSeeder(CancelReasonRepository cancelReasonRepository) {
        this.cancelReasonRepository = cancelReasonRepository;
    }

    @Override
    public void run(String... args) {
        if (cancelReasonRepository.count() > 0) {
            return;
        }
        cancelReasonRepository.save(new CancelReason("Khách hàng đã có xe khác đón", "Customer already has another ride", CancelReasonActor.ALL));
        cancelReasonRepository.save(new CancelReason("Không liên lạc được với khách", "Cannot reach customer", CancelReasonActor.DRIVER));
        cancelReasonRepository.save(new CancelReason("Xa điểm, không đón khách", "Too far, cannot pick up", CancelReasonActor.DRIVER));
    }
}
