package pt.bayonne.sensei.customer.domain;

import net.datafaker.Faker;
import org.junit.jupiter.api.Test;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;

class LastNameTest {

    private final Faker faker = new Faker(Locale.US);

    @Test
    void shouldCreateLastName() {
        //given
        var givenLastName = faker.name().lastName();
        //when
        LastName lastNameCreated = LastName.of(givenLastName);
        //then
        assertNotNull(lastNameCreated);
        assertEquals(givenLastName, lastNameCreated.getValue());
    }

    @Test
    void shouldThrowExceptionWhenLastNameIsNull() {
        //given
        String givenLastName = null;
        //when
        NullPointerException nullPointerException = assertThrows(NullPointerException.class, () -> LastName.of(givenLastName));
        //then
        assertEquals("the value cannot be null", nullPointerException.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenLastNameIsEmpty() {
        //given
        String givenLastName = "";
        //when
        IllegalArgumentException nullPointerException = assertThrows(IllegalArgumentException.class, () -> LastName.of(givenLastName));
        //then
        assertEquals("the value cannot be empty", nullPointerException.getMessage());
    }
}