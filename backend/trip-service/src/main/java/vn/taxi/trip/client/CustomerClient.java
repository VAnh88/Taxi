package vn.taxi.trip.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@FeignClient(name = "customer-service")
public interface CustomerClient {

    @GetMapping("/api/customers/by-user/{userId}")
    CustomerDto getByUserId(@PathVariable("userId") UUID userId);
}
