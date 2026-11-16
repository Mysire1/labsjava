package com.example.demo.async;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
public class BookInfoClient {

    @Value("${app.slow.delay-ms:1000}")
    private long delayMs;

    @Async
    public CompletableFuture<Double> fetchRating(String title) {
        log.info("Fetching rating for '{}' in {}", title, Thread.currentThread().getName());
        Slow.sleep(delayMs);
        return CompletableFuture.completedFuture(4.0 + Math.abs(title.hashCode() % 10) / 10.0);
    }

    @Async
    public CompletableFuture<String> fetchCoverUrl(String title) {
        log.info("Fetching cover for '{}' in {}", title, Thread.currentThread().getName());
        Slow.sleep(delayMs);
        return CompletableFuture.completedFuture("https://covers.example.com/" + title.replace(' ', '-') + ".jpg");
    }
}
