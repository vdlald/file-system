package me.vladislav.fs.fam;

import lombok.Builder;
import me.vladislav.fs.BlockAllocatedSpace;
import me.vladislav.fs.requests.CreateFileRequest;

import java.nio.ByteBuffer;

@Builder(toBuilder = true)
public class MyFileAllocationMethod {

    private final BlockAllocatedSpace allocatedSpace;

    public void createFile(CreateFileRequest createFileRequest) {

    }

    public ByteBuffer readFile(String fileName) {
        return null;
    }
}
