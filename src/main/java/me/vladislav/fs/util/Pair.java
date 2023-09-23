package me.vladislav.fs.util;

import lombok.AllArgsConstructor;
import lombok.Getter;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;

@Getter
@AllArgsConstructor
public class Pair<F, S> {

    @Nullable
    private final F first;

    @Nullable
    private final S second;

    public static <F, S> Pair<F, S> of(F first, S second) {
        return new Pair<>(first, second);
    }

    @Nonnull
    public F first() {
        return Objects.requireNonNull(first);
    }

    @Nonnull
    public S second() {
        return Objects.requireNonNull(second);
    }
}
