package pt.bayonne.sensei.customer.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
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
    public static final String HEADER_NAME = "X-EVENT-TYPE";
    private final CustomerRepository customerRepository;

    private final Sinks.Many<Message<?>> customerProducer;

    @Override
    @Transactional
    public Customer create(final Customer customer) {
        Customer customerCreated = customerRepository.save(customer);

        CustomerEvent.CustomerCreated customerCreatedEvent = mapToEvent(customerCreated);

        var customerCreatedMessage =  MessageBuilder.withPayload(customerCreatedEvent)
                .setHeader(HEADER_NAME, "CustomerCreated").build();

        customerProducer.tryEmitNext(customerCreatedMessage);
        return customerCreated;
    }

    @NotNull
    private static CustomerEvent.CustomerCreated mapToEvent(Customer customerCreated) {
        return new CustomerEvent
                .CustomerCreated(customerCreated.getId(),
                Instant.now(),
                CustomerMapper.mapToCustomerDTO(customerCreated));
    }

    @Override
    public void changeEmail(final Long customerId, final EmailAddress emailAddress) {
        Customer customer = this.customerRepository.findById(customerId)
                .orElseThrow(() -> new IllegalArgumentException(String.format("Couldn't find a customer by id: %s", customerId)));

        customer.changeEmail(emailAddress);
        this.customerRepository.save(customer);

        var customerEmailChangedEvent = new CustomerEvent.EmailChanged(customer.getId(), Instant.now(), CustomerMapper.mapToCustomerDTO(customer));
        var customerEmailChangedMessage =  MessageBuilder.withPayload(customerEmailChangedEvent)
                .setHeader(HEADER_NAME, "EmailChanged").build();
        customerProducer.tryEmitNext(customerEmailChangedMessage);
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
