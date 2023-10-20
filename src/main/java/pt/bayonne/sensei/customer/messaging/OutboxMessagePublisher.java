package pt.bayonne.sensei.customer.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import pt.bayonne.sensei.customer.domain.OutboxMessage;
import pt.bayonne.sensei.customer.messaging.event.CustomerDTO;
import pt.bayonne.sensei.customer.messaging.event.CustomerEvent;
import pt.bayonne.sensei.customer.repository.OutboxMessageRepository;
import reactor.core.publisher.Sinks;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZoneId;

@Component
@RequiredArgsConstructor
@Slf4j
public class OutboxMessagePublisher {

    private final OutboxMessageRepository outboxMessageRepository;
    private final ObjectMapper objectMapper;
    private final Sinks.Many<Message<?>> customerProducer;


    /**
     * @apiNote don't forget to add another scheduler which will delete  messages sent.
     * Something like housekeeper, because this table may grow too fast.
     *
     */
    @Scheduled(fixedDelay = 1000)
    @Transactional
    public void deliver() {
        this.outboxMessageRepository.findTop10BySentOrderByIdAsc(false)
                .forEach(this::deliver);
    }

    private void deliver(final OutboxMessage outboxMessage) {
        Message<CustomerEvent.CustomerCreated> customerCreatedMessage = mapToMessage(outboxMessage);
        customerProducer.tryEmitNext(customerCreatedMessage);
        outboxMessage.delivered();
    }


    @SneakyThrows
    private Message<CustomerEvent.CustomerCreated> mapToMessage(final OutboxMessage outboxMessage) {
        String payload = outboxMessage.getPayload();
        CustomerDTO customerDTO = objectMapper.readValue(payload, CustomerDTO.class);

        Instant createdAt = outboxMessage.getCreationDate()
                .atZone(ZoneId.of("UTC"))
                .toInstant();

        var customerCreated = new CustomerEvent.CustomerCreated(customerDTO.id(), createdAt, customerDTO);
        byte[] idAsByteArray = customerCreated.customerId().toString().getBytes(StandardCharsets.UTF_8);
        return MessageBuilder.withPayload(customerCreated)
                .setHeader("X-EVENT-TYPE", "CustomerCreated")
                .setHeader("X-CORRELATION-ID", customerCreated.customerId())
                .setHeader(KafkaHeaders.KEY, idAsByteArray)
                .build();
    }
}
