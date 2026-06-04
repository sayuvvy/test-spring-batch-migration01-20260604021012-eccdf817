package com.migration.batch.interceptor;

import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.listener.StepExecutionListener;
import org.springframework.batch.core.step.StepExecution;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * StepExecutionListener implementation to log step start, end, and statistics.
 * This listener provides detailed logging for step execution lifecycle events.
 */
@Component
public class StepExecutionListenerImpl implements StepExecutionListener {

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");

    /**
     * Logs the beginning of step execution.
     * 
     * @param stepExecution The current step execution
     */
    @Override
    public void beforeStep(StepExecution stepExecution) {
        JobExecution jobExecution = stepExecution.getJobExecution();
        String jobId = String.valueOf(jobExecution.getJobId());
        String stepName = stepExecution.getStepName();
        String startTime = LocalDateTime.now().format(TIME_FORMATTER);
        
        System.out.println(String.format("[BATCH] Step started - Job ID: %s, Step: %s at %s",
                jobId, stepName, startTime));
    }

    /**
     * Logs step completion and statistics.
     * 
     * @param stepExecution The completed step execution
     */
    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {
        JobExecution jobExecution = stepExecution.getJobExecution();
        String jobId = String.valueOf(jobExecution.getJobId());
        String stepName = stepExecution.getStepName();
        String endTime = LocalDateTime.now().format(TIME_FORMATTER);
        
        long startTime = stepExecution.getStartTime().getTime();
        long endTimeMillis = System.currentTimeMillis();
        long duration = (endTimeMillis - startTime) / 1000; // Convert to seconds
        
        long reads = stepExecution.getReadCount();
        long writes = stepExecution.getWriteCount();
        long skips = stepExecution.getSkipCount();
        long filterCount = stepExecution.getFilterCount();
        long fails = stepExecution.getFailCount();
        
        System.out.println(String.format("[BATCH] Step completed - Job ID: %s, Step: %s at %s",
                jobId, stepName, endTime));
        System.out.println(String.format("[BATCH] Step statistics for %s:", stepName));
        System.out.println(String.format("  Duration: %d seconds", duration));
        System.out.println(String.format("  Reads: %d, Writes: %d", reads, writes));
        System.out.println(String.format("  Skips: %d, Filters: %d, Failures: %d", skips, filterCount, fails));
        
        return stepExecution.getExitStatus();
    }
}
