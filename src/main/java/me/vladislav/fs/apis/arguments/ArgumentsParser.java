package me.vladislav.fs.apis.arguments;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import lombok.AllArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static java.nio.file.StandardOpenOption.*;

@Component
@Scope("prototype")
@AllArgsConstructor
public class ArgumentsParser {

    public static final String ARG_OPERATION = "operation";
    public static final String ARG_FILESYSTEM = "fs";
    public static final String ARG_INIT_SIZE = "init-size";
    public static final String ARG_FILENAME = "filename";
    public static final String ARG_FILE_IN = "file_in";
    public static final String ARG_FILE_OUT = "file_out";

    private final ApplicationArguments args;

    @Nullable
    public String operation() {
        if (!args.containsOption(ARG_OPERATION)) {
            return null;
        }
        return singleRequiredArgument(ARG_OPERATION);
    }

    public Path fileSystemPath() {
        return Path.of(singleRequiredArgument(ARG_FILESYSTEM));
    }

    @Nullable
    public Integer fsInitSize() {
        String raw = singleNotRequiredArgument(ARG_INIT_SIZE);
        if (raw == null) {
            return null;
        }
        return Integer.parseInt(raw);
    }

    public String filename() {
        return singleRequiredArgument(ARG_FILENAME);
    }

    public SeekableByteChannel fileIn() {
        try {
            return Files.newByteChannel(Path.of(singleRequiredArgument(ARG_FILE_IN)), READ);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Nullable
    public SeekableByteChannel fileOut() {
        String out = singleNotRequiredArgument(ARG_FILE_OUT);
        if (out == null) {
            return null;
        }

        try {
            return Files.newByteChannel(Path.of(out), CREATE, WRITE);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Nullable
    private String singleNotRequiredArgument(String argName) {
        List<String> values = args.getOptionValues(argName);
        if (values == null) {
            return null;
        }

        if (values.size() > 1) {
            throw new RuntimeException(incorrectArgument(argName, "it's not list argument"));
        }

        return values.get(0);
    }

    @Nonnull
    private String singleRequiredArgument(String argName) {
        List<String> values = Objects.requireNonNullElse(args.getOptionValues(argName), Collections.emptyList());
        if (values.isEmpty()) {
            throw new RuntimeException(incorrectArgument(argName, "it's empty"));
        }
        if (values.size() > 1) {
            throw new RuntimeException(incorrectArgument(argName, "it's not list argument"));
        }

        return values.get(0);
    }

    private String incorrectArgument(String argument, String reason) {
        return "argument %s incorrect because %s".formatted(argument, reason);
    }
}
