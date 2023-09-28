package me.vladislav.fs.util;

import me.vladislav.fs.AllocatedSpace;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.commons.compress.utils.SeekableInMemoryByteChannel;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

public class ResourceUtils {

    public static String getResourceAsString(String resourceName) {
        InputStream input = Objects.requireNonNull(ResourceUtils.class.getResourceAsStream(resourceName));
        try {
            SeekableInMemoryByteChannel channel = new SeekableInMemoryByteChannel(IOUtils.toByteArray(input));
            try (AllocatedSpace allocatedSpace = AllocatedSpace.builder()
                    .data(channel)
                    .build()) {
                return ByteBufferUtils.readToString(allocatedSpace.read((int) channel.size()));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
