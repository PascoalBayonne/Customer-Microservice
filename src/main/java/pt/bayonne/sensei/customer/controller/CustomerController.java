package pt.bayonne.sensei.customer.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pt.bayonne.sensei.customer.controller.dto.CustomerDTO;
import pt.bayonne.sensei.customer.controller.dto.EmailDTO;
import pt.bayonne.sensei.customer.controller.mapper.CustomerMapper;
import pt.bayonne.sensei.customer.domain.Customer;
import pt.bayonne.sensei.customer.domain.EmailAddress;
import pt.bayonne.sensei.customer.service.CustomerService;

@RestController
@RequestMapping("/api/v1/customer")
@RequiredArgsConstructor
public class CustomerController {
    private final CustomerService customerService;

    @PostMapping
    public ResponseEntity<CustomerDTO> create(@RequestBody @Valid final CustomerDTO customerDTO){

        Customer customer = CustomerMapper.mapToCustomer(customerDTO);
        Customer createdCustomer = customerService.create(customer);

        CustomerDTO customerCreatedDTO = CustomerMapper.mapToCustomerDTO(createdCustomer);
        return ResponseEntity.ok(customerCreatedDTO);
    }

    @PatchMapping("/{customerId}/email")
    public ResponseEntity<Void> changeEmail(@PathVariable final Long customerId, @RequestBody @Valid final EmailDTO emailDTO){
        customerService.changeEmail(customerId, EmailAddress.of(emailDTO.emailAddress()));
        return ResponseEntity.accepted().build();
    }

}
