package com.example.demo.aop;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class TranslateDbErrorsAspectTest {

    @Test
    void dataAccessExceptionBecomes503() {
        JoinPoint jp = mock(JoinPoint.class);
        Signature signature = mock(Signature.class);
        when(signature.toShortString()).thenReturn("BookService.create(..)");
        when(jp.getSignature()).thenReturn(signature);
        DataAccessResourceFailureException cause = new DataAccessResourceFailureException("connection refused");

        assertThatThrownBy(() -> new TranslateDbErrorsAspect().translate(jp, cause))
                .isInstanceOf(ResponseStatusException.class)
                .hasCause(cause)
                .extracting(e -> ((ResponseStatusException) e).getStatusCode())
                .isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
    }
}
