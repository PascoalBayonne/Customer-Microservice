//package pt.bayonne.sensei.customer.messaging;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import jakarta.transaction.Transactional;
//import lombok.RequiredArgsConstructor;
//import lombok.SneakyThrows;
//import lombok.extern.slf4j.Slf4j;
//import org.jetbrains.annotations.NotNull;
//import org.springframework.cloud.function.context.PollableBean;
//import org.springframework.context.annotation.Bean;
//import org.springframework.kafka.support.KafkaHeaders;
//import org.springframework.messaging.Message;
//import org.springframework.messaging.support.MessageBuilder;
//import org.springframework.stereotype.Component;
//import pt.bayonne.sensei.customer.domain.OutboxMessage;
//import pt.bayonne.sensei.customer.messaging.event.CustomerDTO;
//import pt.bayonne.sensei.customer.messaging.event.CustomerEvent;
//import pt.bayonne.sensei.customer.repository.OutboxMessageRepository;
//import reactor.core.publisher.Flux;
//
//import java.nio.charset.StandardCharsets;
//import java.time.Instant;
//import java.time.ZoneId;
//import java.util.List;
//import java.util.function.Supplier;
//import java.util.stream.Collectors;
//
///**
// * Don't use this approach because you are just once again committing sent before acknowledgement from kafka.
// * And also it is async transaction can be difficult to manage
// */
//@Component
//@RequiredArgsConstructor
//@Slf4j
//public class OutboxMessagePublisherWithSCSPollerReactive {
//
//    private final OutboxMessageRepository outboxMessageRepository;
//    private final ObjectMapper objectMapper;
//
//
//
//    @PollableBean
//    @Transactional
//    public Supplier<Flux<Message<CustomerEvent.CustomerCreated>>> publish() {
//        return () -> {
//            List<Message<CustomerEvent.CustomerCreated>> messagesToDeliver = this.outboxMessageRepository.findTop10BySentOrderByIdAsc(false)
//                    .stream()
//                    .map(outboxMessage -> markDelivered(outboxMessage))
//                    .map(outboxMessage -> mapToMessage(outboxMessage))
//                    .peek(customerCreatedMessage -> log.debug("delivering events: {}", customerCreatedMessage))
//                    .collect(Collectors.toList());
//
//            return Flux.fromIterable(messagesToDeliver);
//        };
//    }
//
//    @NotNull
//    private static OutboxMessage markDelivered(final OutboxMessage outboxMessage) {
//        outboxMessage.delivered();
//        return outboxMessage;
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
