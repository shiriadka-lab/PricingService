package com.BookService.Pricing;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.serializer.DeserializationException;
import org.springframework.util.backoff.ExponentialBackOff;
import org.springframework.util.backoff.FixedBackOff;

@Configuration
@Profile("docker")  // only active in Docker
public class KafkaTopicsConfig {

	// The producer also has the same topic definition. 
	// It is not strictly necessary to have the topic defined in both producer and consumer,
	// but it can be helpful for clarity and to ensure the topic is created regardless of which service starts first.
	// it is a common practice to have topic definitions in both producer and consumer services to ensure that the 
	// necessary topics are created when either service starts.
	// If you have depends-on relationships between services, 
	// you might choose to centralize topic creation in one service to avoid duplication,
	// but having it in both can provide redundancy and ensure that the topics are created
	// regardless of the order in which services start.
    @Bean
    public NewTopic bookCreatedTopic() {
        return TopicBuilder.name("book-created")
                .partitions(1)
                .replicas(1)
                .build();
    }
    
    /** 
     * This bean defines a DefaultErrorHandler for Kafka listeners,
        which will handle exceptions that occur during message processing.
        The DeadLetterPublishingRecoverer will publish failed messages to a dead-letter topic after retries are exhausted,
     * @param kafkaTemplate
     * @return
     */
    @Bean
    public DefaultErrorHandler kafkaErrorHandler(KafkaTemplate<Object, Object> kafkaTemplate) {

    	// The DeadLetterPublishingRecoverer will publish failed messages to a dead-letter topic after retries are exhausted
        DeadLetterPublishingRecoverer recoverer =
                new DeadLetterPublishingRecoverer(kafkaTemplate);

        // The ExponentialBackOff will retry failed messages up to 3 times with an initial backoff of 1 second,
        // doubling the backoff after each attempt
        ExponentialBackOff backOff = new ExponentialBackOff(1000L, 2.0);
        // Set the maximum number of retry attempts to 3 (total of 4 attempts including the initial one)
        backOff.setMaxAttempts(3);

        DefaultErrorHandler handler = new DefaultErrorHandler(recoverer, backOff);

        // Specify exceptions that should NOT be retried, as they are unlikely to succeed on retry (e.g., deserialization errors)
        handler.addNotRetryableExceptions(
            IllegalArgumentException.class,
            DeserializationException.class
        );

        return handler;
    }
    
    // Define the dead-letter topic for failed messages
    // This topic will receive messages that failed to process after retries
    // You can customize the topic name and configuration as needed
    /**
     * A Dead Letter Topic is a safety net topic where failed messages land after all retries are exhausted. 
     * Instead of losing the message, Kafka routes it to `book-created.DLT` where you can:
		- Inspect what went wrong
		- Fix the bug and replay the messages
		- Alert your team that something needs attention
     * @return
     */
    @Bean
    public NewTopic bookCreatedDlt() {
        return TopicBuilder.name("book-created.DLT")
                .partitions(1)
                .replicas(1)
                .build();
    }
}