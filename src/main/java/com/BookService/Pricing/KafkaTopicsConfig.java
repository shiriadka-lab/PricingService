package com.BookService.Pricing;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

@Configuration
@Profile("docker")  // only active in Docker
public class KafkaTopicsConfig {

    @Bean
    public NewTopic bookCreatedTopic() {
        return TopicBuilder.name("book-created")
                .partitions(1)
                .replicas(1)
                .build();
    }
    
    @Bean
    public DefaultErrorHandler kafkaErrorHandler(
            KafkaTemplate<Object, Object> kafkaTemplate) {

        DeadLetterPublishingRecoverer recoverer =
                new DeadLetterPublishingRecoverer(kafkaTemplate);

        // retry 3 times with 1 second backoff
        return new DefaultErrorHandler(
                recoverer,
                new FixedBackOff(1000L, 3) // retry 3 times
        );
    }
}