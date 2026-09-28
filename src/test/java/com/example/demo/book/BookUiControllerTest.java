package com.example.demo.book;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookUiControllerTest {

    private static final BookDto DTO = new BookDto(1L, "Title", "Author", 2000);

    @Mock
    private BookService service;

    @InjectMocks
    private BookUiController controller;

    @Test
    void indexWithoutEditPutsEmptyBookInModel() {
        Model model = new ExtendedModelMap();
        when(service.findAll()).thenReturn(List.of(DTO));

        String view = controller.index(null, null, model);

        assertThat(view).isEqualTo("books");
        assertThat(model.getAttribute("books")).isEqualTo(List.of(DTO));
        assertThat(model.getAttribute("book")).isEqualTo(new BookDto(null, null, null, null));
    }

    @Test
    void indexWithEditPutsBookToEditInModel() {
        Model model = new ExtendedModelMap();
        when(service.findById(1L)).thenReturn(DTO);

        controller.index(1L, null, model);

        assertThat(model.getAttribute("book")).isEqualTo(DTO);
    }

    @Test
    void createCallsServiceAndRedirects() {
        assertThat(controller.create(DTO)).isEqualTo("redirect:/");
        verify(service).create(DTO);
    }

    @Test
    void updateCallsServiceAndRedirects() {
        assertThat(controller.update(1L, DTO)).isEqualTo("redirect:/");
        verify(service).update(1L, DTO);
    }

    @Test
    void deleteCallsServiceAndRedirects() {
        assertThat(controller.delete(1L)).isEqualTo("redirect:/");
        verify(service).delete(1L);
    }
}
