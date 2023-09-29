package me.vladislav.fs.apis;

import org.springframework.boot.ApplicationRunner;

public interface ArgumentsApi extends ApplicationRunner, ApplicationApi {
    String HELP = "help";
    String OPERATION_CREATE_FS = "create-file-system";
    String OPERATION_CREATE_FILE = "create-file";
    String OPERATION_READ_FILE = "read-file";
    String OPERATION_UPDATE_FILE = "update-file";
    String OPERATION_DELETE_FILE = "delete-file";
    String OPERATION_LIST_FILES = "list-files";

    class ArgumentsApiException extends RuntimeException {
        public ArgumentsApiException(Throwable cause) {
            super(cause);
        }
    }
}
