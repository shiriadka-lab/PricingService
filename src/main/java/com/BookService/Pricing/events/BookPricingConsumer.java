package com.BookService.Pricing.events;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
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

	@Autowired
    private BookPriceRepository bookPriceRepository;
	
	@Autowired
	private PricingMetricService pricingMetricService;
    
    Logger logger
    	= LoggerFactory.getLogger(LogController.class);

    @Transactional
    @KafkaListener(
        topics = "book-created",
        groupId = "pricing-service"
    )
    public void handleBookCreated(BookCreatedEvent event) {

    	logger.info("BookPricingConsumer: Recieved BookCreatedEvent : {}", event.getBookId());
    	pricingMetricService.consumedBookCreated();
        // ✅ Idempotency check
        if (bookPriceRepository.existsById(event.getBookId())) {
        	logger.info("BookPricingConsumer: Error Id already exists", event.getBookId());
            return; // event already processed
        }

        BookPrice bookPrice = new BookPrice(
            event.getBookId(),
            event.getPrice()
        );
//        bookPrice.setCurrency(event.getCurrency() != null ? event.getCurrency() : "USD");

        bookPriceRepository.save(bookPrice);
        pricingMetricService.recordBookCreated();
    }
}

