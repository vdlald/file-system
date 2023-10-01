package me.vladislav.fs.blocks.view;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@Builder
@ToString
public class FileDescription {

    private final String filename;
    private final long fileSize;
}
