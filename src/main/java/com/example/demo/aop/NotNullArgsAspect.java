package com.example.demo.aop;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class NotNullArgsAspect {

    @Before("@annotation(com.example.demo.aop.NotNullArgs)")
    public void check(JoinPoint jp) {
        Object[] args = jp.getArgs();
        for (int i = 0; i < args.length; i++) {
            if (args[i] == null) {
                throw new IllegalArgumentException(
                        "Argument #" + i + " of " + jp.getSignature().toShortString() + " must not be null");
            }
        }
    }
}
