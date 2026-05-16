package com.bloodbank.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Enables @Async support for non-blocking email sending.
 */
@Configuration
@EnableAsync
public class AsyncConfig {
    // Spring Boot auto-configures the thread pool from application.properties
}
