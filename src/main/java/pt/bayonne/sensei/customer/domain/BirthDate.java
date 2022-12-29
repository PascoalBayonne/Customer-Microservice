package pt.bayonne.sensei.customer.domain;

import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.util.Assert;

import java.time.LocalDate;
import java.util.Objects;

@Embeddable //needed to persist it as a column into our aggregate
@Getter  //custom way to retrieve the values
@NoArgsConstructor(access = AccessLevel.PROTECTED) //hibernate needs it
public class BirthDate {

    private LocalDate value;

    private BirthDate(LocalDate value){
        this.value = value;
    }

    public static BirthDate of(LocalDate value){
        Objects.requireNonNull(value,"the value cannot be null");
        Assert.isTrue(value.isBefore(LocalDate.now()), "the value must be later");
        return new BirthDate(value);
    }
}
