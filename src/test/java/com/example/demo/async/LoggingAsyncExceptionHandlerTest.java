package com.example.demo.async;

import com.example.demo.book.BookDto;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LoggingAsyncExceptionHandlerTest {

    @Test
    void recordsFailure() throws NoSuchMethodException {
        LoggingAsyncExceptionHandler handler = new LoggingAsyncExceptionHandler();

        handler.handleUncaughtException(new IllegalStateException("boom"),
                BookNotifier.class.getMethod("notifyNewBook", BookDto.class));

        assertThat(handler.getFailureCount()).isEqualTo(1);
    }
}
