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
    public static final String THE_BIRTH_DATE_CANNOT_BE_NULL = "the birth date cannot be null";
    public static final String THE_BIRTH_DATE_SHOULD_BE_IN_PAST = "the birth date should be in past";
    @Column(name = "birthDate")
    private LocalDate value;

    private BirthDate(LocalDate value){
        this.value = value;
    }

    public static BirthDate of(final LocalDate value){
        Objects.requireNonNull(value, THE_BIRTH_DATE_CANNOT_BE_NULL);
        Assert.isTrue(value.isBefore(LocalDate.now()), THE_BIRTH_DATE_SHOULD_BE_IN_PAST);
        return new BirthDate(value);
    }
}
