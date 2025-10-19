package pt.bayonne.sensei.customer.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;
import pt.bayonne.sensei.customer.controller.API;
import pt.bayonne.sensei.customer.controller.dto.ResourceNotFoundException;
import pt.bayonne.sensei.customer.domain.Customer;
import pt.bayonne.sensei.customer.domain.EmailAddress;
import pt.bayonne.sensei.customer.domain.OutboxMessage;
import pt.bayonne.sensei.customer.messaging.event.CustomerDTO;
import pt.bayonne.sensei.customer.messaging.event.CustomerEvent;
import pt.bayonne.sensei.customer.repository.CustomerRepository;
import pt.bayonne.sensei.customer.repository.OutboxMessageRepository;
import reactor.core.publisher.Sinks;

import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class CustomerServiceImpl implements CustomerService {
    public static final String CUSTOMER_CREATED_EVENT = "CustomerCreatedEvent";
    /**
     * @apiNote this is the header name used to identify the event type.
     * This is used to identify the event type in the outbox message.
     */
    private static final String HEADER_NAME = "X-EVENT-TYPE";

    private final CustomerRepository customerRepository;

    private final Sinks.Many<Message<?>> customerProducer;

    private final ObjectMapper objectMapper;

    private final OutboxMessageRepository outboxMessageRepository;


    @Override
    @SneakyThrows
    @Transactional
    public Long create(final Customer customer) {
        Customer newCustomer = customerRepository.save(customer);

        CustomerEvent.CustomerCreatedEvent customerCreatedEvent = new CustomerEvent.CustomerCreatedEvent(newCustomer.getId(),
                        newCustomer.getCreatedAt().toInstant(ZoneOffset.UTC),
                        CUSTOMER_CREATED_EVENT,
                API.BASE_PATH + API.CUSTOMER_V1 + "/" + newCustomer.getId());

        var outboxMessage = OutboxMessage.builder()
                .eventType(CUSTOMER_CREATED_EVENT)
                .payload(objectMapper.writeValueAsString(customerCreatedEvent))
                .build();

        outboxMessageRepository.save(outboxMessage);
        return newCustomer.getId();
    }

    @Override
    public void changeEmail(final Long customerId, final EmailAddress emailAddress) {
        Objects.requireNonNull(customerId, "customerId cannot be null");
        Objects.requireNonNull(emailAddress, "emailAddress cannot be null");
        Customer customer = this.customerRepository.findById(customerId)
                .orElseThrow(() -> new IllegalArgumentException(String.format("Couldn't find a customer by id: %s", customerId)));

        customer.changeEmail(emailAddress);
        this.customerRepository.save(customer);

        var customerEmailChangedEvent = new CustomerEvent.EmailChanged(customer.getId(), Instant.now(), CustomerMapper.mapToCustomerDTO(customer));
        var customerEmailChangedMessage = MessageBuilder.withPayload(customerEmailChangedEvent)
                .setHeader(HEADER_NAME, "EmailChanged").build();
        customerProducer.tryEmitNext(customerEmailChangedMessage);
    }

    @Override
    public Customer findByCustomerId(final Long customerId) {
        Objects.requireNonNull(customerId, "Customer id is required");
        return this.customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException(String.format("Customer with id: %s not found", customerId)));
    }


    interface CustomerMapper {
        static CustomerDTO mapToCustomerDTO(final Customer customerCreated) {
            return new CustomerDTO(customerCreated.getId(),
                    customerCreated.getFirstName().getValue(),
                    customerCreated.getLastName().getValue(),
                    customerCreated.getBirthDate().getValue(),
                    customerCreated.getEmailAddress().getValue(),
                    customerCreated.getSsn().getSsn());
        }
    }


}
