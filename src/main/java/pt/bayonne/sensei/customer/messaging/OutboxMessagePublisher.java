package pt.bayonne.sensei.customer.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.modulith.ApplicationModule;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import pt.bayonne.sensei.customer.domain.Customer;
import pt.bayonne.sensei.customer.domain.OutboxMessage;
import pt.bayonne.sensei.customer.messaging.event.CustomerDTO;
import pt.bayonne.sensei.customer.messaging.event.CustomerEvent;
import pt.bayonne.sensei.customer.repository.OutboxMessageRepository;
import reactor.core.publisher.Sinks;

import java.nio.charset.StandardCharsets;

@Component
@RequiredArgsConstructor
@Slf4j
@ApplicationModule
public class OutboxMessagePublisher {

    private final OutboxMessageRepository outboxMessageRepository;
    private final ObjectMapper objectMapper;
    private final Sinks.Many<Message<?>> customerProducer;

    @ApplicationModuleListener
   // @EventListener //Annotation that marks a method as a listener for application events.
    /*
    f you want a particular listener to process events asynchronously, you can use Spring's @Async support, but be aware of the following limitations when using asynchronous events.
If an asynchronous event listener throws an exception, it is not propagated to the caller. See AsyncUncaughtExceptionHandler for more details.
Asynchronous event listener methods cannot publish a subsequent event by returning a value. If you need to publish another event as the result of the processing, inject an ApplicationEventPublisher to publish the event manually.
     */
    void on(final CustomerDTO customer) {

        var customerID = customer.id().toString().getBytes(StandardCharsets.UTF_8);
        Message<CustomerDTO> message = MessageBuilder
                .withPayload(customer)
                .setHeader("event-type", "CUSTOMER_CREATED")
                .setHeader("correlation-id", customerID)
                .setHeader(KafkaHeaders.KEY, customerID)
                .build();
        log.info("------> Customer created: {}", message);

       // customerProducer.emitNext(message, Sinks.EmitFailureHandler.FAIL_FAST);
    }

//    /**
//     * @apiNote don't forget to add another scheduler which will delete  messages sent.
//     * Something like housekeeper, because this table may grow too fast.
//     */
//    @Scheduled(fixedDelay = 1000)
////    @SchedulerLock(name = "outboxMessagePublisher", lockAtMostFor = "PT1M", lockAtLeastFor = "PT30S")
//    @Transactional
//    public void deliver() {
//        this.outboxMessageRepository.findTop10BySentOrderByIdAsc(false)
//                .forEach(this::deliver);
//    }
//
//    @SneakyThrows
//    private void deliver(final OutboxMessage outboxMessage) {
//        Message<CustomerEvent.CustomerCreatedEvent> customerCreatedMessage = mapToMessage(outboxMessage);
//        customerProducer.tryEmitNext(customerCreatedMessage);//ASync how we know if it was sent?
//        log.info("------------> delivering events: {}", customerCreatedMessage);
//        outboxMessage.delivered();
//    }
//
//
//    @SneakyThrows
//    private Message<CustomerEvent.CustomerCreatedEvent> mapToMessage(final OutboxMessage outboxMessage) {
//        String payload = outboxMessage.getPayload();
//        CustomerEvent.CustomerCreatedEvent customerCreatedEvent = objectMapper.readValue(payload, CustomerEvent.CustomerCreatedEvent.class);
//
//        byte[] idAsByteArray = customerCreatedEvent.customerId().toString().getBytes(StandardCharsets.UTF_8);
//        return MessageBuilder.withPayload(customerCreatedEvent)
//                .setHeader("X-EVENT-TYPE", "CustomerCreated")
//                .setHeader("X-CORRELATION-ID", customerCreatedEvent.customerId())
//                .setHeader(KafkaHeaders.KEY, idAsByteArray)
//                .build();
//    }
}
