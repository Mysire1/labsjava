package com.example.demo.book;

import com.example.demo.async.BookInfoClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class BookDetailsService {

    private final BookService bookService;
    private final BookInfoClient infoClient;
    private final long timeoutMs;

    public BookDetailsService(BookService bookService, BookInfoClient infoClient,
                              @Value("${app.async.timeout-ms:2000}") long timeoutMs) {
        this.bookService = bookService;
        this.infoClient = infoClient;
        this.timeoutMs = timeoutMs;
    }

    public BookDetails details(Long id) {
        BookDto book = bookService.findById(id);
        CompletableFuture<Double> rating = safe(infoClient.fetchRating(book.title()), "rating");
        CompletableFuture<String> cover = safe(infoClient.fetchCoverUrl(book.title()), "cover");
        return rating.thenCombine(cover, (r, c) -> new BookDetails(book, r, c)).join();
    }

    private <T> CompletableFuture<T> safe(CompletableFuture<T> future, String what) {
        return future
                .orTimeout(timeoutMs, TimeUnit.MILLISECONDS)
                .exceptionally(ex -> {
                    log.warn("Could not get {}: {}", what, ex.toString());
                    return null;
                });
    }
}
