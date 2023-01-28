package pt.bayonne.sensei.customer.domain;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class SSNTest {

    @Test
    void shouldCreate() {
        //given
        var givenSSN = 498312901;
        //when
        SSN socialSecurityNumber = SSN.create(givenSSN);
        //then
        Assertions.assertEquals(givenSSN,socialSecurityNumber.getSsn());
    }

    @Test
    void shouldThrowExceptionWhenSocialSecurityNumberHaveLessThenNightChars(){
        //given
        var invalidSocialSecurityNumber = 123;
        //when
        IllegalArgumentException illegalArgumentException = Assertions.assertThrows(IllegalArgumentException.class, () -> SSN.create(invalidSocialSecurityNumber));
        //then
        Assertions.assertEquals("The social security number should have 9 characters", illegalArgumentException.getMessage());
    }
}