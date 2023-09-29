package me.vladislav.fs.exceptions;

import java.nio.file.Path;

public class IsNotFileException extends RuntimeException {
    public IsNotFileException(Path path) {
        super("It's not a file in the path: %s".formatted(path));
    }
}
