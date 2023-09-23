package me.vladislav.fs.blocks.serializers;

import me.vladislav.fs.blocks.FileContentBlock;
import org.springframework.stereotype.Component;

import java.nio.ByteBuffer;

@Component
public class FileContentBytesSerializer implements BytesSerializer<FileContentBlock> {

    @Override
    public FileContentBlock from(ByteBuffer src) {
        return new FileContentBlock(src.duplicate());
    }

    @Override
    public ByteBuffer toByteBuffer(FileContentBlock object) {
        return object.getData().duplicate();
    }
}
