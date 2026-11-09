package com.example.demo.annotation;

import com.example.demo.book.BookDto;
import com.example.demo.book.BookDetailsService;
import com.example.demo.book.BookRestController;
import com.example.demo.book.BookService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AnnotationsMvcTest {

    private final BookService service = mock(BookService.class);
    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.standaloneSetup(new BookRestController(service, mock(BookDetailsService.class)))
                .setCustomArgumentResolvers(new CurrentUsernameResolver())
                .build();
    }

    @Test
    void postCreatedRoutesPostAndReturns201() throws Exception {
        when(service.create(any())).thenReturn(new BookDto(1L, "T", "A", 2000));

        mvc.perform(post("/api/books").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"T\",\"author\":\"A\",\"publishedYear\":2000}"))
                .andExpect(status().isCreated());
    }

    @Test
    void invalidYearInRealRequestReturns400() throws Exception {
        mvc.perform(post("/api/books").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"T\",\"author\":\"A\",\"publishedYear\":3000}"))
                .andExpect(status().isBadRequest());

        verify(service, never()).create(any());
    }

    @Test
    void currentUsernameIsUsedInMethodBody() throws Exception {
        mvc.perform(get("/api/books/me").principal(() -> "alice"))
                .andExpect(status().isOk())
                .andExpect(content().string("Hello, alice"));
    }
}
