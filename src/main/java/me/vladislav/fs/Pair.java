package me.vladislav.fs;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class Pair<F, S> {
    private final F first;
    private final S second;

    public static <F, S> Pair<F, S> of(F first, S second) {
        return new Pair<>(first, second);
    }
}
