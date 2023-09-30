package me.vladislav.fs.apis;

import jakarta.annotation.Nonnull;
import lombok.Builder;
import lombok.Getter;
import lombok.With;
import me.vladislav.fs.BlockSize;

import java.nio.file.Path;
import java.util.List;

public interface ApplicationApi extends CrudApplicationApi {

    /**
     * Create new file system
     */
    void createFileSystem(@Nonnull CreateFileSystemRequest request);

    /**
     * List files in file system
     */
    List<String> listFiles(@Nonnull ListFilesRequest request);

    /**
     * Right now it works just as a file renaming, but in the future here will be a complete move from one location
     * to another
     */
    void moveFile(@Nonnull MoveFileRequest request);

    String md5ChecksumFile(@Nonnull Md5ChecksumFileRequest request);

    @Getter
    @Builder
    class ListFilesRequest {

        /**
         * Path to the file system
         */
        @Nonnull
        private final Path fsPath;
    }

    @With
    @Getter
    @Builder(toBuilder = true)
    class CreateFileSystemRequest {

        /**
         * Where to create a file system
         */
        @Nonnull
        private final Path whereToStore;

        /**
         * How much space to allocate to the file system beforehand
         */
        @Builder.Default
        private final int initialSizeInBytes = BlockSize.KB_4.getBlockSizeInBytes();

        /**
         * Size of one block in the file system
         */
        @Builder.Default
        private final BlockSize blockSize = BlockSize.KB_4;
    }

    @Getter
    @Builder
    class MoveFileRequest {

        /**
         * Path to the file system
         */
        @Nonnull
        private final Path fsPath;

        /**
         * Original filename
         */
        @Nonnull
        private final String filename;

        /**
         * New filename
         */
        @Nonnull
        private final String newFilename;
    }

    @Getter
    @Builder
    class Md5ChecksumFileRequest {

        /**
         * Path to the file system
         */
        @Nonnull
        private final Path fsPath;

        /**
         * Original filename
         */
        @Nonnull
        private final String filename;

    }
}
