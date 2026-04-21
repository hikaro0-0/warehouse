package com.hikaro.warehouse.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.hikaro.warehouse.dto.CounterStateResponseDto;
import com.hikaro.warehouse.dto.RaceConditionDemoResponseDto;
import org.junit.jupiter.api.Test;

class ConcurrencyDemoServiceTest {

    private final ConcurrencyDemoService concurrencyDemoService =
            new ConcurrencyDemoService();

    @Test
    void shouldIncrementAndResetThreadSafeCounters() {
        CounterStateResponseDto resetState = concurrencyDemoService.resetCounters();
        assertEquals(0, resetState.synchronizedCounter());
        assertEquals(0, resetState.atomicCounter());

        CounterStateResponseDto synchronizedState =
                concurrencyDemoService.incrementSynchronizedCounter();
        assertEquals(1, synchronizedState.synchronizedCounter());
        assertEquals(0, synchronizedState.atomicCounter());

        CounterStateResponseDto atomicState =
                concurrencyDemoService.incrementAtomicCounter();
        assertEquals(1, atomicState.synchronizedCounter());
        assertEquals(1, atomicState.atomicCounter());

        CounterStateResponseDto finalResetState = concurrencyDemoService.resetCounters();
        assertEquals(0, finalResetState.synchronizedCounter());
        assertEquals(0, finalResetState.atomicCounter());
    }

    @Test
    void shouldDemonstrateRaceConditionAndSafeCounters() {
        RaceConditionDemoResponseDto response =
                concurrencyDemoService.runRaceConditionDemo(64, 2000);

        assertEquals(64, response.threadCount());
        assertEquals(2000, response.incrementsPerThread());
        assertEquals(128000, response.expectedValue());
        assertEquals(128000, response.synchronizedCounterValue());
        assertEquals(128000, response.atomicCounterValue());
        assertTrue(response.raceConditionDetected());
        assertTrue(response.unsafeCounterValue() < response.expectedValue());
    }

    @Test
    void shouldRejectThreadCountBelowRequiredMinimum() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> concurrencyDemoService.runRaceConditionDemo(49, 100)
        );

        assertEquals(
                "threadCount must be greater than or equal to 50",
                exception.getMessage()
        );
    }
}
