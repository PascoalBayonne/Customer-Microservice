package pt.bayonne.sensei.customer.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.util.Assert;

import java.util.Objects;

@Embeddable //needed to persist it as a column into our aggregate
@Getter  //custom way to retrieve the values
@NoArgsConstructor(access = AccessLevel.PROTECTED) //hibernate needs it
public class LastName {

    @Column(name = "lastName")
    private String value;

    private LastName(String value){
        this.value = value;
    }

    public static LastName of(String value){
        Objects.requireNonNull(value,"the value cannot be null");
        Assert.isTrue(!value.isBlank(), "the value cannot be empty");
        return new LastName(value);
    }
}
