package pt.bayonne.sensei.customer.messaging;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.modulith.ApplicationModule;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;
import pt.bayonne.sensei.customer.messaging.event.CustomerDTO;

import java.nio.charset.StandardCharsets;

@Component
@RequiredArgsConstructor
@Slf4j
@ApplicationModule
public class OutboxMessagePublisher {

    @ApplicationModuleListener
    void on(final CustomerDTO customer) {

        var customerID = customer.id().toString().getBytes(StandardCharsets.UTF_8);
        Message<CustomerDTO> message = MessageBuilder
                .withPayload(customer)
                .setHeader("event-type", "CUSTOMER_CREATED")
                .setHeader("correlation-id", customerID)
                .setHeader(KafkaHeaders.KEY, customerID)
                .build();
        log.info("------> Customer created: {}", message);

    }
}
