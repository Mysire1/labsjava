package com.example.demo.book;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookRestControllerTest {

    private static final BookDto DTO = new BookDto(1L, "Title", "Author", 2000);

    @Mock
    private BookService service;

    @Mock
    private BookDetailsService detailsService;

    @InjectMocks
    private BookRestController controller;

    @Test
    void findAllDelegatesToService() {
        when(service.findAll()).thenReturn(List.of(DTO));

        assertThat(controller.findAll()).containsExactly(DTO);
    }

    @Test
    void findByIdDelegatesToService() {
        when(service.findById(1L)).thenReturn(DTO);

        assertThat(controller.findById(1L)).isEqualTo(DTO);
    }

    @Test
    void detailsDelegatesToDetailsService() {
        BookDetails details = new BookDetails(DTO, 4.5, "url");
        when(detailsService.details(1L)).thenReturn(details);

        assertThat(controller.details(1L)).isEqualTo(details);
    }

    @Test
    void createDelegatesToService() {
        when(service.create(DTO)).thenReturn(DTO);

        assertThat(controller.create(DTO)).isEqualTo(DTO);
    }

    @Test
    void updateDelegatesToService() {
        when(service.update(1L, DTO)).thenReturn(DTO);

        assertThat(controller.update(1L, DTO)).isEqualTo(DTO);
    }

    @Test
    void deleteCallsServiceDelete() {
        controller.delete(1L);

        verify(service).delete(1L);
    }
}
