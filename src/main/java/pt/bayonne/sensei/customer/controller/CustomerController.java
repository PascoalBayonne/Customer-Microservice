package pt.bayonne.sensei.customer.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pt.bayonne.sensei.customer.controller.dto.CustomerDTO;
import pt.bayonne.sensei.customer.controller.mapper.CustomerMapper;
import pt.bayonne.sensei.customer.domain.*;
import pt.bayonne.sensei.customer.service.CustomerService;

@RestController
@RequestMapping("/api/v1/customer")
@RequiredArgsConstructor
public class CustomerController {
    private final CustomerService customerService;

    @PostMapping
    public ResponseEntity<Customer> create(@RequestBody @Valid final CustomerDTO customerDTO){

        Customer customer = CustomerMapper.mapToCustomer(customerDTO);
        Customer createdCustomer = customerService.create(customer);

        return ResponseEntity.ok(createdCustomer);
    }

}
