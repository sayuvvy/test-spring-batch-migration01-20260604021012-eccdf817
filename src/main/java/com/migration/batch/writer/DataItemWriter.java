package com.migration.batch.writer;

import com.migration.batch.model.TargetEntity;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;

/**
 * Exactly-once, idempotent JdbcBatchItemWriter for TargetEntity.
 * Implements idempotent writes by using a MERGE/UPSERT pattern to ensure
 * each target entity is processed only once regardless of batch retries or restarts.
 */
@Configuration
@Component
public class DataItemWriter {

    private final JdbcBatchItemWriter<TargetEntity> jdbcBatchItemWriter;

    @Autowired
    public DataItemWriter(DataSource dataSource) {
        this.jdbcBatchItemWriter = createJdbcBatchItemWriter(dataSource);
    }

    public JdbcBatchItemWriter<TargetEntity> getJdbcBatchItemWriter() {
        return jdbcBatchItemWriter;
    }

    private JdbcBatchItemWriter<TargetEntity> createJdbcBatchItemWriter(DataSource dataSource) {
        NamedParameterJdbcTemplate jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);

        JdbcBatchItemWriter<TargetEntity> writer = new JdbcBatchItemWriter<>();
        writer.setJdbcTemplate(jdbcTemplate);
        
        // MERGE/UPSERT SQL ensures exactly-once semantics
        // Replace with actual table and column names as per your schema
        writer.setSql("MERGE INTO dw.FACT_Product_Merchant_Summary AS target\n" +
                       "USING (VALUES (:groupKey1, :groupKey2, :metricCount, :metricSum, :metricAvg, :metricMax, :streamSource, :inStockStatus, :etlLoadTimestamp))\n" +
                       "AS source (GroupKey1, GroupKey2, MetricCount, MetricSum, MetricAvg, MetricMax, StreamSource, InStockStatus, ETLLoadTimestamp)\n" +
                       "ON target.GroupKey1 = source.GroupKey1 AND target.GroupKey2 = source.GroupKey2\n" +
                       "WHEN MATCHED THEN\n" +
                       "    UPDATE SET MetricCount = source.MetricCount,\n" +
                       "               MetricSum = source.MetricSum,\n" +
                       "               MetricAvg = source.MetricAvg,\n" +
                       "               MetricMax = source.MetricMax,\n" +
                       "               StreamSource = source.StreamSource,\n" +
                       "               InStockStatus = source.InStockStatus,\n" +
                       "               ETLLoadTimestamp = source.ETLLoadTimestamp\n" +
                       "WHEN NOT MATCHED THEN\n" +
                       "    INSERT (GroupKey1, GroupKey2, MetricCount, MetricSum, MetricAvg, MetricMax, StreamSource, InStockStatus, ETLLoadTimestamp)\n" +
                       "    VALUES (source.GroupKey1, source.GroupKey2, source.MetricCount, source.MetricSum, source.MetricAvg, source.MetricMax, source.StreamSource, source.InStockStatus, source.ETLLoadTimestamp)");

        writer.setItemPreparedStatementSetter((item, ps) -> {
            ps.setString(1, item.getGroupKey1());
            ps.setString(2, item.getGroupKey2());
            ps.setInt(3, item.getMetricCount());
            ps.setBigDecimal(4, item.getMetricSum());
            ps.setBigDecimal(5, item.getMetricAvg());
            ps.setBigDecimal(6, item.getMetricMax());
            ps.setString(7, item.getStreamSource());
            ps.setString(8, item.getInStockStatus());
            ps.setTimestamp(9, Timestamp.valueOf(item.getEtlLoadTimestamp()));
        });

        // Configure skip and retry policies for exactly-once semantics
        writer.setSkipLimit(100);
        writer.setExceptionHandler(e -> {
            // Log and handle exceptions; ensure transaction rollback for idempotency
            throw e;
        });

        return writer;
    }
}
