package pt.bayonne.sensei.customer.controller.mapper;

import pt.bayonne.sensei.customer.controller.dto.CustomerDTO;
import pt.bayonne.sensei.customer.domain.BirthDate;
import pt.bayonne.sensei.customer.domain.Customer;
import pt.bayonne.sensei.customer.domain.EmailAddress;
import pt.bayonne.sensei.customer.domain.FirstName;
import pt.bayonne.sensei.customer.domain.LastName;
import pt.bayonne.sensei.customer.domain.SSN;


public interface CustomerMapper {

    static Customer mapToCustomer(final CustomerDTO customerDTO) {
        FirstName firstName = FirstName.of(customerDTO.firstName());
        LastName lastName = LastName.of(customerDTO.lastName());
        BirthDate birthDate = BirthDate.of(customerDTO.birthDate());
        EmailAddress emailAddress = EmailAddress.of(customerDTO.emailAddress());
        SSN ssn = SSN.create(customerDTO.ssn());
        return Customer.create(firstName, lastName, birthDate, emailAddress, ssn);
    }

}
