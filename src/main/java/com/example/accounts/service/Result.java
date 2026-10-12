package com.example.accounts.service;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

public sealed interface Result<E, T> {
    record Ok<E, T>(T value) implements Result<E, T> {}
    record Err<E, T>(E error) implements Result<E, T> {}

    static <E, T> Result<E, T> ok(T value) {
        return new Ok<>(value);
    }

    static <E, T> Result<E, T> err(E error) {
        return new Err<>(error);
    }

    static <E, T> Result<E, T> fromOptional(Optional<T> opt, Supplier<E> onEmpty) {
        return opt.<Result<E, T>>map(Result::ok).orElseGet(() -> err(onEmpty.get()));
    }

    default <U> Result<E, U> map(Function<? super T, ? extends U> f) {
        return switch (this) {
            case Ok<E, T>(var v)  -> ok(f.apply(v));
            case Err<E, T>(var e) -> err(e);
        };
    }

    default <U> Result<E, U> flatMap(Function<? super T, ? extends Result<E, U>> f) {
        return switch (this) {
            case Ok<E, T>(var v)  -> f.apply(v);
            case Err<E, T>(var e) -> err(e);
        };
    }
}
