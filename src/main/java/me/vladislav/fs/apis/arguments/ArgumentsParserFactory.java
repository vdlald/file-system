package me.vladislav.fs.apis.arguments;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.ApplicationArguments;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ArgumentsParserFactory {

    private final ObjectProvider<ArgumentsParser> objectProvider;

    public ArgumentsParser create(ApplicationArguments args) {
        return objectProvider.getObject(args);
    }
}
