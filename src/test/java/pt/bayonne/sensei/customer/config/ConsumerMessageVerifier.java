package pt.bayonne.sensei.customer.config;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.jetbrains.annotations.Nullable;
import org.springframework.cloud.contract.verifier.converter.YamlContract;
import org.springframework.cloud.contract.verifier.messaging.MessageVerifierReceiver;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.support.MessageBuilder;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Configuration
@Slf4j
public class ConsumerMessageVerifier  implements MessageVerifierReceiver<Message<?>> {

    private final Set<Message> consumedMessages = Collections.synchronizedSet(new HashSet<>());


    @KafkaListener(topics = {"customer-topic"}, groupId = "customer-test")
    void consumeMessage(ConsumerRecord record){
        log.info("--------------> consuming from kafka listener");
        consumedMessages.add(MessageBuilder.createMessage(record.value(), new MessageHeaders(Collections.emptyMap())));
        log.info(consumedMessages.toString());
    }

    @SneakyThrows
    @Override
    public Message<?> receive(String destination, long timeout, TimeUnit timeUnit, @Nullable YamlContract contract) {
        log.info("--------------> using MessageVerifierReceiver");

        for (int i =0; i< timeout; i++){
            Message message = consumedMessages.stream().findAny().orElse(null);
            if (message != null){
                return message;
            }
            timeUnit.sleep(1);
        }

        return consumedMessages.stream().findAny().orElse(null);
    }

    @Override
    public Message<?> receive(String destination, YamlContract contract) {
        return receive(destination,5, TimeUnit.SECONDS, contract);
    }
}
