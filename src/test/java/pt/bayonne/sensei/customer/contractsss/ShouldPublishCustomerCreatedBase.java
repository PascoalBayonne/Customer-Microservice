package pt.bayonne.sensei.customer.contractsss;

import org.junit.jupiter.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.verifier.messaging.boot.AutoConfigureMessageVerifier;
import org.springframework.context.annotation.Import;
import pt.bayonne.sensei.customer.config.ContainersConfiguration;
import pt.bayonne.sensei.customer.domain.BirthDate;
import pt.bayonne.sensei.customer.domain.Customer;
import pt.bayonne.sensei.customer.domain.EmailAddress;
import pt.bayonne.sensei.customer.domain.FirstName;
import pt.bayonne.sensei.customer.domain.LastName;
import pt.bayonne.sensei.customer.domain.SSN;
import pt.bayonne.sensei.customer.service.CustomerService;

import java.time.LocalDate;

@SpringBootTest
@AutoConfigureMessageVerifier
@Import(ContainersConfiguration.class)
public class ShouldPublishCustomerCreatedBase {

    @Autowired
    private CustomerService customerService;


    void shouldPublishCustomerCreated() {
        var customer = Customer.create(FirstName.of("Nasir"),
                LastName.of("Jones"),
                BirthDate.of(LocalDate.of(1999, 8, 11)),
                EmailAddress.of("nasisthegoat@gmail.com"), SSN.create(888888888));
        Long newCustomerId = customerService.create(customer);
        Assertions.assertNotNull(newCustomerId);
    }
}
