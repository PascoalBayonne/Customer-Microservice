package pt.bayonne.sensei.customer.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import net.datafaker.Faker;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pt.bayonne.sensei.customer.controller.dto.CustomerDTO;
import pt.bayonne.sensei.customer.controller.dto.EmailDTO;
import pt.bayonne.sensei.customer.controller.mapper.CustomerMapper;
import pt.bayonne.sensei.customer.domain.*;
import pt.bayonne.sensei.customer.service.CustomerService;

import java.util.List;
import java.util.Locale;

@RestController
@RequestMapping("/api/v1/customer")
@RequiredArgsConstructor
public class CustomerController {
    private final CustomerService customerService;
    private final Faker faker = new Faker(Locale.of("pt"));

    @PostMapping
    public ResponseEntity<Customer> create(@RequestBody @Valid final CustomerDTO customerDTO) {

        Customer customer = CustomerMapper.mapToCustomer(customerDTO);
        Customer createdCustomer = customerService.create(customer);

        return ResponseEntity.ok(createdCustomer);
    }

    @PatchMapping("/{customerId}/email")
    public ResponseEntity<Void> changeEmail(@PathVariable final Long customerId, @RequestBody @Valid final EmailDTO emailDTO) {
        customerService.changeEmail(customerId, EmailAddress.of(emailDTO.emailAddress()));
        return ResponseEntity.accepted().build();
    }

    /**
     * @return fake realistic customers
     * @implNote At this point, this is only for testing purposes. We should not use it in production.
     * We are using it to generate fake data while we are waiting the database creation, so the
     * frontend team can invoke it.
     */
    @GetMapping
    public ResponseEntity<List<CustomerDTO>> findAll() {
        List<Customer> customers = faker.collection(
                        () -> Customer.create(
                                FirstName.of(faker.name().firstName()),
                                LastName.of(faker.name().lastName()),
                                BirthDate.of(faker.date().birthday().toLocalDateTime().toLocalDate()),
                                EmailAddress.of(faker.internet().emailAddress()),
                                SSN.create(faker.number().numberBetween(100000000, 999999999)))
                ).maxLen(15)
                .generate();

        List<CustomerDTO> customerDTOS = customers.stream().map(CustomerMapper::mapToCustomerDTO)
                .toList();

        return ResponseEntity.ok(customerDTOS);

    }

}
