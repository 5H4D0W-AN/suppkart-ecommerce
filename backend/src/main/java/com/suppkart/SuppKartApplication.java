package com.suppkart;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Main Spring Boot Application class for SuppKart E-commerce Platform
 * 
 * Enables:
 * - JPA Auditing for automatic timestamp management
 * - Caching for performance optimization  
 * - Transaction Management for data consistency
 * - Scheduling for background tasks
 */
@SpringBootApplication
@EnableJpaAuditing
@EnableCaching
@EnableTransactionManagement
@EnableScheduling
public class SuppKartApplication {

    public static void main(String[] args) {
        SpringApplication.run(SuppKartApplication.class, args);
    }
}
