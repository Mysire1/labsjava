package com.example.demo.aop;

import com.example.demo.async.BookNotifier;
import com.example.demo.book.BookDto;
import com.example.demo.book.BookMapper;
import com.example.demo.book.BookRepository;
import com.example.demo.book.BookService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringJUnitConfig
@EnableAspectJAutoProxy
@Import({BookService.class, NotNullArgsAspect.class, TranslateDbErrorsAspect.class, RetryAspect.class})
class SelfInvocationTest {

    @Autowired
    private BookService service;

    @MockitoBean
    private BookRepository repository;

    @MockitoBean
    private BookMapper mapper;

    @MockitoBean
    private BookNotifier notifier;

    @Test
    void externalCallTriggersNotNullArgs() {
        assertThatThrownBy(() -> service.create(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("must not be null");
        verify(mapper, never()).toEntity(any());
    }

    @Test
    void selfInvocationSkipsNotNullArgsSilently() {
        List<BookDto> result = service.createAll(Collections.singletonList(null));

        assertThat(result).hasSize(1);
        verify(mapper).toEntity(null);
    }

    @Test
    void externalCallTranslatesDbError() {
        BookDto dto = new BookDto(null, "T", "A", 2000);
        when(repository.save(any())).thenThrow(new DataAccessResourceFailureException("db down"));

        assertThatThrownBy(() -> service.create(dto))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(e -> ((ResponseStatusException) e).getStatusCode())
                .isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
    }

    @Test
    void selfInvocationLeaksRawDbError() {
        BookDto dto = new BookDto(null, "T", "A", 2000);
        when(repository.save(any())).thenThrow(new DataAccessResourceFailureException("db down"));

        assertThatThrownBy(() -> service.createAll(List.of(dto)))
                .isInstanceOf(DataAccessResourceFailureException.class);
    }

    @Test
    void externalCallRetriesFindAll() {
        when(repository.findAll())
                .thenThrow(new IllegalStateException("flaky"))
                .thenReturn(List.of());

        assertThat(service.findAll()).isEmpty();
        verify(repository, times(2)).findAll();
    }
}
