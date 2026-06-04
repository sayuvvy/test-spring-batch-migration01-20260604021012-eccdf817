package com.migration.batch.config;

import com.migration.batch.BatchApplication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.concurrent.TimeUnit;

@Configuration
@EnableScheduling
public class SchedulerConfig {
    
    private static final Logger logger = LoggerFactory.getLogger(SchedulerConfig.class);
    
    @Autowired
    private Job job;
    
    @Scheduled(cron = "0 2 * * * ?") // Run daily at 02:00 UTC
    public void scheduleBatchJob() {
        logger.info("Starting scheduled batch job execution");
        
        try {
            JobParameters jobParameters = new JobParametersBuilder()
                    .toJobParameters();
            
            BatchApplication.getJobLauncher().run(job, jobParameters);
            
            logger.info("Scheduled batch job completed successfully");
        } catch (Exception e) {
            logger.error("Error occurred during scheduled batch job execution: {}", e.getMessage(), e);
        }
    }
    
    @Scheduled(fixedRate = 300000) // Log every 5 minutes
    public void logSchedulerStatus() {
        logger.debug("Scheduler is active and running");
    }
}
