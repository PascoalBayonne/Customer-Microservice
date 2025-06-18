package pt.bayonne.sensei.customer.domain;

import net.datafaker.Faker;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Locale;

class EmailAddressTest {

    private final Faker faker = new Faker(Locale.US);
    @Test
    @DisplayName("GIVEN a valid email WHEN create THEN email address is created")
    void givenValidEmailAddressWhenCreateThenEmailReturned() {
        var emailAddress = faker.internet().emailAddress();

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