package pt.bayonne.sensei.customer.service;

import pt.bayonne.sensei.customer.domain.Customer;
import pt.bayonne.sensei.customer.domain.EmailAddress;

public interface CustomerService {
    Long create(Customer customer);

    void changeEmail(Long customerId, EmailAddress emailAddress);

    Customer findByCustomerId(Long customerId);
}
