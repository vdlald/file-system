package me.vladislav.fs.apis.arguments;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;

@Slf4j
@Aspect
@Component
public class ArgumentsApiExceptionHandling {

    protected static final String FILE_ST_NAME = "error.stacktrace";

    @Pointcut("execution(public * me.vladislav.fs.apis.ArgumentsApi.*(..))")
    private void argumentsApiPointCut() {
    }

    @Around(value = "argumentsApiPointCut()")
    public Object handleException(ProceedingJoinPoint joinPoint) throws IOException {
        try {
            return joinPoint.proceed();
        } catch (Throwable t) {
            System.err.println(t.getMessage());

            FileOutputStream errorFile = new FileOutputStream(FILE_ST_NAME, true);
            PrintStream errorStream = new PrintStream(errorFile);

            t.printStackTrace(errorStream);

            errorStream.close();
            errorFile.close();
            return null;
        }
    }
}
