package com.hikaro.warehouse.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class ServiceExecutionTimeLoggingAspect {

    private static final Logger log = LoggerFactory.getLogger(ServiceExecutionTimeLoggingAspect.class);

    @Around("within(@org.springframework.stereotype.Service *)")
    public Object logExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        try {
            return joinPoint.proceed();
        } finally {
            long executionTimeMs = System.currentTimeMillis() - startTime;
            log.info(
                    "Service method {}.{} executed in {} ms",
                    joinPoint.getSignature().getDeclaringTypeName(),
                    joinPoint.getSignature().getName(),
                    executionTimeMs
            );
        }
    }
}
