package com.example.demo.async;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.task.TaskRejectedException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.CountDownLatch;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TaskRejectionTest {

    private final CountDownLatch release = new CountDownLatch(1);
    private final ThreadPoolTaskExecutor executor = tinyExecutor();

    @AfterEach
    void tearDown() {
        release.countDown();
        executor.shutdown();
    }

    @Test
    void tinyExecutorRejectsExtraTask() {
        executor.execute(this::block);
        executor.execute(this::block);

        assertThatThrownBy(() -> executor.execute(this::block)).isInstanceOf(TaskRejectedException.class);
    }

    @Test
    void rejectionBecomes503TryLater() {
        executor.execute(this::block);
        executor.execute(this::block);
        TaskRejectedException rejected = null;
        try {
            executor.execute(this::block);
        } catch (TaskRejectedException e) {
            rejected = e;
        }

        ResponseEntity<ProblemDetail> response = new TaskRejectedAdvice().handle(rejected);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
        assertThat(response.getHeaders().getFirst(HttpHeaders.RETRY_AFTER)).isEqualTo("5");
        assertThat(response.getBody().getDetail()).contains("try again later");
    }

    private ThreadPoolTaskExecutor tinyExecutor() {
        ThreadPoolTaskExecutor tiny = new ThreadPoolTaskExecutor();
        tiny.setCorePoolSize(1);
        tiny.setMaxPoolSize(1);
        tiny.setQueueCapacity(1);
        tiny.setThreadNamePrefix("tiny-");
        tiny.initialize();
        return tiny;
    }

    private void block() {
        try {
            release.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
