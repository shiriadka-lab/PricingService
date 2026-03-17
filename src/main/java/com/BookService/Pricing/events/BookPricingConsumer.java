package com.BookService.Pricing.events;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.BookService.Pricing.controller.LogController;
import com.BookService.Pricing.metrics.PricingMetricService;
import com.BookService.Pricing.persistence.model.BookPrice;
import com.BookService.Pricing.persistence.repo.BookPriceRepository;

@Service
@Profile("docker")
public class BookPricingConsumer {

    private BookPriceRepository bookPriceRepository;
	

	private PricingMetricService pricingMetricService;
    
    Logger logger
    	= LoggerFactory.getLogger(LogController.class);
    
    public BookPricingConsumer(BookPriceRepository bookPriceRepository, PricingMetricService pricingMetricService) {
		this.bookPriceRepository = bookPriceRepository;
		this.pricingMetricService = pricingMetricService;
	}

    /**
     *  // Spring registers this with Kafka on startup
    // No extra config bean needed
     * KafkaListener listens to the "book-created" topic and processes incoming BookCreatedEvent messages.
     * The @Transactional annotation ensures that the entire method runs within a transaction,
     *  providing atomicity and consistency when interacting with the database.
     *  Any exceptions thrown during the processing of the event will cause the transaction to roll back,
     *  preventing partial updates to the database.
     *  The DefaultErrorHandler retries on any exception thrown out of your @KafkaListener method. 
     *  
     * @param event
     */
    @Transactional
    @KafkaListener(
        topics = "book-created",
        groupId = "pricing-service"         // all instances of pricing service share the same groupId to load balance events
                                            // It is also used to track which events have been processed by this consumer group using offsets, which is crucial for
        									//enabling features like retries and dead-lettering.
        									// Same service, multiple instances → same group ID (load balancing)
        		                            // Different services → always unique group IDs (each gets all messages)
    )
    public void handleBookCreated(BookCreatedEvent event) {

    	logger.info("BookPricingConsumer: Recieved BookCreatedEvent : {}", event.getBookId());
    	pricingMetricService.consumedBookCreated();
        // ✅ Idempotency check - skip if already processed
    	// This is a simple check to prevent processing the same event multiple times, 
    	// which can happen due to retries or duplicates in Kafka.
        if (bookPriceRepository.existsById(event.getBookId())) {
        	logger.info("BookPricingConsumer: Error Id already exists", event.getBookId());
            return; // event already processed
        }

        BookPrice bookPrice = new BookPrice(
            event.getBookId(),
            event.getPrice()
        );
//        bookPrice.setCurrency(event.getCurrency() != null ? event.getCurrency() : "USD");

        try {
            bookPriceRepository.save(bookPrice);
        } catch (DataIntegrityViolationException e) {
            // CATCH THIS — concurrent duplicate, same reasoning
            logger.info("Concurrent duplicate for bookId: {}, skipping");
        } catch (DataAccessException e) {
            // DON'T CATCH THIS — genuine DB failure, let it escape
            // DefaultErrorHandler will retry
            // After max retries → DLT
            throw e;
        }
        pricingMetricService.recordBookCreated();
    }
    
 // PricingService - handles its own failures 
    @KafkaListener(
        topics = "book-created.DLT",
        groupId = "pricing-service-dlt"
    )
    public void handleDlt(BookCreatedEvent event) {
        logger.error("Message landed in DLT for bookId: {}", event.getBookId());
        // options:
        // 1. alert/notify team
        // 2. save to DB for manual inspection
        // 3. attempt a different recovery path
    }
}

