package me.vladislav.fs.apis.requests;

import lombok.Builder;
import lombok.Getter;

import java.nio.file.Path;

@Getter
@Builder
public class ListFilesRequest {

    private final Path fsPath;
}
