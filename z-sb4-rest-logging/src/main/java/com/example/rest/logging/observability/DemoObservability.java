package com.example.rest.logging.observability;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationRegistry;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

@Component
public class DemoObservability {

    private final MeterRegistry meterRegistry;
    private final ObservationRegistry observationRegistry;

    public DemoObservability(MeterRegistry meterRegistry, ObservationRegistry observationRegistry) {
        this.meterRegistry = meterRegistry;
        this.observationRegistry = observationRegistry;
    }

    public <T> T observe(String component, String operation, Supplier<T> action) {
        Observation observation = Observation.start("zsb4.rest.operation", observationRegistry)
                .contextualName(component + "." + operation)
                .lowCardinalityKeyValue("component", component)
                .lowCardinalityKeyValue("operation", operation);

        long start = System.nanoTime();
        try (Observation.Scope scope = observation.openScope()) {
            T result = action.get();
            recordOutcome(component, operation, "success", start);
            return result;
        } catch (RuntimeException ex) {
            observation.error(ex);
            recordOutcome(component, operation, "error", start);
            throw ex;
        } finally {
            observation.stop();
        }
    }

    public void recordBatchSize(String operation, int size) {
        DistributionSummary.builder("zsb4.rest.batch.size")
                .description("Size of request batches processed by the demo endpoints")
                .tag("operation", operation)
                .register(meterRegistry)
                .record(size);
    }

    private void recordOutcome(String component, String operation, String outcome, long startNanos) {
        long elapsedNanos = System.nanoTime() - startNanos;

        Timer.builder("zsb4.rest.operation.duration")
                .description("Duration of demo REST operations")
                .publishPercentileHistogram()
                .tag("component", component)
                .tag("operation", operation)
                .tag("outcome", outcome)
                .register(meterRegistry)
                .record(elapsedNanos, TimeUnit.NANOSECONDS);

        Counter.builder("zsb4.rest.operation.calls")
                .description("Number of demo REST operations")
                .tag("component", component)
                .tag("operation", operation)
                .tag("outcome", outcome)
                .register(meterRegistry)
                .increment();
    }
}
