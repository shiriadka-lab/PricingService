package com.BookService.Pricing.metrics;

import org.springframework.stereotype.Service;

import io.micrometer.core.instrument.MeterRegistry;

@Service
public class PricingMetricService {

	private final MeterRegistry meterRegistry;

    public PricingMetricService(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    public void recordBookCreated() {
        meterRegistry.counter("pricing.created").increment();
    }
    
    public void recordBookUpdated() {
        meterRegistry.counter("pricing.updated").increment();
    }
    
    public void consumedBookCreated() {
        meterRegistry.counter("kafka.consume.pricing.created").increment();
    }
}
