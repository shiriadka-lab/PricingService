package com.BookService.Pricing.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.BookService.Pricing.events.BookCreatedEvent;
import com.BookService.Pricing.metrics.PricingMetricService;
import com.BookService.Pricing.persistence.model.BookPrice;
import com.BookService.Pricing.persistence.repo.BookPriceRepository;

import io.micrometer.core.annotation.Timed;



@RestController
@RequestMapping("/api/v1/pricing")
public class PricingController {
	
	// creating a logger
    Logger logger
        = LoggerFactory.getLogger(LogController.class);
    
    @Autowired
    private BookPriceRepository bookPriceRepository;
    
    @Autowired
    private PricingMetricService pricingMetricService;

    
    @GetMapping
    @Timed(value = "pricing.list.time", description = "Time taken to list all book pricing", extraTags = {"service", "pricing-service"} )
    public Iterable findAll() {
        return bookPriceRepository.findAll();
    }
    
    @Transactional
    @PostMapping("/book-created")
    public ResponseEntity<String> handleBookCreated(
                            @RequestBody BookCreatedEvent event) {

        logger.info("PricingController: Received BookCreatedEvent : {}", 
                        event.getBookId());
        pricingMetricService.consumedBookCreated();

        // Same idempotency check as Kafka consumer
        if (bookPriceRepository.existsById(event.getBookId())) {
            logger.info("PricingController: Id already exists {}", 
                            event.getBookId());
            return ResponseEntity.status(HttpStatus.CONFLICT)
                                 .body("BookPrice already exists");
        }

        BookPrice bookPrice = new BookPrice(
            event.getBookId(),
            event.getPrice()
        );

        bookPriceRepository.save(bookPrice);
        pricingMetricService.recordBookCreated();

        return ResponseEntity.status(HttpStatus.CREATED)
                             .body("BookPrice created successfully");
    }

}
