package me.vladislav.fs.operations;

import jakarta.annotation.Nonnull;
import me.vladislav.fs.blocks.view.FileDescription;

import java.util.List;

/**
 * Represents the basic operations available in the file system
 */
public interface ExtendedFileSystemOperations extends CrudFileSystemOperations {

    /**
     * Returns a list of all files in file system
     */
    List<FileDescription> listFiles();

    /**
     * Move file
     *
     * @param filename    original file name
     * @param newFilename new file name
     */
    void moveFile(String filename, String newFilename);

    String checksum(@Nonnull String filename);
}
