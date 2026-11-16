package com.example.demo.async;

import com.example.demo.book.BookDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Component
public class BookNotifier {

    private final AtomicInteger sent = new AtomicInteger();

    @Value("${app.slow.delay-ms:1000}")
    private long delayMs;

    @Async
    public void notifyNewBook(BookDto book) {
        if (book.title() == null) {
            throw new IllegalArgumentException("Cannot send email for book without title");
        }
        Slow.sleep(delayMs);
        sent.incrementAndGet();
        log.info("Email 'New book: {}' sent from {}", book.title(), Thread.currentThread().getName());
    }

    public int getSentCount() {
        return sent.get();
    }
}
