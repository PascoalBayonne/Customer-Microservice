package pt.bayonne.sensei.customer.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Customer {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;


    private FirstName firstName;

    private LastName lastName;

    private BirthDate birthDate;

    private EmailAddress emailAddress;

    private SSN ssn;

    @CreatedDate
    private LocalDateTime createdAt;

    private Customer(FirstName firstName, LastName lastName, BirthDate birthDate, EmailAddress emailAddress, SSN ssn) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.birthDate = birthDate;
        this.emailAddress = emailAddress;
        this.ssn = ssn;
    }

    public static Customer create(FirstName firstName, LastName lastName, BirthDate birthDate, EmailAddress emailAddress, SSN ssn){
        return new Customer(firstName,lastName,birthDate,emailAddress, ssn);
    }

    public void changeEmail(final EmailAddress emailAddress){
        this.emailAddress = emailAddress;
    }

}
