package com.example.demo.book;

import com.example.demo.async.BookNotifier;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.task.TaskRejectedException;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookServiceTest {

    @Mock
    private BookRepository repository;

    @Mock
    private BookMapper mapper;

    @Mock
    private BookNotifier notifier;

    @InjectMocks
    private BookService service;

    private static Book book(Long id, String title) {
        Book book = new Book();
        book.setId(id);
        book.setTitle(title);
        return book;
    }

    @Test
    void findAllBooksExistReturnsMappedDtos() {
        Book first = book(1L, "A");
        Book second = book(2L, "B");
        BookDto firstDto = new BookDto(1L, "A", null, null);
        BookDto secondDto = new BookDto(2L, "B", null, null);
        when(repository.findAll()).thenReturn(List.of(first, second));
        when(mapper.toDto(first)).thenReturn(firstDto);
        when(mapper.toDto(second)).thenReturn(secondDto);

        List<BookDto> result = service.findAll();

        assertThat(result).containsExactly(firstDto, secondDto);
    }

    @Test
    void findAllNoBooksReturnsEmptyList() {
        when(repository.findAll()).thenReturn(List.of());

        List<BookDto> result = service.findAll();

        assertThat(result).isEmpty();
    }

    @Test
    void findByIdExistingIdReturnsDto() {
        Book entity = book(1L, "Clean Code");
        BookDto dto = new BookDto(1L, "Clean Code", "Robert Martin", 2008);
        when(repository.findById(1L)).thenReturn(Optional.of(entity));
        when(mapper.toDto(entity)).thenReturn(dto);

        BookDto result = service.findById(1L);

        assertThat(result).isEqualTo(dto);
    }

    @Test
    void findByIdMissingIdThrowsNotFound() {
        when(repository.findById(42L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findById(42L))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Book 42 not found")
                .extracting(e -> ((ResponseStatusException) e).getStatusCode())
                .isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void createValidDtoSavesEntityAndReturnsSavedDto() {
        BookDto input = new BookDto(null, "Clean Code", "Robert Martin", 2008);
        Book entity = book(null, "Clean Code");
        Book saved = book(1L, "Clean Code");
        BookDto expected = new BookDto(1L, "Clean Code", "Robert Martin", 2008);
        when(mapper.toEntity(input)).thenReturn(entity);
        when(repository.save(entity)).thenReturn(saved);
        when(mapper.toDto(saved)).thenReturn(expected);

        BookDto result = service.create(input);

        ArgumentCaptor<Book> captor = ArgumentCaptor.forClass(Book.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue()).isSameAs(entity);
        assertThat(result).isEqualTo(expected);
        verify(notifier).notifyNewBook(expected);
    }

    @Test
    void createExecutorFullStillReturnsSavedBook() {
        BookDto input = new BookDto(null, "T", "A", 2000);
        Book entity = book(null, "T");
        BookDto expected = new BookDto(1L, "T", "A", 2000);
        when(mapper.toEntity(input)).thenReturn(entity);
        when(repository.save(entity)).thenReturn(entity);
        when(mapper.toDto(entity)).thenReturn(expected);
        doThrow(new TaskRejectedException("full")).when(notifier).notifyNewBook(expected);

        assertThat(service.create(input)).isEqualTo(expected);
    }

    @Test
    void createNullDtoPropagatesMapperFailureAndDoesNotSave() {
        when(mapper.toEntity(null)).thenThrow(new IllegalArgumentException("dto is null"));

        assertThatThrownBy(() -> service.create(null)).isInstanceOf(IllegalArgumentException.class);
        verify(repository, never()).save(any());
    }

    @Test
    void updateExistingIdAppliesChangesAndReturnsDto() {
        Book entity = book(1L, "Old");
        BookDto input = new BookDto(null, "New", "Author", 2020);
        BookDto expected = new BookDto(1L, "New", "Author", 2020);
        when(repository.findById(1L)).thenReturn(Optional.of(entity));
        when(mapper.toDto(entity)).thenReturn(expected);

        BookDto result = service.update(1L, input);

        verify(mapper).update(input, entity);
        assertThat(result).isEqualTo(expected);
    }

    @Test
    void updateMissingIdThrowsNotFoundAndDoesNotMap() {
        when(repository.findById(7L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.update(7L, new BookDto(null, "X", null, null)))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Book 7 not found");
        verify(mapper, never()).update(any(), any());
    }

    @Test
    void deleteExistingIdDeletesFoundEntity() {
        Book entity = book(3L, "To delete");
        when(repository.findById(3L)).thenReturn(Optional.of(entity));

        service.delete(3L);

        ArgumentCaptor<Book> captor = ArgumentCaptor.forClass(Book.class);
        verify(repository).delete(captor.capture());
        assertThat(captor.getValue().getId()).isEqualTo(3L);
    }

    @Test
    void deleteMissingIdThrowsNotFoundAndDoesNotDelete() {
        when(repository.findById(9L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.delete(9L))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Book 9 not found");
        verify(repository, never()).delete(any());
    }
}
