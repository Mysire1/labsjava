package com.example.demo.book;

import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import static org.assertj.core.api.Assertions.assertThat;

class BookMapperTest {

    private final BookMapper mapper = Mappers.getMapper(BookMapper.class);

    @Test
    void toEntityDtoWithIdIgnoresId() {
        Book book = mapper.toEntity(new BookDto(5L, "T", "A", 1999));

        assertThat(book.getId()).isNull();
        assertThat(book.getTitle()).isEqualTo("T");
        assertThat(book.getAuthor()).isEqualTo("A");
        assertThat(book.getPublishedYear()).isEqualTo(1999);
    }

    @Test
    void toDtoEntityCopiesAllFields() {
        Book book = new Book();
        book.setId(1L);
        book.setTitle("T");
        book.setAuthor("A");
        book.setPublishedYear(2001);

        assertThat(mapper.toDto(book)).isEqualTo(new BookDto(1L, "T", "A", 2001));
    }

    @Test
    void updateKeepsIdAndOverwritesFields() {
        Book book = new Book();
        book.setId(1L);
        book.setTitle("Old");

        mapper.update(new BookDto(99L, "New", "A", 2020), book);

        assertThat(book.getId()).isEqualTo(1L);
        assertThat(book.getTitle()).isEqualTo("New");
    }

    @Test
    void nullInputsReturnNull() {
        assertThat(mapper.toDto(null)).isNull();
        assertThat(mapper.toEntity(null)).isNull();
    }
}
