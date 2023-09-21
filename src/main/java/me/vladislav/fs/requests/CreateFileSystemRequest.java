package me.vladislav.fs.requests;

import lombok.Builder;
import lombok.Getter;

import java.nio.file.Path;

@Getter
@Builder(toBuilder = true)
public class CreateFileSystemRequest {

    private final Path whereToStore;
    private final String fileSystemName;
    private final FileAllocationMethodType fileAllocationMethod;
    private final int initialSizeInBytes;

    public enum FileAllocationMethodType {
        INDEX_ALLOCATION
    }
}
