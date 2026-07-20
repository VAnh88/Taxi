package vn.taxi.trip.service;

import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import vn.taxi.trip.client.AvailableDriverDto;
import vn.taxi.trip.client.CustomerClient;
import vn.taxi.trip.client.DriverClient;
import vn.taxi.trip.domain.SourceChannel;
import vn.taxi.trip.domain.Trip;
import vn.taxi.trip.domain.TripStatus;
import vn.taxi.trip.domain.TripStatusHistory;
import vn.taxi.trip.event.TripEventPublisher;
import vn.taxi.trip.repository.CancelReasonRepository;
import vn.taxi.trip.repository.TripRepository;
import vn.taxi.trip.repository.TripStatusHistoryRepository;
import vn.taxi.trip.web.dto.CreateTripRequest;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
public class TripService {

    private static final double DEFAULT_SEARCH_RADIUS_KM = 5.0;
    private static final long BASE_FARE = 15_000L;
    private static final long FARE_PER_KM = 10_000L;

    private static final Set<TripStatus> CANCEL_STATUSES = EnumSet.of(
            TripStatus.CANCELLED_BY_CUSTOMER, TripStatus.CANCELLED_BY_DRIVER, TripStatus.CANCELLED_BY_DISPATCHER);

    /** Bảng chuyển trạng thái hợp lệ — chặn PATCH status nhảy cóc/ngược. */
    private static final Map<TripStatus, Set<TripStatus>> ALLOWED_TRANSITIONS = new EnumMap<>(TripStatus.class);

    static {
        ALLOWED_TRANSITIONS.put(TripStatus.DRIVER_ASSIGNED,
                EnumSet.of(TripStatus.DRIVER_ARRIVING, TripStatus.CANCELLED_BY_CUSTOMER,
                        TripStatus.CANCELLED_BY_DRIVER, TripStatus.CANCELLED_BY_DISPATCHER));
        ALLOWED_TRANSITIONS.put(TripStatus.DRIVER_ARRIVING,
                EnumSet.of(TripStatus.CUSTOMER_ONBOARD, TripStatus.CANCELLED_BY_CUSTOMER,
                        TripStatus.CANCELLED_BY_DRIVER, TripStatus.CANCELLED_BY_DISPATCHER));
        ALLOWED_TRANSITIONS.put(TripStatus.CUSTOMER_ONBOARD,
                EnumSet.of(TripStatus.COMPLETED, TripStatus.CANCELLED_BY_DISPATCHER));
    }

    private final TripRepository tripRepository;
    private final CancelReasonRepository cancelReasonRepository;
    private final TripStatusHistoryRepository tripStatusHistoryRepository;
    private final DriverClient driverClient;
    private final CustomerClient customerClient;
    private final TripEventPublisher tripEventPublisher;

    public TripService(TripRepository tripRepository, CancelReasonRepository cancelReasonRepository,
                        TripStatusHistoryRepository tripStatusHistoryRepository, DriverClient driverClient,
                        CustomerClient customerClient, TripEventPublisher tripEventPublisher) {
        this.tripRepository = tripRepository;
        this.cancelReasonRepository = cancelReasonRepository;
        this.tripStatusHistoryRepository = tripStatusHistoryRepository;
        this.driverClient = driverClient;
        this.customerClient = customerClient;
        this.tripEventPublisher = tripEventPublisher;
    }

    /** Khách đặt xe qua app dùng userId (auth) — resolve sang customer-service's Customer.id thật. */
    public UUID resolveCustomerId(UUID authUserId) {
        try {
            return customerClient.getByUserId(authUserId).id();
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy hồ sơ khách hàng cho user này");
        }
    }

    public Trip create(UUID customerId, String callerPhone, String callerName, CreateTripRequest request) {
        Trip trip = new Trip(
                customerId, callerPhone, callerName,
                request.pickupAddress(), request.pickupLat(), request.pickupLng(),
                request.dropoffAddress(), request.dropoffLat(), request.dropoffLng(),
                SourceChannel.valueOf(request.sourceChannel().toUpperCase())
        );
        trip = tripRepository.save(trip);
        recordHistory(trip, null, trip.getStatus(), null);
        tripEventPublisher.publish(trip);

        tryAssignNearestDriver(trip);
        return trip;
    }

    /** Tổng đài chọn tay 1 tài xế cụ thể — ghi đè tài xế auto-gán, hoặc gán khi trước đó NO_DRIVER_AVAILABLE. */
    public Trip manualAssign(UUID tripId, UUID driverId, UUID actorUserId) {
        Trip trip = getById(tripId);
        Set<TripStatus> allowedFrom = EnumSet.of(TripStatus.REQUESTED, TripStatus.NO_DRIVER_AVAILABLE, TripStatus.DRIVER_ASSIGNED);
        if (!allowedFrom.contains(trip.getStatus())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Không thể gán tay tài xế khi chuyến đang ở trạng thái " + trip.getStatus());
        }
        TripStatus from = trip.getStatus();
        trip.assignDriver(driverId);
        trip = tripRepository.save(trip);
        recordHistory(trip, from, trip.getStatus(), actorUserId);
        tripEventPublisher.publish(trip);
        return trip;
    }

    private void tryAssignNearestDriver(Trip trip) {
        List<AvailableDriverDto> candidates;
        try {
            candidates = driverClient.findAvailable(trip.getPickupLat(), trip.getPickupLng(),
                    DEFAULT_SEARCH_RADIUS_KM, 1);
        } catch (Exception e) {
            candidates = List.of();
        }

        TripStatus from = trip.getStatus();
        if (candidates.isEmpty()) {
            trip.setStatus(TripStatus.NO_DRIVER_AVAILABLE);
        } else {
            trip.assignDriver(candidates.get(0).id());
        }
        trip = tripRepository.save(trip);
        recordHistory(trip, from, trip.getStatus(), null);
        tripEventPublisher.publish(trip);
    }

    public Trip getById(UUID id) {
        return tripRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy chuyến đi"));
    }

    public List<Trip> findByCustomer(UUID customerId) {
        return tripRepository.findByCustomerId(customerId);
    }

    public List<Trip> findByDriver(UUID driverId) {
        return tripRepository.findByDriverId(driverId);
    }

    /** Dùng cho màn hình theo dõi chuyến của admin/dispatcher — 50 chuyến gần nhất. */
    public List<Trip> findRecent() {
        return tripRepository.findAllByOrderByRequestedAtDesc(PageRequest.of(0, 50));
    }

    public List<TripStatusHistory> getHistory(UUID tripId) {
        return tripStatusHistoryRepository.findByTripIdOrderByChangedAtAsc(tripId);
    }

    /** Chuyển trạng thái không phải hủy (DRIVER_ARRIVING, CUSTOMER_ONBOARD, COMPLETED). */
    public Trip updateStatus(UUID tripId, TripStatus newStatus, UUID actorUserId) {
        if (CANCEL_STATUSES.contains(newStatus)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Hủy chuyến phải gọi qua endpoint /cancel kèm lý do");
        }
        Trip trip = getById(tripId);
        TripStatus from = validateTransition(trip, newStatus);

        trip.setStatus(newStatus);
        if (newStatus == TripStatus.COMPLETED) {
            double distanceKm = GeoUtil.distanceKm(trip.getPickupLat(), trip.getPickupLng(),
                    trip.getDropoffLat(), trip.getDropoffLng());
            trip.setDistanceKm(distanceKm);
            trip.setPrice(calculatePrice(distanceKm));
        }

        trip = tripRepository.save(trip);
        recordHistory(trip, from, newStatus, actorUserId);
        tripEventPublisher.publish(trip);
        return trip;
    }

    /** Hủy chuyến — bắt buộc chọn lý do trong danh mục cancel_reasons (không giới hạn số lần ở MVP). */
    public Trip cancel(UUID tripId, TripStatus cancelStatus, UUID cancelReasonId, String cancelNote, UUID actorUserId) {
        if (!CANCEL_STATUSES.contains(cancelStatus)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, cancelStatus + " không phải trạng thái hủy");
        }
        if (!cancelReasonRepository.existsById(cancelReasonId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Lý do hủy không hợp lệ");
        }
        Trip trip = getById(tripId);
        TripStatus from = validateTransition(trip, cancelStatus);

        trip.cancel(cancelStatus, cancelReasonId, cancelNote);
        trip = tripRepository.save(trip);
        recordHistory(trip, from, cancelStatus, actorUserId);
        tripEventPublisher.publish(trip);
        return trip;
    }

    /** Khách đánh giá tài xế sau khi hoàn tất — 1 chiều (quyết định nghiệp vụ đã chốt). */
    public Trip rate(UUID tripId, short rating, String comment) {
        Trip trip = getById(tripId);
        if (trip.getStatus() != TripStatus.COMPLETED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Chỉ đánh giá được chuyến đã hoàn tất");
        }
        if (trip.getCustomerRating() != null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Chuyến này đã được đánh giá");
        }
        if (rating < 1 || rating > 5) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Rating phải từ 1 đến 5");
        }
        trip.rate(rating, comment);
        return tripRepository.save(trip);
    }

    private TripStatus validateTransition(Trip trip, TripStatus newStatus) {
        Set<TripStatus> allowed = ALLOWED_TRANSITIONS.get(trip.getStatus());
        if (allowed == null || !allowed.contains(newStatus)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Không thể chuyển từ " + trip.getStatus() + " sang " + newStatus);
        }
        return trip.getStatus();
    }

    private void recordHistory(Trip trip, TripStatus from, TripStatus to, UUID actorUserId) {
        tripStatusHistoryRepository.save(new TripStatusHistory(trip.getId(), from, to, actorUserId));
    }

    private long calculatePrice(double distanceKm) {
        return BASE_FARE + Math.round(distanceKm * FARE_PER_KM);
    }
}
