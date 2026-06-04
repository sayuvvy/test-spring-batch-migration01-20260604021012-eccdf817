package com.migration.batch.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * CommonBeanConfig provides common beans required across the batch processing pipeline.
 * This includes ObjectMapper for JSON processing and RestTemplate for external API calls.
 */
@Configuration
public class CommonBeanConfig {

    /**
     * Provides an ObjectMapper bean for JSON serialization/deserialization.
     * Configured for consistent JSON handling across the application.
     * 
     * @return Configured ObjectMapper instance
     */
    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

    /**
     * Provides a RestTemplate bean for making HTTP requests to external services.
     * Configured with a connection timeout of 5000 milliseconds and read timeout of 10000 milliseconds.
     * 
     * @return Configured RestTemplate instance
     */
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate(clientHttpRequestFactory());
    }
    
    /**
     * Provides a ClientHttpRequestFactory with configured timeouts.
     * Connection timeout: 5000 ms, Read timeout: 10000 ms.
     * 
     * @return Configured ClientHttpRequestFactory
     */
    @Bean
    public ClientHttpRequestFactory clientHttpRequestFactory() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(5000);  // 5 seconds connection timeout
        factory.setReadTimeout(10000);     // 10 seconds read timeout
        return factory;
    }
}
