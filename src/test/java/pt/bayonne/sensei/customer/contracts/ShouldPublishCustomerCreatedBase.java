package pt.bayonne.sensei.customer.contracts;

import org.junit.jupiter.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.contract.verifier.messaging.boot.AutoConfigureMessageVerifier;
import pt.bayonne.sensei.customer.config.IntegrationTestBaseConfig;
import pt.bayonne.sensei.customer.domain.*;
import pt.bayonne.sensei.customer.service.CustomerService;

import java.time.LocalDate;


@AutoConfigureMessageVerifier
public class ShouldPublishCustomerCreatedBase extends IntegrationTestBaseConfig {

    @Autowired
    private CustomerService customerService;


    void shouldPublishCustomerCreated() {
        var customer = Customer.create(FirstName.of("Nasir"),
                LastName.of("Jones"),
                BirthDate.of(LocalDate.of(1999, 8, 11)),
                EmailAddress.of("nasisthegoat@gmail.com"), SSN.create(888888888));
        Customer customerCreated = customerService.create(customer);
        Assertions.assertNotNull(customerCreated);
    }
}
