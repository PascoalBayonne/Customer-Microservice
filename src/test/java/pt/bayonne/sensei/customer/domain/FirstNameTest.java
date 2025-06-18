package pt.bayonne.sensei.customer.domain;

import net.datafaker.Faker;
import org.junit.jupiter.api.Test;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;

class FirstNameTest {

    private Faker faker = new Faker(Locale.of("en_IN"));
    @Test
    void shouldCreateFirstName() {
        //given
        var givenFirstName = faker.name().firstName();
        //when
        FirstName firstNameCreated = FirstName.of(givenFirstName);
        //then
        assertNotNull(firstNameCreated);
        assertEquals(givenFirstName, firstNameCreated.getValue());
    }

    @Test
    void shouldThrowExceptionWhenFirstNameIsNull() {
        //given
        String givenFirstName = null;
        //when
        NullPointerException nullPointerException = assertThrows(NullPointerException.class, () -> FirstName.of(givenFirstName));
        //then
        assertEquals("the value of the first name cannot be null", nullPointerException.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenFirstNameIsEmpty() {
        //given
        String givenFirstName = "";
        //when
        IllegalArgumentException nullPointerException = assertThrows(IllegalArgumentException.class, () -> FirstName.of(givenFirstName));
        //then
        assertEquals("the first name cannot be empty", nullPointerException.getMessage());
    }
}