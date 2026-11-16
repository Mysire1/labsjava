package com.example.demo.async;

import com.example.demo.book.BookDetails;
import com.example.demo.book.BookDetailsService;
import com.example.demo.book.BookDto;
import com.example.demo.book.BookService;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class BookDetailsServiceTest {

    private static final BookDto BOOK = new BookDto(1L, "T", "A", 2000);

    private final BookService bookService = mock(BookService.class);
    private final BookInfoClient client = mock(BookInfoClient.class);
    private final BookDetailsService service = new BookDetailsService(bookService, client, 200);

    @Test
    void failedCallFallsBackToNullInsteadOfFailing() {
        when(bookService.findById(1L)).thenReturn(BOOK);
        when(client.fetchRating("T")).thenReturn(CompletableFuture.failedFuture(new IllegalStateException("down")));
        when(client.fetchCoverUrl("T")).thenReturn(CompletableFuture.completedFuture("url"));

        BookDetails details = service.details(1L);

        assertThat(details).isEqualTo(new BookDetails(BOOK, null, "url"));
    }

    @Test
    void hangingCallIsCutByTimeout() {
        when(bookService.findById(1L)).thenReturn(BOOK);
        when(client.fetchRating("T")).thenReturn(CompletableFuture.completedFuture(4.5));
        when(client.fetchCoverUrl("T")).thenReturn(new CompletableFuture<>());
        long start = System.nanoTime();

        BookDetails details = service.details(1L);

        long elapsedMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);
        assertThat(details).isEqualTo(new BookDetails(BOOK, 4.5, null));
        assertThat(elapsedMs).isLessThan(1000);
    }
}
