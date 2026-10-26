package com.example.demo.annotation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = PublishedYearValidator.class)
public @interface ValidPublishedYear {

    String message() default "invalid published year";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
