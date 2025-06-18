package pt.bayonne.sensei.customer.messaging;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.modulith.ApplicationModule;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;
import pt.bayonne.sensei.customer.messaging.event.CustomerDTO;

@Component
@Slf4j
@RequiredArgsConstructor
@ApplicationModule
public class CustomerEventListener {

    private final StreamBridge streamBridge;

    @ApplicationModuleListener
    void onCustomerCreated(CustomerDTO event) {
        Message<CustomerDTO> message = MessageBuilder
                .withPayload(event)
                .setHeader("event-type", "ORDER_CREATED")
                .setHeader("correlation-id", event.id().byteValue())
                .build();
       // streamBridge.send("customerSupplier-out-0", message);
        log.info("------> Customer created: {}", event);
    }
}
