package me.vladislav.fs.apis;

import jakarta.annotation.Nonnull;
import lombok.Builder;
import lombok.Getter;
import lombok.With;
import me.vladislav.fs.BlockSize;

import java.nio.file.Path;
import java.util.List;

public interface ApplicationApi extends CrudApplicationApi {

    void createFileSystem(@Nonnull CreateFileSystemRequest request);

    List<String> listFiles(@Nonnull ListFilesRequest request);

    @Getter
    @Builder
    class ListFilesRequest {

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
}
