package com.migration.batch.tasklet;

import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.StepScope;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.FileSystemUtils;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

/**
 * Tasklet responsible for file management operations such as moving files,
 * validating file existence, and performing cleanup operations as part of the ETL pipeline.
 * This component ensures that source files are properly handled before and after processing.
 */
@Component
@StepScope
public class FileTasklet implements Tasklet {

    @Value("${input.file.path}")
    private String inputFilePath;

    @Value("${archive.file.path}")
    private String archiveFilePath;

    /**
     * Executes file management operations.
     * Validates input file existence, moves processed files to archive,
     * and performs any necessary pre-processing cleanup.
     *
     * @param contribution Step contribution containing execution context
     * @param repeatStatus Repeat status for controlling iteration
     * @return RepeatStatus to indicate whether to continue processing
     * @throws Exception if any file operation fails
     */
    @Override
    public RepeatStatus execute(StepContribution contribution, RepeatStatus repeatStatus) throws Exception {
        // Validate input file path
        if (inputFilePath == null || inputFilePath.trim().isEmpty()) {
            throw new IllegalArgumentException("Input file path cannot be null or empty");
        }

        Path sourcePath = Paths.get(inputFilePath);
        
        // Check if input file exists
        if (!Files.exists(sourcePath)) {
            throw new IllegalStateException("Input file not found at path: " + inputFilePath);
        }

        // Create archive directory if it doesn't exist
        Path archivePath = Paths.get(archiveFilePath);
        if (!Files.exists(archivePath)) {
            Files.createDirectories(archivePath);
        }

        // Move file to archive
        Path targetPath = archivePath.resolve(sourcePath.getFileName());
        Files.move(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);

        // Log successful file movement
        contribution.getStepExecution().getJobExecution().getLogger()
                .info("Successfully moved input file {} to archive location {}", inputFilePath, archiveFilePath);

        return RepeatStatus.FINISHED;
    }
}
