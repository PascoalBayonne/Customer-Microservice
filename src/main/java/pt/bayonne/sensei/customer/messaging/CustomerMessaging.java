package pt.bayonne.sensei.customer.messaging;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import pt.bayonne.sensei.customer.domain.*;
import pt.bayonne.sensei.customer.messaging.event.CustomerEvent;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.util.function.Supplier;

@Configuration
@RequiredArgsConstructor
public class CustomerMessaging {

    @Bean
    public Sinks.Many<CustomerEvent> customerProducer(){
        return Sinks.many().replay().latest();
    }

    @Bean
    public Supplier<Flux<CustomerEvent>> customerSupplier(){
        return ()-> customerProducer().asFlux();
    }
}
