package com.example.demo.async;

import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Component
public class LoggingAsyncExceptionHandler implements AsyncUncaughtExceptionHandler {

    private final AtomicInteger failures = new AtomicInteger();

    @Override
    public void handleUncaughtException(Throwable ex, Method method, Object... params) {
        failures.incrementAndGet();
        log.error("Async method {} failed", method.getName(), ex);
    }

    public int getFailureCount() {
        return failures.get();
    }
}
