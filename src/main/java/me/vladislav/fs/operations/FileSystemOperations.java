package me.vladislav.fs.operations;

import jakarta.annotation.Nonnull;
import me.vladislav.fs.requests.CreateFileRequest;
import me.vladislav.fs.requests.UpdateFileRequest;

import java.io.IOException;
import java.nio.channels.SeekableByteChannel;

/**
 * Represents the basic operations available in the file system
 */
public interface FileSystemOperations {

    /**
     * Creates a file from a request {@link CreateFileRequest}
     * If a file with this name already exists, it gives an error {@link java.nio.file.FileAlreadyExistsException}
     */
    void createFile(@Nonnull CreateFileRequest createFileRequest) throws IOException;

    /**
     * Reads the file
     * If the file is not found, it gives an error {@link java.io.FileNotFoundException}
     *
     * @return File content channel
     */
    @Nonnull
    SeekableByteChannel readFile(@Nonnull String filename) throws IOException;

    /**
     * Updates the file
     * If the new file size is smaller - frees unoccupied space
     * If the new file size is larger - takes up more space
     * If the file is not found, it gives an error {@link java.io.FileNotFoundException}
     */
    void updateFile(@Nonnull UpdateFileRequest updateFileRequest) throws IOException;

    /**
     * Deletes the file
     * Frees the space occupied by the file and its method
     */
    void deleteFile(@Nonnull String filename) throws IOException;
}
