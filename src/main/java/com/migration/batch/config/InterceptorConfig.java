package com.migration.batch.config;

import org.springframework.batch.core.listener.StepExecutionListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.migration.batch.interceptor.ProductMerchantStepListener;
import com.migration.batch.interceptor.MerchantDataQualityListener;
import com.migration.batch.interceptor.ProductDataQualityListener;

/**
 * Configuration class for registering StepExecutionListeners
 * Implements the requirement: "Create mandatory interceptor java package 
 * to accomodate StepExecutionListener implementation"
 */
@Configuration
public class InterceptorConfig {
    
    /**
     * Registers a StepExecutionListener for Product data quality validation
     * Implements BRD requirement: "Data Quality Rules" and "Error Handling"
     */
    @Bean
    public StepExecutionListener productDataQualityListener() {
        return new ProductDataQualityListener();
    }
    
    /**
     * Registers a StepExecutionListener for Merchant data quality validation
     * Implements BRD requirement: "Data Quality Rules" and "Error Handling"
     */
    @Bean
    public StepExecutionListener merchantDataQualityListener() {
        return new MerchantDataQualityListener();
    }
    
    /**
     * Registers a StepExecutionListener for Product-Merchant join processing
     * Implements BRD requirement: "Join Product-Merchant Data" and 
     * "Conditional Branching Logic"
     */
    @Bean
    public StepExecutionListener productMerchantStepListener() {
        return new ProductMerchantStepListener();
    }
}
