package com.hikaro.warehouse.aop;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.aop.aspectj.annotation.AspectJProxyFactory;
import org.springframework.stereotype.Service;

class ServiceExecutionTimeLoggingAspectTest {

    @Test
    void shouldLogExecutionTimeForServiceMethod() {
        Logger logger = (Logger) LoggerFactory.getLogger(ServiceExecutionTimeLoggingAspect.class);
        ListAppender<ILoggingEvent> appender = new ListAppender<>();
        appender.start();
        logger.addAppender(appender);

        TestService target = new TestService();
        AspectJProxyFactory proxyFactory = new AspectJProxyFactory(target);
        proxyFactory.addAspect(new ServiceExecutionTimeLoggingAspect());
        TestService proxy = proxyFactory.getProxy();

        String result = proxy.process();

        logger.detachAppender(appender);

        assertEquals("ok", result);
        assertEquals(1, appender.list.size());
        assertTrue(appender.list.getFirst().getFormattedMessage().contains("TestService.process executed in"));
    }

    @Service
    static class TestService {

        String process() {
            return "ok";
        }
    }
}
