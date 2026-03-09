package com.BookService.Pricing.persistence.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import com.BookService.Pricing.persistence.model.BookPrice;

public interface BookPriceRepository extends JpaRepository<BookPrice, Long> {

}
