package vn.taxi.trip.web;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import vn.taxi.trip.domain.Trip;
import vn.taxi.trip.domain.TripStatus;
import vn.taxi.trip.service.TripService;
import vn.taxi.trip.web.dto.AssignDriverRequest;
import vn.taxi.trip.web.dto.CancelTripRequest;
import vn.taxi.trip.web.dto.CreateTripRequest;
import vn.taxi.trip.web.dto.RateTripRequest;
import vn.taxi.trip.web.dto.TripResponse;
import vn.taxi.trip.web.dto.UpdateTripStatusRequest;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/api/trips")
public class TripController {

    private static final Set<String> PRIVILEGED_ROLES = Set.of("DISPATCHER", "ADMIN");

    private final TripService tripService;

    public TripController(TripService tripService) {
        this.tripService = tripService;
    }

    @PostMapping
    public ResponseEntity<TripResponse> create(@RequestHeader("X-User-Id") String userId,
                                                @RequestHeader(value = "X-User-Role", required = false) String role,
                                                @Valid @RequestBody CreateTripRequest request) {
        UUID customerId;
        String callerPhone = null;
        String callerName = null;

        if (PRIVILEGED_ROLES.contains(role)) {
            // Tổng đài/admin: có thể tạo hộ khách đã có tài khoản (customerId) hoặc khách vãng lai gọi điện (callerPhone).
            if (request.customerId() != null) {
                customerId = UUID.fromString(request.customerId());
            } else if (request.callerPhone() != null && !request.callerPhone().isBlank()) {
                customerId = null;
                callerPhone = request.callerPhone();
                callerName = request.callerName();
            } else {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Cần customerId (khách đã có tài khoản) hoặc callerPhone (khách gọi điện)");
            }
        } else {
            // App khách hàng: resolve customerId thật từ customer-service qua JWT userId, không tin dữ liệu client tự khai.
            customerId = tripService.resolveCustomerId(UUID.fromString(userId));
        }

        Trip trip = tripService.create(customerId, callerPhone, callerName, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(TripResponse.from(trip));
    }

    @GetMapping("/{id}")
    public TripResponse get(@PathVariable UUID id) {
        return TripResponse.from(tripService.getById(id));
    }

    @GetMapping
    public List<TripResponse> list(@RequestParam(required = false) UUID customerId,
                                    @RequestParam(required = false) UUID driverId) {
        List<Trip> trips;
        if (customerId != null) {
            trips = tripService.findByCustomer(customerId);
        } else if (driverId != null) {
            trips = tripService.findByDriver(driverId);
        } else {
            trips = tripService.findRecent();
        }
        return trips.stream().map(TripResponse::from).toList();
    }

    @PatchMapping("/{id}/status")
    public TripResponse updateStatus(@PathVariable UUID id,
                                      @RequestHeader(value = "X-User-Id", required = false) String userId,
                                      @Valid @RequestBody UpdateTripStatusRequest request) {
        Trip trip = tripService.updateStatus(id, TripStatus.valueOf(request.status().toUpperCase()), parseUserId(userId));
        return TripResponse.from(trip);
    }

    /** Hủy chuyến — bắt buộc lý do trong danh mục /api/cancel-reasons (không giới hạn số lần ở MVP). */
    @PostMapping("/{id}/cancel")
    public TripResponse cancel(@PathVariable UUID id,
                                @RequestHeader(value = "X-User-Id", required = false) String userId,
                                @Valid @RequestBody CancelTripRequest request) {
        Trip trip = tripService.cancel(id, TripStatus.valueOf(request.status().toUpperCase()),
                UUID.fromString(request.cancelReasonId()), request.cancelNote(), parseUserId(userId));
        return TripResponse.from(trip);
    }

    /** Khách đánh giá tài xế sau khi hoàn tất — 1 chiều. */
    @PostMapping("/{id}/rating")
    public TripResponse rate(@PathVariable UUID id, @Valid @RequestBody RateTripRequest request) {
        Trip trip = tripService.rate(id, request.rating(), request.comment());
        return TripResponse.from(trip);
    }

    /** Tổng đài chọn tay 1 xe cụ thể trên bản đồ để gán/ghi đè cho cuốc — chỉ DISPATCHER/ADMIN. */
    @PostMapping("/{id}/assign")
    public TripResponse assign(@PathVariable UUID id,
                                @RequestHeader(value = "X-User-Id", required = false) String userId,
                                @RequestHeader(value = "X-User-Role", required = false) String role,
                                @Valid @RequestBody AssignDriverRequest request) {
        if (!PRIVILEGED_ROLES.contains(role)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Chỉ tổng đài/admin được gán tay tài xế");
        }
        Trip trip = tripService.manualAssign(id, UUID.fromString(request.driverId()), parseUserId(userId));
        return TripResponse.from(trip);
    }

    private UUID parseUserId(String userId) {
        return userId != null ? UUID.fromString(userId) : null;
    }
}
