package com.example.demo.book;

import com.example.demo.aop.NotNullArgs;
import com.example.demo.aop.Retry;
import com.example.demo.aop.TranslateDbErrors;
import com.example.demo.async.BookNotifier;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.task.TaskRejectedException;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class BookService {

    private final BookRepository repository;
    private final BookMapper mapper;
    private final BookNotifier notifier;

    @Retry(attempts = 3)
    @Transactional(readOnly = true)
    public List<BookDto> findAll() {
        return repository.findAll().stream().map(mapper::toDto).toList();
    }

    @Transactional(readOnly = true)
    public BookDto findById(Long id) {
        return mapper.toDto(get(id));
    }

    @NotNullArgs
    @TranslateDbErrors
    public BookDto create(BookDto dto) {
        BookDto saved = mapper.toDto(repository.save(mapper.toEntity(dto)));
        try {
            notifier.notifyNewBook(saved);
        } catch (TaskRejectedException e) {
            log.warn("Notification for book {} skipped: executor is full", saved.id());
        }
        return saved;
    }

    public List<BookDto> createAll(List<BookDto> dtos) {
        return dtos.stream().map(this::create).toList();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @NotNullArgs
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
