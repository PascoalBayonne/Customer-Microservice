package pt.bayonne.sensei.customer.messaging.event;

import org.springframework.modulith.events.Externalized;

import java.time.LocalDate;

@Externalized(target = "customer-topic::#{#this.id()}")
public record CustomerDTO(Long id,String firstName, String lastName, LocalDate birthDate, String emailAddress, Integer ssn) {
}
