package com.migration.batch.listener;

import org.springframework.batch.core.listener.ChunkListener;
import org.springframework.batch.core.scope.StepScope;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

/**
 * Listener to log chunk-level processing statistics
 * Implements ChunkListener to track read/write counts
 */
@Slf4j
@Component
@StepScope
public class ChunkLoggingListener implements ChunkListener {

    private long readCount = 0;
    private long writeCount = 0;

    @Override
    public void beforeChunk() {
        // No action needed before chunk processing
    }

    @Override
    public void afterChunk() {
        // No action needed after chunk processing
    }

    @Override
    public void afterChunkError(Exception exception) {
        // Log errors that occur during chunk processing
        log.error("Error occurred during chunk processing: {}", exception.getMessage(), exception);
    }

    /**
     * Called after a chunk is read - increments read counter
     */
    public void afterRead() {
        readCount++;
    }

    /**
     * Called after items are written - increments write counter
     * 
     * @param count Number of items written
     */
    public void afterWrite(int count) {
        writeCount += count;
        log.info("Chunk processed - Read: {}, Written: {} items", readCount, writeCount);
    }

    /**
     * Get total items read
     * 
     * @return read count
     */
    public long getReadCount() {
        return readCount;
    }

    /**
     * Get total items written
     * 
     * @return write count
     */
    public long getWriteCount() {
        return writeCount;
    }
}
