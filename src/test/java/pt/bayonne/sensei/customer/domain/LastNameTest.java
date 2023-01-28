package pt.bayonne.sensei.customer.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LastNameTest {

    @Test
    void shouldCreateLastName() {
        //given
        var givenLastName = "Martin";
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