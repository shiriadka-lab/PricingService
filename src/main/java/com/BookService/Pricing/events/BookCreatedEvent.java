package com.BookService.Pricing.events;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;


@JsonIgnoreProperties(ignoreUnknown = true)
public class BookCreatedEvent {

	@JsonProperty("id") 
    private Long bookId;
    private BigDecimal price;
    private String currency;

    public BookCreatedEvent() {}

    // Getters & setters
    public Long getBookId() { return bookId; }
    public void setBookId(Long bookId) { this.bookId = bookId; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    @Override
    public String toString() {
        return "BookCreatedEvent{" +
               "bookId=" + bookId +
               ", price=" + price +
               ", currency='" + currency + '\'' +
               '}';
    }
}

