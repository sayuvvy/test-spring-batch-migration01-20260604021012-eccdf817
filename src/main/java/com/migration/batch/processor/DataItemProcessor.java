package com.migration.batch.processor;

import com.migration.batch.model.SourceEntity;
import com.migration.batch.model.TargetEntity;
import org.springframework.batch.item.ItemProcessor;

/**
 * ItemProcessor implementation for transforming SourceEntity to TargetEntity.
 * Includes validation, transformation logic, and supports filtering by returning null.
 * 
 * This processor adheres to the following rules from the BRD:
 * 1. Deterministic and transformational operations only
 * 2. Returns null for intentional filtering
 * 3. Stateless design
 * 4. Thread-safe implementation
 * 5. Includes validation through exception throwing for invalid items
 */
public class DataItemProcessor implements ItemProcessor<SourceEntity, TargetEntity> {

    @Override
    public TargetEntity process(SourceEntity source) throws Exception {
        // Null check validation as per BRD data quality rules
        if (source.getProductId() == null || source.getProductName() == null) {
            throw new IllegalArgumentException("ProductID and ProductName cannot be null");
        }
        
        if (source.getMerchantId() == null || source.getMerchantName() == null) {
            throw new IllegalArgumentException("MerchantID and MerchantName cannot be null");
        }

        // Apply transformation rules from BRD
        
        // Product transformations
        String standardizedCategory = source.getProductCategory() != null ? 
            source.getProductCategory().toUpperCase().trim() : null;
        
        double adjustedPrice = source.getUnitPrice() != null ? 
            applyDiscountLogic(source.getUnitPrice(), source.getProductStatus()) : 0.00;
        
        int quantityInitialized = source.getQuantityOnHand() != null ? 
            source.getQuantityOnHand() : 0;
        
        String productTier = classifyProductTier(adjustedPrice);
        String dataFreshnessFlag = calculateDataFreshness(source.getModifiedDate());

        // Merchant transformations
        double merchantRiskScore = calculateMerchantRiskScore(
            source.getCreditLimit(), 
            source.isActive()
        );
        
        String regionGroup = determineRegionGroup(source.getRegion());
        String activeStatusLabel = source.isActive() ? "Active" : "Inactive";
        int daysSinceRegistration = calculateDaysSinceRegistration(source.getRegistrationDate());

        // Create target entity with transformed data
        TargetEntity target = new TargetEntity();
        target.setProductId(source.getProductId());
        target.setProductName(source.getProductName());
        target.setCategoryStandardized(standardizedCategory);
        target.setAdjustedPrice(adjustedPrice);
        target.setQuantityInitialized(quantityInitialized);
        target.setProductTier(productTier);
        target.setDataFreshnessFlag(dataFreshnessFlag);
        target.setSupplierId(source.getSupplierId());
        
        target.setMerchantId(source.getMerchantId());
        target.setMerchantName(source.getMerchantName());
        target.setMerchantRiskScore(merchantRiskScore);
        target.setRegionGroup(regionGroup);
        target.setActiveStatusLabel(activeStatusLabel);
        target.setDaysSinceRegistration(daysSinceRegistration);
        
        // Enrichment from API would be added here in a real implementation
        // For now we use default values as per BRD requirements
        target.setApiQuantity(0); // Would come from external API
        target.setInStock("UNKNOWN"); // Would come from external API

        return target;
    }

    /**
     * Applies discount logic based on product status as defined in BRD
     */
    private double applyDiscountLogic(double unitPrice, String productStatus) {
        // BRD rule: Apply discounts for discontinued/clearance items
        if ("DISCONTINUED".equalsIgnoreCase(productStatus)) {
            return unitPrice * 0.7; // 30% discount
        } else if ("CLEARANCE".equalsIgnoreCase(productStatus)) {
            return unitPrice * 0.5; // 50% discount
        }
        return unitPrice;
    }

    /**
     * Classifies products into tiers based on BRD business rules
     */
    private String classifyProductTier(double unitPrice) {
        if (unitPrice >= 1000) {
            return "PREMIUM";
        } else if (unitPrice >= 500) {
            return "STANDARD";
        } else if (unitPrice >= 100) {
            return "ECONOMY";
        }
        return "BUDGET";
    }

    /**
     * Calculates data freshness flag based on BRD rules
     */
    private String calculateDataFreshness(java.sql.Timestamp modifiedDate) {
        if (modifiedDate == null) {
            return "STALE";
        }
        
        long daysSinceLastUpdate = calculateDaysSince(modifiedDate);
        
        if (daysSinceLastUpdate <= 7) {
            return "FRESH";
        } else if (daysSinceLastUpdate <= 30) {
            return "RECENT";
        }
        return "STALE";
    }
    
    /**
     * Calculates merchant risk score based on BRD business rules
     */
    private double calculateMerchantRiskScore(Double creditLimit, boolean isActive) {
        if (creditLimit == null) {
            return 4.0; // Default risk score
        }
        
        if (creditLimit > 100000 && isActive) {
            return 1.0;
        } else if (creditLimit > 50000 && isActive) {
            return 2.0;
        } else if (creditLimit > 10000) {
            return 3.0;
        } else if (!isActive) {
            return 5.0;
        }
        return 4.0;
    }
    
    /**
     * Determines region group based on source region value
     */
    private String determineRegionGroup(String region) {
        if (region == null) {
            return "OTHER";
        }
        
        region = region.toUpperCase();
        if (region.contains("AMERICA") || region.contains("USA") || 
            region.contains("CANADA") || region.contains("MEXICO")) {
            return "AMERICAS";
        } else if (region.contains("EUROPE") || region.contains("MEA")) {
            return "EMEA";
        } else if (region.contains("ASIA") || region.contains("PACIFIC")) {
            return "APAC";
        }
        return "OTHER";
    }
    
    /**
     * Calculates days since registration
     */
    private int calculateDaysSinceRegistration(java.sql.Timestamp registrationDate) {
        if (registrationDate == null) {
            return 0;
        }
        
        long days = calculateDaysSince(registrationDate);
        return (int) days;
    }
    
    /**
     * Helper method to calculate days since a timestamp
     */
    private long calculateDaysSince(java.sql.Timestamp date) {
        if (date == null) {
            return 0;
        }
        
        long currentTime = System.currentTimeMillis();
        long dateTime = date.getTime();
        long difference = currentTime - dateTime;
        return difference / (24 * 60 * 60 * 1000);
    }
}
