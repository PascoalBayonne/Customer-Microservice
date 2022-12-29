package pt.bayonne.sensei.customer.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pt.bayonne.sensei.customer.domain.Customer;
import pt.bayonne.sensei.customer.repository.CustomerRepository;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class CustomerServiceImpl implements CustomerService{

    private final CustomerRepository customerRepository;

    @Override
    public Customer createCustomer(Customer customer) {

        return customerRepository.save(customer);
    }
}
