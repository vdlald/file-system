package me.vladislav.fs.util;

public class Utils {

    public static <T> T avoidException(Supplier<T> function) {
        try {
            return function.produce();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @FunctionalInterface
    public interface Supplier<T> {
        T produce() throws Exception;
    }
}
