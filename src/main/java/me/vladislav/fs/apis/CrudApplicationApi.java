package me.vladislav.fs.apis;

import jakarta.annotation.Nonnull;
import lombok.Builder;
import lombok.Getter;
import lombok.With;

import java.nio.channels.SeekableByteChannel;
import java.nio.file.Path;

public interface CrudApplicationApi {
    void createFile(@Nonnull CreateFileRequest request);

    SeekableByteChannel readFile(@Nonnull ReadFileRequest request);

    void updateFile(@Nonnull UpdateFileRequest request);

    void deleteFile(@Nonnull DeleteFileRequest request);

    @Getter
    @Builder(toBuilder = true)
    class UpdateFileRequest {

        /**
         * name of file to update
         */
        @Nonnull
        private final String filename;

        /**
         * content of file to update
         */
        @Nonnull
        private final SeekableByteChannel content;

        /**
         * where the file system is stored
         */
        @Nonnull
        private final Path fsPath;
    }

    @Getter
    @Builder
    class ReadFileRequest {

        // name of file in fs
        @Nonnull
        private final String filename;

        // where the file system is stored
        @Nonnull
        private final Path fsPath;

    }

    // todo: можно менять контент из вне
    @With
    @Getter
    @Builder(toBuilder = true)
    class CreateFileRequest {

        /**
         * name of file to create
         */
        @Nonnull
        private final String filename;

        /**
         * content of file to create
         */
        @Nonnull
        private final SeekableByteChannel content;

        /**
         * where the file system is stored
         */
        @Nonnull
        private final Path fsPath;

    }

    @Getter
    @Builder
    class DeleteFileRequest {

        /**
         * name of file in fs
         */
        @Nonnull
        private final String filename;

        /**
         * where the file system is stored
         */
        @Nonnull
        private final Path fsPath;

    }
}
