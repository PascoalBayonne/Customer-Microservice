package pt.bayonne.sensei.customer.service;

import pt.bayonne.sensei.customer.domain.Customer;
import pt.bayonne.sensei.customer.domain.EmailAddress;

public interface CustomerService {
    Customer create(Customer customer);

    void changeEmail(Long customerId, EmailAddress emailAddress);
}
