package me.vladislav.fs.exceptions;

import lombok.Getter;

public class FileNotFoundException extends RuntimeException {

    @Getter
    private final String filename;

    public FileNotFoundException(String filename) {
        super("not found file with name - %s".formatted(filename));
        this.filename = filename;
    }
}
