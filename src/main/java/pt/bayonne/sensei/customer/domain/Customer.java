package pt.bayonne.sensei.customer.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Customer {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private FirstName firstName;
    private LastName lastName;
    private BirthDate birthDate;
    private EmailAddress emailAddress;

    public Customer(FirstName firstName, LastName lastName, BirthDate birthDate, EmailAddress emailAddress) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.birthDate = birthDate;
        this.emailAddress = emailAddress;
    }
}
