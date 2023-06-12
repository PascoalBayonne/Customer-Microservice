package pt.bayonne.sensei.customer.controller.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;

import java.time.LocalDate;

public record CustomerDTO(@NotBlank(message = "first name cannot be blank") String firstName,
                          @NotBlank(message = "last name cannot be blank") String lastName,
                          @NotNull(message = "the birth date cannot be null")
                          @Past(message = "the birth date is invalid, it should be in the past")
                          LocalDate birthDate,
                          @NotBlank(message = "emailAddress cannot be blank")
                          @Email(message = "please provide a valid email address")
                          String emailAddress,
                          @NotNull(message = "Social Security Number should not be null")
                          Integer ssn,
                          Long id) {
}
