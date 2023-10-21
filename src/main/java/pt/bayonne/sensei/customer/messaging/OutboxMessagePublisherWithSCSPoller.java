//package pt.bayonne.sensei.customer.messaging;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import jakarta.transaction.Transactional;
//import lombok.RequiredArgsConstructor;
//import lombok.SneakyThrows;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.context.annotation.Bean;
//import org.springframework.kafka.support.KafkaHeaders;
//import org.springframework.messaging.Message;
//import org.springframework.messaging.support.MessageBuilder;
//import org.springframework.stereotype.Component;
//import pt.bayonne.sensei.customer.domain.OutboxMessage;
//import pt.bayonne.sensei.customer.messaging.event.CustomerDTO;
//import pt.bayonne.sensei.customer.messaging.event.CustomerEvent;
//import pt.bayonne.sensei.customer.repository.OutboxMessageRepository;
//import reactor.core.publisher.Sinks;
//
//import java.nio.charset.StandardCharsets;
//import java.time.Instant;
//import java.time.ZoneId;
//import java.util.List;
//import java.util.function.Supplier;
//import java.util.stream.Collectors;
//
//
///**
// * 1- Don't use this approach because you are just once again committing sent before acknowledgement from kafka.
// * And also it is async transaction can be difficult to manage
// * 2- You may end up having multiple instances of this application, so remember that they will be triggered in the same
// * time causing duplication of messages or even deadlock in resources such as database. For that reason we are going to
// * talk soon about distributed locks. But you can try to implement it by yourself.
// */
//
//@Component
//@RequiredArgsConstructor
//@Slf4j
//public class OutboxMessagePublisherWithSCSPoller {
//
//    private final OutboxMessageRepository outboxMessageRepository;
//    private final ObjectMapper objectMapper;
//
//
//    @Bean
//    @Transactional
//    public Supplier<List<Message<CustomerEvent.CustomerCreated>>> publish() {
//        return () -> this.outboxMessageRepository.findTop10BySentOrderByIdAsc(false)
//                .stream()
//                .map(outboxMessage -> {
//                    outboxMessage.delivered();
//                    return outboxMessage;
//                })
//                .map(outboxMessage -> mapToMessage(outboxMessage))
//                .peek(customerCreatedMessage -> log.debug("delivering events: {}", customerCreatedMessage))
//                .collect(Collectors.toList());
//    }
//
//    @SneakyThrows
//    private Message<CustomerEvent.CustomerCreated> mapToMessage(final OutboxMessage outboxMessage) {
//        String payload = outboxMessage.getPayload();
//        CustomerDTO customerDTO = objectMapper.readValue(payload, CustomerDTO.class);
//
//        Instant createdAt = outboxMessage.getCreationDate()
//                .atZone(ZoneId.of("UTC"))
//                .toInstant();
//
//        var customerCreated = new CustomerEvent.CustomerCreated(customerDTO.id(), createdAt, customerDTO);
//        byte[] idAsByteArray = customerCreated.customerId().toString().getBytes(StandardCharsets.UTF_8);
//        return MessageBuilder.withPayload(customerCreated)
//                .setHeader("X-EVENT-TYPE", "CustomerCreated")
//                .setHeader("X-CORRELATION-ID", customerCreated.customerId())
//                .setHeader(KafkaHeaders.KEY, idAsByteArray)
//                .build();
//    }
//}
