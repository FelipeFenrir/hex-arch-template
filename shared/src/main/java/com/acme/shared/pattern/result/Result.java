package com.acme.shared.pattern.result;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public sealed interface Result<V, E> permits Result.Success, Result.Failure {

    record Success<V, E>(V value) implements Result<V, E> {}
    record Failure<V, E>(E error) implements Result<V, E> {}

    static <V, E> Result<V, E> success(V value) { return new Success<>(value); }
    static <V, E> Result<V, E> failure(E error) { return new Failure<>(error); }

    default boolean isSuccess() {
        return this instanceof Success<V, E>;
    }

    default boolean isFailure() {
        return this instanceof Failure<V, E>;
    }

    default <T> T fold(Function<V, T> onSuccess, Function<E, T> onFailure) {
        return this instanceof Success<V, E>(V value)
                ? onSuccess.apply(value)
                : onFailure.apply(((Failure<V, E>) this).error());
    }

    default Result<V, E> onSuccess(Consumer<V> consumer) {
        if (this instanceof Success<V, E>(V value)) {
            consumer.accept(value);
        }
        return this;
    }

    default Result<V, E> onFailure(Consumer<E> consumer) {
        if (this instanceof Failure<V, E>(E error)) {
            consumer.accept(error);
        }
        return this;
    }

    default V getOrElseThrow(Function<E, ? extends RuntimeException> exceptionMapper) {
        return fold(
                Function.identity(),
                error -> {
                    throw exceptionMapper.apply(error);
                }
        );
    }

    default E errorOrElseThrow(Supplier<? extends RuntimeException> exceptionSupplier) {
        return fold(
                value -> {
                    throw exceptionSupplier.get();
                },
                Function.identity()
        );
    }

    // Railway Oriented: Encaminha o sucesso para a próxima função
    default <T> Result<T, E> flatMap(Function<V, Result<T, E>> mapper) {
        return this instanceof Success<V, E>(V value) ? mapper.apply(value) : (Result<T, E>) this;
    }

    // Transforma o valor interno em caso de sucesso
    default <T> Result<T, E> map(Function<V, T> mapper) {
        return this instanceof Success<V, E>(V value) ? Result.success(mapper.apply(value)) : (Result<T, E>) this;
    }
}