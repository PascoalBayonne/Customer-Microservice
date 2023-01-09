package pt.bayonne.sensei.customer.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
public class Customer {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;


    private FirstName firstName;

    private LastName lastName;

    private BirthDate birthDate;

    private EmailAddress emailAddress;

    private Customer(FirstName firstName, LastName lastName, BirthDate birthDate, EmailAddress emailAddress) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.birthDate = birthDate;
        this.emailAddress = emailAddress;
    }

    public static Customer create(FirstName firstName, LastName lastName, BirthDate birthDate, EmailAddress emailAddress){
        return new Customer(firstName,lastName,birthDate,emailAddress);
    }

    public void changeEmail(final EmailAddress emailAddress){
        this.emailAddress = emailAddress;
    }

}
