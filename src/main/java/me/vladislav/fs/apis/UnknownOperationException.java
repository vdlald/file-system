package me.vladislav.fs.apis;

public class UnknownOperationException extends RuntimeException {
    public UnknownOperationException(String operationName) {
        super("Unknown operation \"%s\"".formatted(operationName));
    }
}
