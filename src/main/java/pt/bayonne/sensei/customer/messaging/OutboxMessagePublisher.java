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
import pt.bayonne.sensei.customer.messaging.event.CustomerEvent;
import pt.bayonne.sensei.customer.repository.OutboxMessageRepository;
import reactor.core.publisher.Sinks;

import java.nio.charset.StandardCharsets;

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
     */
    @Scheduled(fixedDelay = 1000)
//    @SchedulerLock(name = "outboxMessagePublisher", lockAtMostFor = "PT1M", lockAtLeastFor = "PT30S")
    @Transactional
    public void deliver() {
        this.outboxMessageRepository.findTop10BySentOrderByIdAsc(false)
                .forEach(this::deliver);
    }

    @SneakyThrows
    private void deliver(final OutboxMessage outboxMessage) {
        Message<CustomerEvent.CustomerCreatedEvent> customerCreatedMessage = mapToMessage(outboxMessage);
        customerProducer.tryEmitNext(customerCreatedMessage);//ASync how we know if it was sent?
        log.info("------------> delivering events: {}", customerCreatedMessage);
        outboxMessage.delivered();
    }


    @SneakyThrows
    private Message<CustomerEvent.CustomerCreatedEvent> mapToMessage(final OutboxMessage outboxMessage) {
        String payload = outboxMessage.getPayload();
        CustomerEvent.CustomerCreatedEvent customerCreatedEvent = objectMapper.readValue(payload, CustomerEvent.CustomerCreatedEvent.class);

        byte[] idAsByteArray = customerCreatedEvent.customerId().toString().getBytes(StandardCharsets.UTF_8);
        return MessageBuilder.withPayload(customerCreatedEvent)
                .setHeader("X-EVENT-TYPE", "CustomerCreated")
                .setHeader("X-CORRELATION-ID", customerCreatedEvent.customerId())
                .setHeader(KafkaHeaders.KEY, idAsByteArray)
                .build();
    }
}
