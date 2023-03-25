package pt.bayonne.sensei.customer.controller.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record EmailDTO (@NotBlank(message = "emailAddress cannot be blank")
                        @Email(message = "please provide a valid email address")
                        String emailAddress) {
}
