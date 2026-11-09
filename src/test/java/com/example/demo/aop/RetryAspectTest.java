package com.example.demo.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RetryAspectTest {

    private final RetryAspect aspect = new RetryAspect();
    private final ProceedingJoinPoint pjp = mock(ProceedingJoinPoint.class);
    private final Retry retry = mock(Retry.class);

    @BeforeEach
    void setUp() {
        Signature signature = mock(Signature.class);
        when(signature.toShortString()).thenReturn("BookService.findAll()");
        when(pjp.getSignature()).thenReturn(signature);
        when(retry.attempts()).thenReturn(3);
    }

    @Test
    void successOnFirstTryCallsProceedOnce() throws Throwable {
        when(pjp.proceed()).thenReturn("ok");

        assertThat(aspect.retry(pjp, retry)).isEqualTo("ok");
        verify(pjp, times(1)).proceed();
    }

    @Test
    void failsThenSucceedsRetriesUntilSuccess() throws Throwable {
        when(pjp.proceed())
                .thenThrow(new IllegalStateException("boom"))
                .thenThrow(new IllegalStateException("boom"))
                .thenReturn("ok");

        assertThat(aspect.retry(pjp, retry)).isEqualTo("ok");
        verify(pjp, times(3)).proceed();
    }

    @Test
    void alwaysFailsRethrowsAfterMaxAttempts() throws Throwable {
        when(pjp.proceed()).thenThrow(new IllegalStateException("boom"));

        assertThatThrownBy(() -> aspect.retry(pjp, retry)).hasMessage("boom");
        verify(pjp, times(3)).proceed();
    }
}
