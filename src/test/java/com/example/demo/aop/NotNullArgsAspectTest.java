package com.example.demo.aop;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class NotNullArgsAspectTest {

    private final NotNullArgsAspect aspect = new NotNullArgsAspect();
    private final JoinPoint jp = mock(JoinPoint.class);

    @Test
    void allArgsPresentPasses() {
        when(jp.getArgs()).thenReturn(new Object[]{1L, "x"});

        assertThatCode(() -> aspect.check(jp)).doesNotThrowAnyException();
    }

    @Test
    void nullArgThrowsWithIndex() {
        Signature signature = mock(Signature.class);
        when(signature.toShortString()).thenReturn("BookService.update(..)");
        when(jp.getSignature()).thenReturn(signature);
        when(jp.getArgs()).thenReturn(new Object[]{1L, null});

        assertThatThrownBy(() -> aspect.check(jp))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Argument #1");
    }
}
