package com.example.demo.aop;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
@Order(1)
public class RetryAspect {

    @Around("@annotation(retry)")
    public Object retry(ProceedingJoinPoint pjp, Retry retry) throws Throwable {
        int attempts = Math.max(1, retry.attempts());
        for (int i = 1; ; i++) {
            try {
                return pjp.proceed();
            } catch (RuntimeException e) {
                if (i >= attempts) {
                    log.warn("{} failed after {} attempts", pjp.getSignature().toShortString(), i);
                    throw e;
                }
                log.warn("{} failed (attempt {}/{}): {}", pjp.getSignature().toShortString(), i, attempts, e.getMessage());
            }
        }
    }
}
