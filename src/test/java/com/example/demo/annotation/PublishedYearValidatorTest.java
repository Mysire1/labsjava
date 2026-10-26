package com.example.demo.annotation;

import com.example.demo.book.BookDto;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class PublishedYearValidatorTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void validYearHasNoViolations() {
        assertThat(validator.validate(new BookDto(null, "T", "A", 2000))).isEmpty();
    }

    @Test
    void futureYearHasClearViolation() {
        Set<ConstraintViolation<BookDto>> violations = validator.validate(new BookDto(null, "T", "A", 3000));

        assertThat(violations).singleElement().satisfies(v -> {
            assertThat(v.getPropertyPath()).hasToString("publishedYear");
            assertThat(v.getMessage()).startsWith("publishedYear 3000 must be between 1450 and ");
        });
    }

    @Test
    void tooOldYearHasViolation() {
        assertThat(validator.validate(new BookDto(null, "T", "A", 1000))).hasSize(1);
    }
}
