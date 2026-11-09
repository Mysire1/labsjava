package com.example.demo.book;

import com.example.demo.annotation.CurrentUsername;
import com.example.demo.annotation.PostCreated;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor
public class BookRestController {

    private final BookService service;
    private final BookDetailsService detailsService;

    @GetMapping
    public List<BookDto> findAll() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public BookDto findById(@PathVariable Long id) {
        return service.findById(id);
    }

    @GetMapping("/{id}/details")
    public BookDetails details(@PathVariable Long id) {
        return detailsService.details(id);
    }

    @GetMapping("/me")
    public String me(@CurrentUsername String username) {
        return "Hello, " + username;
    }

    @PostCreated
    public BookDto create(@Valid @RequestBody BookDto dto) {
        return service.create(dto);
    }

    @PutMapping("/{id}")
    public BookDto update(@PathVariable Long id, @Valid @RequestBody BookDto dto) {
        return service.update(id, dto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}
