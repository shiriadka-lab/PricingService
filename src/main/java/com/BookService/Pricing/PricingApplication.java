package com.BookService.Pricing;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient  // ← ADD THIS: registers with Eureka on startup, deregisters on shutdown
public class PricingApplication {

	public static void main(String[] args) {
		SpringApplication.run(PricingApplication.class, args);
	}

}
