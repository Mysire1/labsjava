package com.example.demo.aop;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
@Aspect
@Component
public class TranslateDbErrorsAspect {

    @AfterThrowing(pointcut = "@annotation(com.example.demo.aop.TranslateDbErrors)", throwing = "ex")
    public void translate(JoinPoint jp, DataAccessException ex) {
        log.error("DB error in {}", jp.getSignature().toShortString(), ex);
        throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Database is unavailable", ex);
    }
}
