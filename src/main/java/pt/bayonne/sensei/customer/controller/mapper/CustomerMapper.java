package pt.bayonne.sensei.customer.controller.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;
import pt.bayonne.sensei.customer.controller.dto.CustomerDTO;
import pt.bayonne.sensei.customer.domain.BirthDate;
import pt.bayonne.sensei.customer.domain.Customer;
import pt.bayonne.sensei.customer.domain.EmailAddress;
import pt.bayonne.sensei.customer.domain.FirstName;
import pt.bayonne.sensei.customer.domain.LastName;
import pt.bayonne.sensei.customer.domain.SSN;


@Mapper(unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface CustomerMapper {

    CustomerMapper INSTANCE = Mappers.getMapper(CustomerMapper.class);

    static Customer mapToCustomer(final CustomerDTO customerDTO) {
        FirstName firstName = FirstName.of(customerDTO.firstName());
        LastName lastName = LastName.of(customerDTO.lastName());
        BirthDate birthDate = BirthDate.of(customerDTO.birthDate());
        EmailAddress emailAddress = EmailAddress.of(customerDTO.emailAddress());
        SSN ssn = SSN.create(customerDTO.ssn());
        return Customer.create(firstName, lastName, birthDate, emailAddress, ssn);
    }

    @Mapping(target = "firstName", source = "firstName.value")
    @Mapping(target = "lastName", source = "lastName.value")
    @Mapping(target = "birthDate", source = "birthDate.value")
    @Mapping(target = "emailAddress", source = "emailAddress.value")
    @Mapping(target = "ssn", source = "ssn.ssn")
    CustomerDTO mapToCustomerDTO(Customer customer);

}
