package pt.bayonne.sensei.customer.base;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.verifier.messaging.boot.AutoConfigureMessageVerifier;
import pt.bayonne.sensei.customer.domain.*;
import pt.bayonne.sensei.customer.service.CustomerService;

import java.time.LocalDate;

@SpringBootTest
@AutoConfigureMessageVerifier
public class ShouldPublishCustomerCreatedBase {

    @Autowired
    private CustomerService customerService;

    void shouldPublishCustomerCreated() {
        var customer = Customer.create(FirstName.of("Josh"),
                LastName.of("Bloch"),
                BirthDate.of(LocalDate.of(1956, 12, 20)),
                EmailAddress.of("JoshBlock@gmail.com"), SSN.create(888888888));
        customerService.create(customer);
    }
}
