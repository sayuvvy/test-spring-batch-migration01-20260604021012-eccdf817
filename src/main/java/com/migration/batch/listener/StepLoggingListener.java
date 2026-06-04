package com.migration.batch.listener;

import org.springframework.batch.core.listener.StepExecutionListener;
import org.springframework.batch.core.step.StepExecution;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

/**
 * Listener to log detailed step metrics during batch processing.
 * Implements StepExecutionListener to capture and log step execution details.
 */
@Slf4j
@Component
public class StepLoggingListener implements StepExecutionListener {

    /**
     * Callback method before step execution begins.
     * Logs the start of step processing.
     * 
     * @param stepExecution The current step execution context
     */
    @Override
    public void beforeStep(StepExecution stepExecution) {
        log.info("Starting step: {}", stepExecution.getStepName());
    }

    /**
     * Callback method after step execution completes.
     * Logs comprehensive metrics about the step execution including:
     * - Step name
     * - Start and end times
     * - Commit and rollback counts
     * - Skip counts (read, write, process)
     * - Filter count
     * - Exit status
     * 
     * @param stepExecution The completed step execution context
     */
    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {
        log.info("Completed step: {} | Start: {} | End: {} | Commits: {} | Rollbacks: {} | " +
                 "Reads: {} | Writes: {} | Skips: {} | Filter: {} | Status: {}",
            stepExecution.getStepName(),
            stepExecution.getStartTime(),
            stepExecution.getEndTime(),
            stepExecution.getCommitCount(),
            stepExecution.getRollbackCount(),
            stepExecution.getReadCount(),
            stepExecution.getWriteCount(),
            stepExecution.getSkipCount(),
            stepExecution.getFilterCount(),
            stepExecution.getStatus());

        return stepExecution.getExitStatus();
    }
}
