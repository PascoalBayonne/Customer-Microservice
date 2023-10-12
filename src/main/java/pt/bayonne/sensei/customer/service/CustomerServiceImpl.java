package pt.bayonne.sensei.customer.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;
import pt.bayonne.sensei.customer.domain.Customer;
import pt.bayonne.sensei.customer.domain.EmailAddress;
import pt.bayonne.sensei.customer.domain.OutboxMessage;
import pt.bayonne.sensei.customer.messaging.event.CustomerDTO;
import pt.bayonne.sensei.customer.messaging.event.CustomerEvent;
import pt.bayonne.sensei.customer.repository.CustomerRepository;
import pt.bayonne.sensei.customer.repository.OutboxMessageRepository;
import reactor.core.publisher.Sinks;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class CustomerServiceImpl implements CustomerService {
    public static final String HEADER_NAME = "X-EVENT-TYPE";
    private final CustomerRepository customerRepository;

    private final Sinks.Many<Message<?>> customerProducer;

    private final ObjectMapper objectMapper;

    private final OutboxMessageRepository outboxMessageRepository;

    @SneakyThrows
    @Override
    @Transactional
    public Customer create(final Customer customer) {
        Customer customerCreated = customerRepository.save(customer);

        CustomerDTO customerDTO = CustomerMapper.mapToCustomerDTO(customerCreated);

        String payload = objectMapper.writeValueAsString(customerDTO);
        var outboxMessage = OutboxMessage.builder()
                .eventType("CustomerCreated")
                .payload(payload)
                .build();

        outboxMessageRepository.save(outboxMessage);
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
            return new CustomerDTO(customerCreated.getId(), customerCreated.getFirstName().getValue(),
                    customerCreated.getLastName().getValue(),
                    customerCreated.getBirthDate().getValue(),
                    customerCreated.getEmailAddress().getValue(),
                    customerCreated.getSsn().getSsn());
        }
    }


}
