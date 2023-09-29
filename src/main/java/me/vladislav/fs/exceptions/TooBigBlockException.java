package me.vladislav.fs.exceptions;

public class TooBigBlockException extends RuntimeException {
    public TooBigBlockException(int expectedBlockSize, int actualBlockSize) {
        super("Block size %s when expected %s".formatted(actualBlockSize, expectedBlockSize));
    }
}
