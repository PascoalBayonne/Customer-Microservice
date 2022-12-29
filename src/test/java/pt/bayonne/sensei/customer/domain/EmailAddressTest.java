package pt.bayonne.sensei.customer.domain;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;


class EmailAddressTest {

    @Test
    void of(){
        var expectedEmailAddress = "someone@gmail.com";
        EmailAddress actualEmailAddress =  EmailAddress.of(expectedEmailAddress);
        Assertions.assertNotNull(actualEmailAddress);
        Assertions.assertEquals(expectedEmailAddress, actualEmailAddress.getValue());
    }

}