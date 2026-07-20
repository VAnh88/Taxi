package vn.taxi.customer.event;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import vn.taxi.customer.domain.Customer;
import vn.taxi.customer.repository.CustomerRepository;

@Component
public class UserCreatedEventListener {

    private final CustomerRepository customerRepository;

    public UserCreatedEventListener(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    @KafkaListener(topics = "user.created", groupId = "customer-service")
    public void onUserCreated(UserCreatedEvent event) {
        if (!"CUSTOMER".equalsIgnoreCase(event.role())) {
            return;
        }
        if (customerRepository.findByUserId(event.userId()).isPresent()) {
            return;
        }
        customerRepository.save(new Customer(event.userId(), event.username(), event.phone()));
    }
}
