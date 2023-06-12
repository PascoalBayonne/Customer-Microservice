package pt.bayonne.sensei.customer.controller.mapper;

import org.mapstruct.Mapper;
import pt.bayonne.sensei.customer.controller.dto.CustomerDTO;
import pt.bayonne.sensei.customer.domain.*;


@Mapper
public interface CustomerMapper {

    static Customer mapToCustomer(final CustomerDTO customerDTO) {
        FirstName firstName = FirstName.of(customerDTO.firstName());
        LastName lastName = LastName.of(customerDTO.lastName());
        BirthDate birthDate = BirthDate.of(customerDTO.birthDate());
        EmailAddress emailAddress = EmailAddress.of(customerDTO.emailAddress());
        SSN ssn = SSN.create(customerDTO.ssn());
        return Customer.create(firstName, lastName, birthDate, emailAddress, ssn);
    }


    static CustomerDTO mapToCustomerDTO(final Customer customer) {
        if (customer.getFirstName() == null
                || customer.getLastName() == null
                || customer.getBirthDate() == null
                || customer.getEmailAddress() == null
                || customer.getSsn() == null) {
            //please refactor this code in order to throw more precise exception or use pure mapstruct
            throw new IllegalStateException("all customer fields should be present");
        }
        return new CustomerDTO(customer.getFirstName().getValue(),
                customer.getLastName().getValue(),
                customer.getBirthDate().getValue(),
                customer.getEmailAddress().getValue(),
                customer.getSsn().getSsn(),
                customer.getId());
    }


}
