package vn.taxi.trip.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "driver-service")
public interface DriverClient {

    @GetMapping("/api/drivers/available")
    List<AvailableDriverDto> findAvailable(@RequestParam("lat") double lat,
                                            @RequestParam("lng") double lng,
                                            @RequestParam("radiusKm") double radiusKm,
                                            @RequestParam("limit") int limit);
}
