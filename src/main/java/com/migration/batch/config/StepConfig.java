package com.migration.batch.config;

import com.migration.batch.listener.JobLoggingListener;
import com.migration.batch.listener.StepLoggingListener;
import com.migration.batch.policy.SkipAndRetryPolicy;
import com.migration.batch.processor.ProductMerchantProcessor;
import com.migration.batch.reader.ProductJdbcReader;
import com.migration.batch.writer.ProductMerchantWriter;
import jakarta.annotation.Resource;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.skip.SkipPolicy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

@Configuration
public class StepConfig {

    @Resource
    private JobRepository jobRepository;

    @Resource
    private PlatformTransactionManager transactionManager;

    @Resource
    private DataSource dataSource;

    @Resource
    private JobLoggingListener jobLoggingListener;

    @Resource
    private StepLoggingListener stepLoggingListener;

    @Resource
    private SkipAndRetryPolicy skipAndRetryPolicy;

    @Resource
    private ProductJdbcReader productJdbcReader;

    @Resource
    private ProductMerchantProcessor productMerchantProcessor;

    @Resource
    private ProductMerchantWriter productMerchantWriter;

    @Bean
    @JobScope
    public org.springframework.batch.core.job.JobConfiguration productMerchantJob() {
        return JobBuilder.builder(jobRepository)
                .listener(jobLoggingListener)
                .start(productMerchantStep())
                .build();
    }

    @Bean
    @JobScope
    public org.springframework.batch.core.step.StepConfiguration productMerchantStep() {
        return StepBuilder.builder(jobRepository, transactionManager)
                .name("productMerchantStep")
                .<Object, Object>chunk(1000, new SimpleAsyncTaskExecutor())
                .reader(productJdbcReader)
                .processor(productMerchantProcessor)
                .writer(productMerchantWriter)
                .listener(stepLoggingListener)
                .skipPolicy(skipAndRetryPolicy)
                .retryPolicy(skipAndRetryPolicy)
                .build();
    }
}
