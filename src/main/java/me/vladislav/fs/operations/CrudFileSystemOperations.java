package me.vladislav.fs.operations;

import jakarta.annotation.Nonnull;
import me.vladislav.fs.exceptions.FileAlreadyExistsException;
import me.vladislav.fs.exceptions.FileNotFoundException;
import me.vladislav.fs.requests.CreateFileRequest;
import me.vladislav.fs.requests.UpdateFileRequest;

import java.nio.channels.SeekableByteChannel;

public interface CrudFileSystemOperations {
    /**
     * Creates a file from a request {@link CreateFileRequest}
     * If a file with this name already exists, it gives an error {@link FileAlreadyExistsException}
     */
    void createFile(@Nonnull CreateFileRequest createFileRequest);

    /**
     * Reads the file
     * If the file is not found, it gives an error {@link FileNotFoundException}
     *
     * @return File content channel
     */
    @Nonnull
    SeekableByteChannel readFile(@Nonnull String filename);

    /**
     * Updates the file
     * If the new file size is smaller - frees unoccupied space
     * If the new file size is larger - takes up more space
     * If the file is not found, it gives an error {@link FileNotFoundException}
     */
    void updateFile(@Nonnull UpdateFileRequest updateFileRequest);

    /**
     * Deletes the file
     * Frees the space occupied by the file and its method
     * If the file is not found, it gives an error {@link FileNotFoundException}
     */
    void deleteFile(@Nonnull String filename);
}
