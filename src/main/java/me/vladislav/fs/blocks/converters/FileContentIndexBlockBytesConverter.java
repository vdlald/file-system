package me.vladislav.fs.blocks.converters;

import me.vladislav.fs.BytesConverter;
import me.vladislav.fs.blocks.FileContentIndexBlock;
import org.springframework.stereotype.Component;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

@Component
public class FileContentIndexBlockBytesConverter implements BytesConverter<FileContentIndexBlock> {

    @Override
    public FileContentIndexBlock from(ByteBuffer src) {
        FileContentIndexBlock block = new FileContentIndexBlock();

        block.setNextIndexBlock(src.slice(0, FileContentIndexBlock.NEXT_INDEX_BLOCK_SIZE).asIntBuffer().get());

        IntBuffer blockPointersBuffer = src.slice(FileContentIndexBlock.NEXT_INDEX_BLOCK_SIZE, FileContentIndexBlock.BLOCK_POINTERS_SIZE).asIntBuffer();
        while (blockPointersBuffer.hasRemaining()) {
            int pointer = blockPointersBuffer.get();
            if (pointer == 0) {
                continue;
            }
            block.addBlockPointer(pointer);
        }

        return block;
    }

    @Override
    public ByteBuffer toByteBuffer(FileContentIndexBlock object) {
        ByteBuffer blockPointersBuffer = ByteBuffer.allocate(FileContentIndexBlock.BLOCK_POINTERS_SIZE);
        for (int blockPointer : object.getBlockPointers()) {
            blockPointersBuffer.putInt(blockPointer);
        }

        return ByteBuffer.allocate(FileContentIndexBlock.TOTAL_SIZE)
                .put(ByteBuffer.allocate(FileContentIndexBlock.NEXT_INDEX_BLOCK_SIZE)
                        .putInt(object.getNextIndexBlock())
                        .rewind())
                .put(blockPointersBuffer.rewind())
                .rewind();
    }
}
