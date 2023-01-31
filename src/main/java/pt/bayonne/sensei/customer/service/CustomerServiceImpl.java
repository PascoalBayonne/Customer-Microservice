package pt.bayonne.sensei.customer.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pt.bayonne.sensei.customer.domain.Customer;
import pt.bayonne.sensei.customer.domain.EmailAddress;
import pt.bayonne.sensei.customer.messaging.event.CustomerDTO;
import pt.bayonne.sensei.customer.messaging.event.CustomerEvent;
import pt.bayonne.sensei.customer.repository.CustomerRepository;
import reactor.core.publisher.Sinks;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class CustomerServiceImpl implements CustomerService {
    private final CustomerRepository customerRepository;

    private final Sinks.Many<CustomerEvent> customerProducer;

    @Override
    public Customer create(final Customer customer) {
        Customer customerCreated = customerRepository.save(customer);
        var customerCreatedEvent = new CustomerEvent.CustomerCreated(customerCreated.getId(), Instant.now(), CustomerMapper.mapToCustomerDTO(customerCreated));
        customerProducer.tryEmitNext(customerCreatedEvent);
        return customerCreated;
    }

    @Override
    public void changeEmail(final Long customerId, final EmailAddress emailAddress) {
        Customer customer = this.customerRepository.findById(customerId)
                .orElseThrow(() -> new IllegalArgumentException(String.format("Couldn't find a customer by id: %s", customerId)));

        customer.changeEmail(emailAddress);
        this.customerRepository.save(customer);
    }


    interface CustomerMapper {
        static CustomerDTO mapToCustomerDTO(final Customer customerCreated) {
            return new CustomerDTO(customerCreated.getFirstName().getValue(),
                    customerCreated.getLastName().getValue(),
                    customerCreated.getBirthDate().getValue(),
                    customerCreated.getEmailAddress().getValue(),
                    customerCreated.getSsn().getSsn());
        }
    }


}
