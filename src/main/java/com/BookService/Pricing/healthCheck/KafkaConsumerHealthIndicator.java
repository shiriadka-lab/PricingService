package com.BookService.Pricing.healthCheck;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.listener.MessageListenerContainer;
import org.springframework.stereotype.Component;

@Component
public class KafkaConsumerHealthIndicator implements HealthIndicator {

    private final KafkaListenerEndpointRegistry registry;

    public KafkaConsumerHealthIndicator(
            KafkaListenerEndpointRegistry registry) {
        this.registry = registry;
    }

    @Override
    public Health health() {
        boolean allRunning = registry.getListenerContainers()
            .stream()
            .allMatch(MessageListenerContainer::isRunning);

        if (allRunning) {
            return Health.up().build();
        }

        return Health.down()
                     .withDetail("reason", "One or more listener containers stopped")
                     .build();
    }
}
