package com.migration.batch.listener;

import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.listener.JobExecutionListenerAdapter;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * Listener that logs comprehensive job execution details including start time,
 * end time, status, duration, and parameters.
 * 
 * Implements JobExecutionListenerAdapter for easy extension of job lifecycle events.
 */
@Component
public class JobLoggingListener extends JobExecutionListenerAdapter {

    @Override
    public void beforeJob(JobExecution jobExecution) {
        // Log job start with timestamp and parameters
        LocalDateTime startTime = LocalDateTime.now();
        System.out.println("=== JOB STARTED AT: " + startTime + " ===");
        System.out.println("Job ID: " + jobExecution.getJobId());
        System.out.println("Job Parameters: " + jobExecution.getJobParameters());
    }

    @Override
    public void afterJob(JobExecution jobExecution) {
        // Log job completion with status, duration, and exit status
        LocalDateTime endTime = LocalDateTime.now();
        Duration duration = Duration.between(jobExecution.getStartTime(), endTime);
        
        System.out.println("=== JOB COMPLETED AT: " + endTime + " ===");
        System.out.println("Job Status: " + jobExecution.getStatus());
        System.out.println("Exit Status: " + jobExecution.getExitStatus().getExitCode());
        System.out.println("Duration: " + duration.toMinutes() + " minutes");
        System.out.println("Job Parameters: " + jobExecution.getJobParameters());
    }
}
