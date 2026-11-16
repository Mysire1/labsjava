package com.example.demo.async;

import com.example.demo.book.BookDetails;
import com.example.demo.book.BookDetailsService;
import com.example.demo.book.BookDto;
import com.example.demo.book.BookService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@SpringJUnitConfig
@Import({AsyncConfig.class, LoggingAsyncExceptionHandler.class, BookNotifier.class, BookInfoClient.class,
        BookDetailsService.class, AsyncExecutionTest.ThreadProbe.class})
@TestPropertySource(properties = {"app.slow.delay-ms=500", "app.async.timeout-ms=2000"})
class AsyncExecutionTest {

    private static final BookDto BOOK = new BookDto(1L, "Clean Code", "Robert Martin", 2008);

    @Autowired
    private BookNotifier notifier;

    @Autowired
    private BookDetailsService detailsService;

    @Autowired
    private ThreadProbe probe;

    @Autowired
    private LoggingAsyncExceptionHandler exceptionHandler;

    @MockitoBean
    private BookService bookService;

    @Test
    void fireAndForgetReturnsBeforeSlowMethodFinishes() throws InterruptedException {
        int before = notifier.getSentCount();
        long start = System.nanoTime();

        notifier.notifyNewBook(BOOK);

        long elapsedMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);
        assertThat(elapsedMs).isLessThan(200);
        assertThat(notifier.getSentCount()).isEqualTo(before);
        waitUntil(() -> notifier.getSentCount() == before + 1);
    }

    @Test
    void asyncMethodRunsInOwnExecutorThread() {
        String thread = probe.threadName().orTimeout(2, TimeUnit.SECONDS).join();

        assertThat(thread).startsWith(AsyncConfig.THREAD_PREFIX);
    }

    @Test
    void twoSlowCallsRunConcurrentlyAndAreCombined() {
        when(bookService.findById(1L)).thenReturn(BOOK);
        long start = System.nanoTime();

        BookDetails details = detailsService.details(1L);

        long elapsedMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);
        assertThat(details.book()).isEqualTo(BOOK);
        assertThat(details.rating()).isNotNull();
        assertThat(details.coverUrl()).contains("Clean-Code");
        assertThat(elapsedMs).isGreaterThanOrEqualTo(500).isLessThan(900);
    }

    @Test
    void voidAsyncFailureIsRecordedByUncaughtHandler() throws InterruptedException {
        int before = exceptionHandler.getFailureCount();

        notifier.notifyNewBook(new BookDto(2L, null, null, null));

        waitUntil(() -> exceptionHandler.getFailureCount() == before + 1);
    }

    private static void waitUntil(BooleanSupplier condition) throws InterruptedException {
        long deadline = System.currentTimeMillis() + 3000;
        while (!condition.getAsBoolean()) {
            assertThat(System.currentTimeMillis()).isLessThan(deadline);
            Thread.sleep(20);
        }
    }

    @Component
    static class ThreadProbe {

        @Async
        public CompletableFuture<String> threadName() {
            return CompletableFuture.completedFuture(Thread.currentThread().getName());
        }
    }
}
