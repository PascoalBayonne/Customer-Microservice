package pt.bayonne.sensei.customer.domain;

import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.util.Assert;

import java.util.Objects;
import java.util.regex.Pattern;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EmailAddress {
    private String value;

    private EmailAddress(String value){
        this.value = value;
    }


    public static EmailAddress of(final String value) {
        Objects.requireNonNull(value, "email address cannot be null");
        Assert.isTrue(!value.isBlank(), "the value cannot be empty");

        var  regexPattern = "^(?=.{1,64}@)[A-Za-z0-9_-]+(\\.[A-Za-z0-9_-]+)*@"
                + "[^-][A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*(\\.[A-Za-z]{2,})$";

        boolean matches = Pattern.compile(regexPattern).matcher(value).matches();
        Assert.isTrue(matches,"invalid email address");

        return new EmailAddress(value);
    }
}
