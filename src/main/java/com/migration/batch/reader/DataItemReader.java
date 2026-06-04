package com.migration.batch.reader;

import org.springframework.batch.item.database.JdbcPagingItemReader;
import org.springframework.batch.item.database.Order;
import org.springframework.batch.item.database.PagingQueryProvider;
import org.springframework.batch.item.database.builder.JdbcPagingItemReaderBuilder;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.stereotype.Component;

import com.migration.batch.model.ProductData;
import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

/**
 * DataItemReader implementation using JdbcPagingItemReader for SQL Server.
 * Implements efficient paging for large datasets with configurable page size.
 */
@Component
public class DataItemReader {

    public JdbcPagingItemReader<ProductData> reader(DataSource dataSource) {
        return new JdbcPagingItemReaderBuilder<ProductData>()
                .name("productJdbcPagingItemReader")
                .dataSource(dataSource)
                .pageSize(1000) // Configured page size as per requirements
                .rowMapper(new BeanPropertyRowMapper<>(ProductData.class))
                .queryProvider(createQueryProvider())
                .build();
    }

    /**
     * Creates a paging query provider for product data retrieval.
     * Includes ordering by ProductID to ensure consistent pagination.
     */
    private PagingQueryProvider createQueryProvider() {
        return () -> {
            String baseQuery = "SELECT ProductID, ProductName, UnitPrice, QuantityOnHand, SupplierID, ModifiedDate " +
                               "FROM dbo.Product WHERE ModifiedDate BETWEEN :fromDate AND :toDate";
            
            Map<String, Order> sorts = new HashMap<>();
            sorts.put("ProductID", Order.ASCENDING);
            
            return new org.springframework.batch.item.database.PagingQueryProvider.QueryProvider() {
                @Override
                public String getSql() {
                    return baseQuery;
                }

                @Override
                public Map<String, Object> getParameters() {
                    // Parameters will be resolved at runtime through StepScope
                    return Map.of();
                }

                @Override
                public Map<String, Order> getSorts() {
                    return sorts;
                }
            };
        };
    }
}
