package com.example.demo.book;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class BookService {

    private final BookRepository repository;
    private final BookMapper mapper;

    @Transactional(readOnly = true)
    public List<BookDto> findAll() {
        return repository.findAll().stream().map(mapper::toDto).toList();
    }

    @Transactional(readOnly = true)
    public BookDto findById(Long id) {
        return mapper.toDto(get(id));
    }

    public BookDto create(BookDto dto) {
        return mapper.toDto(repository.save(mapper.toEntity(dto)));
    }

    @PreAuthorize("hasRole('ADMIN')")
    public BookDto update(Long id, BookDto dto) {
        Book book = get(id);
        mapper.update(dto, book);
        return mapper.toDto(book);
    }

    public void delete(Long id) {
        repository.delete(get(id));
    }

    private Book get(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Book " + id + " not found"));
    }
}
