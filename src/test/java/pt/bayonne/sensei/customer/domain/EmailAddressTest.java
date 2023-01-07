package pt.bayonne.sensei.customer.domain;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class EmailAddressTest {

    @Test
    @DisplayName("GIVEN a valid email WHEN create THEN email address is created")
    void givenValidEmailAddressWhenCreateThenEmailReturned() {
        var emailAddress = "someone@gmail.com";

        EmailAddress actualEmailAddress = EmailAddress.of(emailAddress);
        Assertions.assertNotNull(actualEmailAddress);
        Assertions.assertEquals(emailAddress, actualEmailAddress.getValue());
    }

    @Test
    @DisplayName("GIVEN invalid email address with null value WHEN create THEN NullPointerException")
    void givenInvalidEmailAddressWhenCreateThenNullPointerException() {
        NullPointerException nullPointerException = Assertions.assertThrows(NullPointerException.class, () -> EmailAddress.of(null));
        Assertions.assertEquals("the email address cannot be null", nullPointerException.getMessage());
    }
}