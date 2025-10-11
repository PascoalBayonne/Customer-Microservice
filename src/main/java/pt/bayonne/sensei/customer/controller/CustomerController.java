package pt.bayonne.sensei.customer.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;
import pt.bayonne.sensei.customer.controller.dto.CustomerDTO;
import pt.bayonne.sensei.customer.controller.dto.EmailDTO;
import pt.bayonne.sensei.customer.controller.mapper.CustomerMapper;
import pt.bayonne.sensei.customer.domain.Customer;
import pt.bayonne.sensei.customer.domain.EmailAddress;
import pt.bayonne.sensei.customer.service.CustomerService;

import java.net.URI;

@RestController
@RequestMapping(API.BASE_PATH)
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;

    @PostMapping(value = API.CUSTOMER_V1)
    public ResponseEntity<CustomerDTO> create(@RequestBody @Valid final CustomerDTO customerDTO,
                                              UriComponentsBuilder uriBuilder) {

        Customer customer = CustomerMapper.mapToCustomer(customerDTO);
        Long newCustomerId = customerService.create(customer);

        URI location = uriBuilder.path(API.CUSTOMER_V1+"/{id}")
                .buildAndExpand(newCustomerId)
                .toUri();
        return ResponseEntity.created(location).build();
    }

    @PatchMapping("/v1/customer/{customerId}/email")
    public ResponseEntity<Void> changeEmail(@PathVariable final Long customerId, @RequestBody @Valid final EmailDTO emailDTO) {
        customerService.changeEmail(customerId, EmailAddress.of(emailDTO.emailAddress()));
        return ResponseEntity.accepted().build();
    }

    @GetMapping("/v1/customer/{customerId}")
    public ResponseEntity<CustomerDTO> findByCustomerId(@PathVariable(value = "customerId") Long customerId) {
        Customer customer = this.customerService.findByCustomerId(customerId);
        CustomerDTO customerDTO = CustomerMapper.INSTANCE.mapToCustomerDTO(customer);
        return ResponseEntity.ok(customerDTO);
    }

}
