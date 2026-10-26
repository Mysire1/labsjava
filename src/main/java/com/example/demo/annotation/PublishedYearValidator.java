package com.example.demo.annotation;

import com.example.demo.book.BookDto;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.Year;

public class PublishedYearValidator implements ConstraintValidator<ValidPublishedYear, BookDto> {

    static final int MIN_YEAR = 1450;

    @Override
    public boolean isValid(BookDto dto, ConstraintValidatorContext context) {
        if (dto == null || dto.publishedYear() == null) {
            return true;
        }
        if (dto.publishedYear() >= MIN_YEAR && dto.publishedYear() <= Year.now().getValue()) {
            return true;
        }
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(
                        "publishedYear " + dto.publishedYear() + " must be between " + MIN_YEAR + " and " + Year.now().getValue())
                .addPropertyNode("publishedYear")
                .addConstraintViolation();
        return false;
    }
}
