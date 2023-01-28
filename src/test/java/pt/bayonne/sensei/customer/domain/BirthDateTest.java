package pt.bayonne.sensei.customer.domain;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class BirthDateTest {

    @Test
    void shouldCreateLBirthDate() {
        //given
        LocalDate birthDate = LocalDate.now().minusYears(29);
        //when
        BirthDate birthDateCreated = BirthDate.of(birthDate);
        //then
        assertEquals(birthDate, birthDateCreated.getValue());
    }

    @Test
    void shouldThrowExceptionWhenBirthDateIsAfter() {
        //given
        LocalDate givenBirthDate = LocalDate.now().plusYears(29);
        //when
        IllegalArgumentException illegalArgumentException = assertThrows(IllegalArgumentException.class, () -> BirthDate.of(givenBirthDate));
        //then
        assertEquals(BirthDate.THE_BIRTH_DATE_SHOULD_BE_IN_PAST, illegalArgumentException.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenBirthDateIsNull() {
        //given
        LocalDate givenBirthDate = null;
        //when
        NullPointerException nullPointerException = assertThrows(NullPointerException.class, () -> BirthDate.of(givenBirthDate));
        //then
        assertEquals(BirthDate.THE_BIRTH_DATE_CANNOT_BE_NULL, nullPointerException.getMessage());
    }
}