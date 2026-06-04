package com.migration.batch.config;

import com.migration.batch.listener.JobLoggingListener;
import com.migration.batch.listener.StepLoggingListener;
import com.migration.batch.policy.SkipAndRetryPolicy;
import jakarta.annotation.PreDestroy;
import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.configuration.annotation.DefaultBatchConfigurer;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.JobExecutionListener;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.listener.StepExecutionListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import javax.sql.DataSource;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class JobConfig {

    private final JobRepository jobRepository;
    private final DataSourceTransactionManager transactionManager;
    private final DataSource dataSource;
    private final JobLoggingListener jobLoggingListener;
    private final StepLoggingListener stepLoggingListener;
    private final StepExecutionListener stepExecutionListener;
    private final SkipAndRetryPolicy skipAndRetryPolicy;

    @Autowired
    private EntityManagerFactory entityManagerFactory;

    @Bean
    public Job productMerchantETLJob() {
        return new JobBuilder("ProductMerchantETLJob", jobRepository)
                .listener(jobLoggingListener)
                .start(step1())
                .build();
    }

    private org.springframework.batch.core.step.Step step1() {
        return new StepBuilder("ProcessingStep", jobRepository)
                .<Object, Object>chunk(1000, transactionManager)
                .reader(null) // Reader will be injected later
                .processor(null) // Processor will be injected later  
                .writer(null) // Writer will be injected later
                .faultTolerant()
                .skipPolicy(skipAndRetryPolicy)
                .retryPolicy(skipAndRetryPolicy)
                .listener(stepLoggingListener)
                .listener(stepExecutionListener)
                .taskExecutor(new SimpleAsyncTaskExecutor())
                .build();
    }

    @PreDestroy
    public void destroy() {
        if (entityManagerFactory != null && entityManagerFactory.isOpen()) {
            entityManagerFactory.getCache().clear();
            log.info("EntityManagerFactory closed");
        }
    }
}
