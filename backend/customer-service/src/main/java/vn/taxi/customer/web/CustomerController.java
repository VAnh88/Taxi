package vn.taxi.customer.web;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import vn.taxi.customer.domain.Customer;
import vn.taxi.customer.domain.CustomerType;
import vn.taxi.customer.repository.CustomerRepository;
import vn.taxi.customer.web.dto.CustomerResponse;
import vn.taxi.customer.web.dto.UpdateCustomerTypeRequest;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/customers")
public class CustomerController {

    private final CustomerRepository customerRepository;

    public CustomerController(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    @GetMapping
    public List<CustomerResponse> list() {
        return customerRepository.findAll().stream().map(CustomerResponse::from).toList();
    }

    @GetMapping("/{id}")
    public CustomerResponse get(@PathVariable UUID id) {
        return CustomerResponse.from(findOrThrow(id));
    }

    /** Dùng bởi trip-service để resolve customerId từ X-User-Id khi khách đặt xe qua app. */
    @GetMapping("/by-user/{userId}")
    public CustomerResponse getByUser(@PathVariable UUID userId) {
        Customer customer = customerRepository.findByUserId(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy hồ sơ khách hàng"));
        return CustomerResponse.from(customer);
    }

    /** Đánh dấu VIP / blacklist ("Lừa, quấy nhiễu") — theo cơ chế đã khảo sát ở hệ thống Thiên Đức. */
    @PatchMapping("/{id}/type")
    public CustomerResponse updateType(@PathVariable UUID id, @Valid @RequestBody UpdateCustomerTypeRequest request) {
        Customer customer = findOrThrow(id);
        CustomerType type = CustomerType.valueOf(request.type().toUpperCase());
        if (type == CustomerType.BLACKLIST && (request.blacklistReason() == null || request.blacklistReason().isBlank())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cần lý do khi đánh dấu blacklist");
        }
        customer.setType(type, request.blacklistReason());
        return CustomerResponse.from(customerRepository.save(customer));
    }

    private Customer findOrThrow(UUID id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy khách hàng"));
    }
}
