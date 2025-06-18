package pt.bayonne.sensei.customer.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.modulith.events.EventExternalizationConfiguration;
import pt.bayonne.sensei.customer.messaging.event.CustomerDTO;

import java.time.Instant;
import java.util.HashMap;

@Configuration
public class ExternalizationConfiguration {

    @Bean
    public EventExternalizationConfiguration eventExternalizationConfiguration() {

        return EventExternalizationConfiguration.externalizing()
                .select(EventExternalizationConfiguration.annotatedAsExternalized())
                .mapping(CustomerDTO.class, (CustomerDTO customer) -> customer)
                .headers(event -> {
                    var headers = new HashMap<String, Object>();
                    headers.put("event-type", "CUSTOMER_CREATED");
                    headers.put("event-time", Instant.now().toString());
                    return headers;
                })
                .build();
    }
}
