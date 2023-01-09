package pt.bayonne.sensei.customer.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.util.Assert;

import java.util.Objects;

@Getter
@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FirstName {

    @Column(name = "firstName")
    private String value;

    private FirstName(String value){
        this.value = value;
    }

    public static FirstName of(final String value){
        Objects.requireNonNull(value, "the value of the first name cannot be null");
        Assert.isTrue(!value.isBlank(), "the first name cannot be empty");
        return new FirstName(value);
    }

}
