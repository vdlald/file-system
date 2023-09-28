package me.vladislav.fs.exceptions;

import lombok.Getter;

public class FileAlreadyExistsException extends RuntimeException {

    @Getter
    private final String filename;

    public FileAlreadyExistsException(String filename) {
        super("already exists file with name - %s".formatted(filename));
        this.filename = filename;
    }
}
