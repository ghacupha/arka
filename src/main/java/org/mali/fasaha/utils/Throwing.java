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
package org.mali.fasaha.utils;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.IntConsumer;

/**
 * Variations on the standard functional interfaces which throw Throwable.
 * <p>
 * {@link org.mali.fasaha.utils.Errors} can convert these into standard functional interfaces.
 *
 * @author edwin_njeru
 * @version $Id: $Id
 */
public interface Throwing {
    /**
     * Variations on the standard functional interfaces which throw a specific subclass of Throwable.
     */
    public interface Specific {
        @FunctionalInterface
        public interface Runnable<E extends Throwable> {
            void run() throws E;
        }

        @FunctionalInterface
        public interface Supplier<T, E extends Throwable> {
            T get() throws E;
        }

        /**
         * Represents an operation that accepts a single input argument and returns no
         * result. Unlike most other functional interfaces, {@code Consumer} is expected
         * to operate via side-effects.
         *
         * @param <T> the type of the input to the operation
         * @param <E> Throwable created when the {@link #accept(Object)} methods leads to an exception
         */
        @FunctionalInterface
        public interface Consumer<T, E extends Throwable> {

            /**
             * Performs this operation on the given argument.
             * @param t the input argument
             * @throws E Type of a ThrowableException thrown by the argument
             */
            void accept(T t) throws E;

            /**
             * Returns a composed {@code Consumer} that performs, in sequence, this
             * operation followed by the {@code after} operation. If performing either
             * operation throws an exception, it is relayed to the caller of the
             * composed operation.  If performing this operation throws an exception,
             * the {@code after} operation will not be performed.
             *
             * @param after the operation to perform after this operation
             * @return a composed {@code Consumer} that performs in sequence this
             * operation followed by the {@code after} operation
             * @throws NullPointerException if {@code after} is null
             * @throws E Type of a ThrowableException thrown by executing the argument
             */
            default Consumer<T, E> andThen(Consumer<? super T, ? super E> after) throws E{
                Objects.requireNonNull(after);

                Consumer<T, E> consumer;

                try{
                    consumer = (T t) -> {accept(t);
                        try {
                            after.accept(t);
                        } catch (Throwable throwable) {
                            throw new RuntimeException(throwable);
                        }
                    };
                } catch (Throwable e){
                    throw new RuntimeException(e);
                }

                return consumer;
            }
        }

        /**
         * Represents an operation that accepts a single {@code int}-valued argument and
         * returns no result.  This is the primitive type specialization of
         * {@link java.util.function.Consumer} for {@code int}.  Unlike most other functional interfaces,
         * {@code IntConsumer} is expected to operate via side-effects.
         * It's typical like any other consumer except that it operates with native un-boxed integers
         * and can be typed by {@link Throwable}
         *
         * <p>This is a <a href="package-summary.html">functional interface</a>
         * whose functional method is {@link #accept(int)}.
         *
         * @see java.util.function.Consumer
         * @since 1.8
         */
        @FunctionalInterface
        public interface IntConsumer<E extends Throwable> {

            /**
             * Performs this operation on the given argument.
             * @param t value the input argument
             * @throws E throwable exception
             */
            void accept(int t) throws E;

            /**
             * Returns a composed {@code IntConsumer} that performs, in sequence, this
             * operation followed by the {@code after} operation. If performing either
             * operation throws an exception, it is relayed to the caller of the
             * composed operation.  If performing this operation throws an exception,
             * the {@code after} operation will not be performed.
             *
             * @param after the operation to perform after this operation
             * @return a composed {@code IntConsumer} that performs in sequence this
             * @throws NullPointerException if {@code after} is null
             * @throws E RuntimeException thrown if an exceptional event occurs
             */
            default IntConsumer andThen(IntConsumer after) throws E {

                Objects.requireNonNull(after, "The intConsumer provided cannot be null");

                IntConsumer<E> intConsumer;

                try{
                    intConsumer = (int t) -> { accept(t);
                        try {
                            after.accept(t);
                        } catch (Throwable throwable) {
                            throw new RuntimeException(throwable);
                        }
                    };
                } catch (Throwable e){
                    throw new RuntimeException(e);
                }

                return intConsumer;
            }
        }

        @FunctionalInterface
        public interface Function<T, R, E extends Throwable> {
            R apply(T t) throws E;
        }

        @FunctionalInterface
        public interface Predicate<T, E extends Throwable> {
            boolean test(T t) throws E;
        }

        @FunctionalInterface
        public interface BiConsumer<T, U, E extends Throwable> {
            void accept(T t, U u) throws E;
        }

        @FunctionalInterface
        public interface BiIntConsumer<T, U, E extends Throwable> {
            void accept(T t, U u) throws E;
        }

        @FunctionalInterface
        public interface BiFunction<T, U, R, E extends Throwable> {
            R apply(T t, U u) throws E;
        }

        @FunctionalInterface
        public interface BiPredicate<T, U, E extends Throwable> {
            boolean accept(T t, U u) throws E;
        }
    }

    @FunctionalInterface
    public interface Runnable extends Specific.Runnable<Throwable> {
    }

    @FunctionalInterface
    public interface Supplier<T> extends Specific.Supplier<T, Throwable> {
    }

    @FunctionalInterface
    public interface Consumer<T> extends Specific.Consumer<T, Throwable> {
    }

    @FunctionalInterface
    public interface Function<T, R> extends Specific.Function<T, R, Throwable> {
    }

    @FunctionalInterface
    public interface Predicate<T> extends Specific.Predicate<T, Throwable> {
    }

    @FunctionalInterface
    public interface BiConsumer<T, U> extends Specific.BiConsumer<T, U, Throwable> {
    }

    @FunctionalInterface
    public interface BiFunction<T, U, R> extends Specific.BiFunction<T, U, R, Throwable> {
    }

    @FunctionalInterface
    public interface BiPredicate<T, U> extends Specific.BiPredicate<T, U, Throwable> {
    }
}
