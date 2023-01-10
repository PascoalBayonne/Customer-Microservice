package pt.bayonne.sensei.customer.messaging;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import pt.bayonne.sensei.customer.domain.*;
import pt.bayonne.sensei.customer.service.CustomerService;

import java.time.LocalDate;
import java.util.function.Supplier;

@Component
@RequiredArgsConstructor
public class CustomerMessaging {

    private final CustomerService customerService;

    @Bean
    public Supplier<Customer> customerSupplier(){
        return ()->{
            Customer customerCreated = Customer.create(
                    FirstName.of("James"),
                    LastName.of("Gosling"),
                    BirthDate.of(LocalDate.of(1965, 11, 10)),
                    EmailAddress.of("james.jdk@hotmail.com"));
            return customerService.create(customerCreated);
        };
    }
}
