/* (C)2026 */
package pt.bayonne.sensei.customer.controller.dto;

import java.time.LocalDate;

/** Outgoing representation of a customer, including its generated {@code id}. */
public record CustomerResponse(
    Long id,
    String firstName,
    String lastName,
    LocalDate birthDate,
    String emailAddress,
    String ssn) {}
