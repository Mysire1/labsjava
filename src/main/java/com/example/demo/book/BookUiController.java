package com.example.demo.book;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
public class BookUiController {

    private final BookService service;

    @GetMapping("/")
    public String index(@RequestParam(required = false) Long edit, Authentication auth, Model model) {
        model.addAttribute("auth", auth);
        model.addAttribute("books", service.findAll());
        model.addAttribute("book", edit == null ? new BookDto(null, null, null, null) : service.findById(edit));
        return "books";
    }

    @PostMapping("/books")
    public String create(BookDto dto) {
        service.create(dto);
        return "redirect:/";
    }

    @PostMapping("/books/{id}")
    public String update(@PathVariable Long id, BookDto dto) {
        service.update(id, dto);
        return "redirect:/";
    }

    @PostMapping("/books/{id}/delete")
    public String delete(@PathVariable Long id) {
        service.delete(id);
        return "redirect:/";
    }
}
