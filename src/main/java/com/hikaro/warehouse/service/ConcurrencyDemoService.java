package com.hikaro.warehouse.service;

import com.hikaro.warehouse.dto.CounterStateResponseDto;
import com.hikaro.warehouse.dto.RaceConditionDemoResponseDto;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.LockSupport;
import org.springframework.stereotype.Service;

@Service
public class ConcurrencyDemoService {

    private static final int MIN_THREAD_COUNT = 50;
    private static final long DEMO_TIMEOUT_SECONDS = 30L;

    private final AtomicInteger atomicCounter = new AtomicInteger();
    private int synchronizedCounter;

    public synchronized CounterStateResponseDto incrementSynchronizedCounter() {
        synchronizedCounter++;
        return getCounterState();
    }

    public CounterStateResponseDto incrementAtomicCounter() {
        atomicCounter.incrementAndGet();
        return getCounterState();
    }

    public synchronized CounterStateResponseDto getCounterState() {
        return new CounterStateResponseDto(synchronizedCounter, atomicCounter.get());
    }

    public synchronized CounterStateResponseDto resetCounters() {
        synchronizedCounter = 0;
        atomicCounter.set(0);
        return getCounterState();
    }

    public RaceConditionDemoResponseDto runRaceConditionDemo(
            int threadCount,
            int incrementsPerThread
    ) {
        validateDemoParameters(threadCount, incrementsPerThread);

        int expectedValue = threadCount * incrementsPerThread;
        UnsafeCounter unsafeCounter = new UnsafeCounter();
        SynchronizedCounter safeSynchronizedCounter = new SynchronizedCounter();
        AtomicInteger safeAtomicCounter = new AtomicInteger();

        executeConcurrentIncrementDemo(
                threadCount,
                incrementsPerThread,
                unsafeCounter::increment,
                safeSynchronizedCounter::increment,
                safeAtomicCounter::incrementAndGet
        );

        int unsafeValue = unsafeCounter.get();
        int synchronizedValue = safeSynchronizedCounter.get();
        int atomicValue = safeAtomicCounter.get();

        return new RaceConditionDemoResponseDto(
                threadCount,
                incrementsPerThread,
                expectedValue,
                unsafeValue,
                synchronizedValue,
                atomicValue,
                unsafeValue < expectedValue
        );
    }

    private void validateDemoParameters(int threadCount, int incrementsPerThread) {
        if (threadCount < MIN_THREAD_COUNT) {
            throw new IllegalArgumentException(
                    "threadCount must be greater than or equal to " + MIN_THREAD_COUNT
            );
        }
        if (incrementsPerThread <= 0) {
            throw new IllegalArgumentException(
                    "incrementsPerThread must be greater than 0"
            );
        }
    }

    private void executeConcurrentIncrementDemo(
            int threadCount,
            int incrementsPerThread,
            Runnable unsafeIncrement,
            Runnable synchronizedIncrement,
            Runnable atomicIncrement
    ) {
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch ready = new CountDownLatch(threadCount);
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(threadCount);

        try {
            for (int i = 0; i < threadCount; i++) {
                executor.submit(() -> {
                    ready.countDown();
                    awaitLatch(start, "Timed out waiting to start race condition demo");

                    for (int attempt = 0; attempt < incrementsPerThread; attempt++) {
                        unsafeIncrement.run();
                        synchronizedIncrement.run();
                        atomicIncrement.run();
                    }

                    done.countDown();
                });
            }

            awaitLatch(ready, "Timed out waiting for demo workers to be ready");
            start.countDown();
            awaitLatch(done, "Timed out waiting for demo workers to finish");
        } finally {
            executor.shutdown();
            awaitExecutorShutdown(executor);
        }
    }

    private void awaitLatch(CountDownLatch latch, String timeoutMessage) {
        try {
            boolean completed = latch.await(DEMO_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            if (!completed) {
                throw new IllegalStateException(timeoutMessage);
            }
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Thread was interrupted during demo execution", ex);
        }
    }

    private void awaitExecutorShutdown(ExecutorService executor) {
        try {
            boolean terminated = executor.awaitTermination(
                    DEMO_TIMEOUT_SECONDS,
                    TimeUnit.SECONDS
            );
            if (!terminated) {
                executor.shutdownNow();
                throw new IllegalStateException("Timed out waiting for executor shutdown");
            }
        } catch (InterruptedException ex) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Thread was interrupted during executor shutdown", ex);
        }
    }

    private static final class UnsafeCounter {

        private int value;

        private void increment() {
            int currentValue = value;
            // Intentionally widen the race window so lost updates are visible in the demo.
            LockSupport.parkNanos(1L);
            value = currentValue + 1;
        }

        private int get() {
            return value;
        }
    }

    private static final class SynchronizedCounter {

        private int value;

        private synchronized void increment() {
            value++;
        }

        private synchronized int get() {
            return value;
        }
    }
}
