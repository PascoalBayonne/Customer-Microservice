package pt.bayonne.sensei.customer.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.util.Assert;

import java.time.LocalDate;
import java.util.Locale;
import java.util.Objects;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BirthDate {
    @Column(name = "birthDate")
    private LocalDate value;

    private BirthDate(LocalDate value){
        this.value = value;
    }

    public static BirthDate of(final LocalDate value){
        Objects.requireNonNull(value, "the birth date cannot be null");
        Assert.isTrue(!value.isAfter(LocalDate.now()),"the birth date should be in past");
        return new BirthDate(value);
    }
}
