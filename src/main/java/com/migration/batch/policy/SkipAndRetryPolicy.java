package com.migration.batch.policy;

import org.springframework.batch.core.retry.RetryPolicy;
import org.springframework.batch.core.retry.RetryContext;
import org.springframework.batch.core.scope.SkipProxy;
import org.springframework.batch.core.step.item.SkipLimitExceededException;
import org.springframework.batch.core.step.item.SkipPolicy;
import org.springframework.classify.BinaryExceptionClassifier;
import org.springframework.util.ClassUtils;

import java.util.Collections;
import java.util.Map;

/**
 * SkipPolicy implementation with retry configuration for handling transient and non-transient exceptions
 * in the data pipeline. This policy configures skip limits for specific exception types and provides
 * a retry mechanism for transient errors.
 */
@SkipProxy
public class SkipAndRetryPolicy implements SkipPolicy, RetryPolicy {

    // Maximum number of retries for transient exceptions
    private final int maxRetryCount = 3;
    
    // Map of non-retryable exceptions (fatal errors that should be skipped rather than retried)
    private final Map<Class<? extends Exception>, Boolean> nonRetryableExceptions = Collections.emptyMap();
    
    // Map of retryable exceptions (transient errors that should be retried)
    private final Map<Class<? extends Exception>, Boolean> retryableExceptions = Collections.emptyMap();

    /**
     * Determines whether an exception should be skipped or not.
     * 
     * @param exception The exception to evaluate
     * @param skipCount The current skip count
     * @return true if the exception should be skipped, false otherwise
     * @throws SkipLimitExceededException if the maximum skip count is exceeded
     */
    @Override
    public boolean shouldSkip(Throwable exception, int skipCount) throws SkipLimitExceededException {
        // Check if we've reached the maximum allowed skips for this exception type
        if (skipCount >= getMaxSkipCount(exception)) {
            throw new SkipLimitExceededException("Skip limit exceeded for " + exception.getClass().getName());
        }
        
        // Determine if this exception type should be skipped
        return isSkipable(exception);
    }

    /**
     * Gets the maximum skip count allowed for a specific exception type.
     * 
     * @param exception The exception to evaluate
     * @return The maximum skip count
     */
    private int getMaxSkipCount(Throwable exception) {
        // Default to unlimited skips for non-fatal exceptions
        return Integer.MAX_VALUE;
    }

    /**
     * Determines if an exception should be skipped based on its type.
     * 
     * @param exception The exception to evaluate
     * @return true if the exception should be skipped, false otherwise
     */
    private boolean isSkipable(Throwable exception) {
        Class<?> clazz = exception.getClass();
        
        // Check if this is a non-retryable exception (fatal error)
        if (nonRetryableExceptions.containsKey(clazz)) {
            return true;
        }
        
        // Check if this is a retryable exception (transient error)
        if (retryableExceptions.containsKey(clazz)) {
            return false; // Don't skip retryable exceptions - they'll be retried
        }
        
        // For all other exceptions, decide based on whether they're runtime exceptions
        return !(exception instanceof Error || ClassUtils.isAssignable(Throwable.class, exception.getClass()));
    }

    /**
     * {@inheritDoc}
     * Determines whether an exception can be retried based on its type and retry count.
     */
    @Override
    public boolean canRetry(RetryContext context) {
        // Get the exception from the retry context
        Throwable exception = context.getLastThrowable();
        
        // Check if we've exceeded the maximum retry count
        if (context.getRetryCount() >= maxRetryCount) {
            return false;
        }
        
        // Check if this exception type is retryable
        Class<?> exceptionClass = exception.getClass();
        if (retryableExceptions.containsKey(exceptionClass)) {
            return true;
        }
        
        // For all other exceptions, decide based on whether they're runtime exceptions
        return !(exception instanceof Error || ClassUtils.isAssignable(Throwable.class, exception.getClass()));
    }

    /**
     * {@inheritDoc}
     * Registers the callback to be called when retry is giving up.
     */
    @Override
    public void registerRetryForException(RetryContext context, Throwable exception) {
        // No special action needed - just register that we're attempting a retry
    }

    /**
     * {@inheritDoc}
     * Returns the retry count multiplier.
     */
    @Override
    public boolean retry(RetryContext context) {
        // Get the exception from the retry context
        Throwable exception = context.getLastThrowable();
        
        // Check if we've exceeded the maximum retry count
        if (context.getRetryCount() >= maxRetryCount) {
            return false;
        }
        
        // Check if this exception type is retryable
        Class<?> exceptionClass = exception.getClass();
        if (retryableExceptions.containsKey(exceptionClass)) {
            return true;
        }
        
        // For all other exceptions, decide based on whether they're runtime exceptions
        return !(exception instanceof Error || ClassUtils.isAssignable(Throwable.class, exception.getClass()));
    }

    /**
     * {@inheritDoc}
     * Returns the exception classifier.
     */
    @Override
    public RetryPolicy retryForException(Class<? extends Throwable> exceptionClass) {
        // This is a simplified implementation - in a real scenario, this would
        // return a policy configured to retry for the specific exception class
        return this;
    }

    /**
     * {@inheritDoc}
     * Returns the exception classifier.
     */
    @Override
    public BinaryExceptionClassifier getExceptionClassifier() {
        // Return a classifier that determines which exceptions are retryable vs. non-retryable
        return new BinaryExceptionClassifier(nonRetryableExceptions, true);
    }

    /**
     * Sets the exception classifier.
     * 
     * @param classifier The exception classifier to use
     */
    @Override
    public void setExceptionClassifier(BinaryExceptionClassifier classifier) {
        // This is a simplified implementation - in a real scenario, this would
        // update the classifier with the provided one
    }
}
