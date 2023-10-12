package pt.bayonne.sensei.customer.messaging.event;

import java.time.LocalDate;

public record CustomerDTO(Long id,String firstName, String lastName, LocalDate birthDate, String emailAddress, Integer ssn) {
}
