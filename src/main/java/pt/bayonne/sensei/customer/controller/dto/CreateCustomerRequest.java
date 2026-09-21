/* (C)2026 */
package pt.bayonne.sensei.customer.controller.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;

/** Incoming payload a client must send to create a customer. */
public record CreateCustomerRequest(
    @NotBlank(message = "first name cannot be blank") String firstName,
    @NotBlank(message = "last name cannot be blank") String lastName,
    @NotNull(message = "the birth date cannot be null") @Past(message = "the birth date is invalid, it should be in the past") LocalDate birthDate,
    @NotBlank(message = "emailAddress cannot be blank") @Email(message = "please provide a valid email address") String emailAddress,
    @NotBlank(message = "Social Security Number should not be blank") @Pattern(regexp = "\\d{9}", message = "Social Security Number must be exactly 9 digits") String ssn) {}
