/*
 * Copyright © 2018 Edwin Njeru (mailnjeru@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.mali.fasaha.utils;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * A minimal implementation of Either abstraction similar to the implementation in the Scala API. This is an abstraction that
 * represents a value of one of two possible types (a disjoint union). An instance of {@link Either} is an
 * instance of either Left or Right.
 * <br>
 * A common use of {@link Either} is an alternative to {@link Optional}, or such a circumstance with possibly missing values.
 * Conventionally Left is used when there is failure and Right when successful. The same could also be used whenever we expect failure.
 * <br>
 * For instance lets say you wanted to create a function that you know could return an exception depending on how the client were to
 * apply it. You could return a map containing the answer and the Exception. When successful the exception is null.
 * When there is an exception the result will be null as shown here:
 * <pre>
 * {@code
 *   class DividedByZeroException extends RuntimeException{
 *         DividedByZeroException(final String message) {
 *             super(message);
 *         }
 *     }
 *
 *    Map<String, Object> divide (double numerator, double divisor) {
 *       Map<String, Object> result = new HashMap<>();
 *         if (divisor == 0 && numerator != 0) {
 *             result.put("Exception", new DividedByZeroException("Attempted To Divide By Zero"));
 *             result.put("Division", null);
 *             return result;
 *         } else {
 *             result.put("Exception", null);
 *             result.put("Division", numerator/divisor);
 *             return result;
 *         }
 *     }
 *
 * }
 * </pre>
 * That looks busy, you also miss out on type checking. Instead you could put the results in Either. Exception goes left side when there's failure, result goes right side when the divide function is
 * successful:
 * <pre>
 *     {@code
 *      Either<DividedByZeroException, Double> divide(double numerator, double divisor) {
 *        if (divisor == 0 && numerator != 0) {
 *            return Either.createLeft(new DividedByZeroException(format("Could not divide the number: %s by zero",numerator)));
 *          } else {
 *            return Either.createRight(numerator/divisor);
 *          }
 *       }
 *     }
 * </pre>
 *
 * @author edwin_njeru
 * @version $Id: $Id
 */
public abstract class Either<L, R> {
    private Either() {
    }

    /**
     * Creates a left or right, depending on which element is non-null.  Precisely one element should be non-null.
     *
     * @param l   a L object.
     * @param r   a R object.
     * @param <L> a L object.
     * @param <R> a R object.
     * @return a {@link Either} object.
     */
    public static <L, R> Either<L, R> create(L l, R r) {
        if (l == null && r != null) {
            return createRight(r);
        } else if (l != null && r == null) {
            return createLeft(l);
        } else {
            if (l == null) {
                throw new IllegalArgumentException("Both arguments were null.");
            } else {
                throw new IllegalArgumentException("Both arguments were non-null: " + l + " " + r);
            }
        }
    }

    /**
     * Creates an instance of Left.
     *
     * @param l   a L object.
     * @param <L> a L object.
     * @param <R> a R object.
     * @return a {@link Either} object.
     */
    public static <L, R> Either<L, R> createLeft(L l) {
        return new Left<>(l);
    }

    /**
     * Creates an instance of Right.
     *
     * @param r   a R object.
     * @param <L> a L object.
     * @param <R> a R object.
     * @return a {@link Either} object.
     */
    public static <L, R> Either<L, R> createRight(R r) {
        return new Right<>(r);
    }

    /**
     * True if it's left.
     *
     * @return a boolean.
     */
    public abstract boolean isLeft();

    /**
     * True if it's right.
     *
     * @return a boolean.
     */
    public final boolean isRight() {
        return !isLeft();
    }

    /**
     * Returns the left side. Throws an exception if it's really a Right.
     *
     * @return a L object.
     */
    public abstract L getLeft();

    /**
     * Returns the right side. Throws an exception if it's really a Left.
     *
     * @return a R object.
     */
    public abstract R getRight();

    /**
     * Performs the given action if this is a Left.
     *
     * @param consumer a {@link java.util.function.Consumer} object.
     */
    public final void ifLeft(Consumer<? super L> consumer) {
        if (isLeft()) {
            consumer.accept(getLeft());
        }
    }

    /**
     * Performs the given action if this is a Right.
     *
     * @param consumer a {@link java.util.function.Consumer} object.
     */
    public final void ifRight(Consumer<? super R> consumer) {
        if (isRight()) {
            consumer.accept(getRight());
        }
    }

    /**
     * Returns the left side as an Optional.
     *
     * @return a {@link java.util.Optional} object.
     */
    public final Optional<L> asOptionalLeft() {
        return fold(Optional::of, val -> Optional.<L>empty());
    }

    /**
     * Returns the right side as an Optional.
     *
     * @return a {@link java.util.Optional} object.
     */
    public final Optional<R> asOptionalRight() {
        return fold(val -> Optional.<R>empty(), Optional::of);
    }

    /**
     * Applies either the left or the right function as appropriate.
     *
     * @param left  a {@link java.util.function.Function} object.
     * @param right a {@link java.util.function.Function} object.
     * @param <T>   a T object.
     * @return a T object.
     */
    public final <T> T fold(Function<? super L, ? extends T> left, Function<? super R, ? extends T> right) {
        if (isLeft()) {
            return left.apply(getLeft());
        } else {
            return right.apply(getRight());
        }
    }

    /**
     * Accepts either the left or the right consumer as appropriate.
     *
     * @param left  a {@link java.util.function.Consumer} object.
     * @param right a {@link java.util.function.Consumer} object.
     */
    public final void accept(Consumer<? super L> left, Consumer<? super R> right) {
        if (isLeft()) {
            left.accept(getLeft());
        } else {
            right.accept(getRight());
        }
    }

    /**
     * <p>mapLeft.</p>
     *
     * @param mapper a {@link java.util.function.Function} object.
     * @param <T>    a T object.
     * @return a {@link Either} object.
     */
    @SuppressWarnings("unchecked")
    public final <T> Either<T, R> mapLeft(Function<? super L, ? extends T> mapper) {
        if (isLeft()) {
            return Either.createLeft(mapper.apply(getLeft()));
        } else {
            return (Either<T, R>) this;
        }
    }

    /**
     * <p>mapRight.</p>
     *
     * @param mapper a {@link java.util.function.Function} object.
     * @param <T>    a T object.
     * @return a {@link Either} object.
     */
    @SuppressWarnings("unchecked")
    public final <T> Either<L, T> mapRight(Function<? super R, ? extends T> mapper) {
        if (isLeft()) {
            return (Either<L, T>) this;
        } else {
            return Either.createRight(mapper.apply(getRight()));
        }
    }

    /**
     * Accepts both the left and right consumers, using the default values to set the empty side.
     *
     * @param left         a {@link java.util.function.Consumer} object.
     * @param right        a {@link java.util.function.Consumer} object.
     * @param defaultLeft  a L object.
     * @param defaultRight a R object.
     */
    public final void acceptBoth(Consumer<? super L> left, Consumer<? super R> right, L defaultLeft, R defaultRight) {
        left.accept(isLeft() ? getLeft() : defaultLeft);
        right.accept(isRight() ? getRight() : defaultRight);
    }

    /**
     * Implementation of left.
     */
    private static final class Left<L, R> extends Either<L, R> {
        private final L value;

        private Left(L value) {
            this.value = Objects.requireNonNull(value);
        }

        @Override
        public final boolean isLeft() {
            return true;
        }

        @Override
        public final L getLeft() {
            return value;
        }

        @Override
        public final R getRight() {
            throw new UnsupportedOperationException();
        }

        @Override
        public final boolean equals(Object otherObj) {
            if (otherObj instanceof Left) {
                return Objects.equals(value, ((Left<?, ?>) otherObj).value);
            } else {
                return false;
            }
        }

        @Override
        public final int hashCode() {
            return Objects.hash(Left.class, value);
        }

        @Override
        public final String toString() {
            return "Left[" + value.toString() + "]";
        }
    }

    /**
     * Implementation of right.
     */
    private static final class Right<L, R> extends Either<L, R> {
        private final R value;

        private Right(R value) {
            this.value = Objects.requireNonNull(value);
        }

        @Override
        public final boolean isLeft() {
            return false;
        }

        @Override
        public final L getLeft() {
            throw new UnsupportedOperationException();
        }

        @Override
        public final R getRight() {
            return value;
        }

        @Override
        public final boolean equals(Object otherObj) {
            if (otherObj instanceof Right) {
                return Objects.equals(value, ((Right<?, ?>) otherObj).value);
            } else {
                return false;
            }
        }

        @Override
        public final int hashCode() {
            return Objects.hash(Right.class, value);
        }

        @Override
        public final String toString() {
            return "Right[" + value.toString() + "]";
        }
    }
}
