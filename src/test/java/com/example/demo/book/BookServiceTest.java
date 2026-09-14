package com.example.demo.book;

import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class BookServiceTest {

    private final BookRepository repository = mock(BookRepository.class);
    private final BookService service = new BookService(repository, Mappers.getMapper(BookMapper.class));

    @Test
    void createMapsDtoAndReturnsSavedBook() {
        when(repository.save(any())).thenAnswer(inv -> {
            Book book = inv.getArgument(0);
            book.setId(1L);
            return book;
        });

        BookDto result = service.create(new BookDto(null, "Clean Code", "Robert Martin", 2008));

        assertThat(result).isEqualTo(new BookDto(1L, "Clean Code", "Robert Martin", 2008));
    }

    @Test
    void findByIdThrowsWhenMissing() {
        when(repository.findById(42L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findById(42L)).isInstanceOf(ResponseStatusException.class);
    }
}
