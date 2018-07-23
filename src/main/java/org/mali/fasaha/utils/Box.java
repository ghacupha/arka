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
import java.util.function.DoubleConsumer;
import java.util.function.DoubleSupplier;
import java.util.function.Function;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;
import java.util.function.LongConsumer;
import java.util.function.LongSupplier;
import java.util.function.Supplier;

/**
 * Provides get/set access to a mutable non-null value.
 *
 * @author edwin_njeru
 * @version $Id: $Id
 */
public interface Box<T> extends Supplier<T>, Consumer<T> {
    /**
     * Creates a `Box` holding the given value in a `volatile` field.
     * <p>
     * Every call to {@link #set(Object)} confirms that the argument is actually non-null, and the value is stored in a volatile variable.
     *
     * @param value a T object.
     * @return a {@link org.mali.fasaha.utils.Box} object.
     * @param <T> a T object.
     */
    static <T> Box<T> ofVolatile(T value) {
        return new Volatile<>(value);
    }

    /**
     * Creates a `Box` holding the given value in a non-`volatile` field.
     * <p>
     * The value is stored in standard non-volatile field, and non-null-ness is not checked on every call to set.
     *
     * @param value a T object.
     * @return a {@link org.mali.fasaha.utils.Box} object.
     * @param <T> a T object.
     */
    static <T> Box<T> of(T value) {
        return new Default<>(value);
    }

    /**
     * Creates a `Box` from a `Supplier` and a `Consumer`.
     *
     * @param getter a {@link java.util.function.Supplier} object.
     * @param setter a {@link java.util.function.Consumer} object.
     * @return a {@link org.mali.fasaha.utils.Box} object.
     * @param <T> a T object.
     */
    static <T> Box<T> from(Supplier<T> getter, Consumer<T> setter) {
        Objects.requireNonNull(getter);
        Objects.requireNonNull(setter);
        return new Box<T>() {
            @Override
            public T get() {
                return Objects.requireNonNull(getter.get());
            }

            @Override
            public void set(T value) {
                setter.accept(Objects.requireNonNull(value));
            }

            @Override
            public String toString() {
                return "Box.from[" + get() + "]";
            }
        };
    }

    /**
     * Sets the value which will later be returned by get().
     *
     * @param value a T object.
     */
    void set(T value);

    /**
     * Delegates to set().
     *
     * @deprecated Provided to satisfy the {@link java.util.function.Consumer} interface; use {@link #set} instead.
     * @param value a T object.
     */
    @Deprecated
    default void accept(T value) {
        set(value);
    }

    /**
     * Performs a set() on the result of a get().
     * <p>
     * Some implementations may provide atomic semantics, but it's not required.
     *
     * @param mutator a {@link java.util.function.Function} object.
     * @return a T object.
     */
    default T modify(Function<? super T, ? extends T> mutator) {
        T modified = mutator.apply(get());
        set(modified);
        return modified;
    }

    /**
     * Maps one {@code Box} to another {@code Box}, preserving any {@link #modify(Function)} guarantees of the underlying Box.
     *
     * @param converter a {@link org.mali.fasaha.utils.Converter} object.
     * @return a {@link org.mali.fasaha.utils.Box} object.
     * @param <R> a R object.
     */
    default <R> Box<R> map(Converter<T, R> converter) {
        return new Mapped<>(this, converter);
    }

    /**
     * Provides get/set access to a mutable nullable value.
     */
    interface Nullable<T> extends Supplier<T>, Consumer<T> {
        /**
         * Creates a `Box.Nullable` holding the given possibly-null value in a `volatile` field.
         */
        static <T> Nullable<T> ofVolatile(@javax.annotation.Nullable T init) {
            //checkNotNull(init, "Dude! Are you trying to initiate a box with a null value?");
            return new Volatile<>(init);
        }

        /**
         * Creates a `Box.Nullable` holding a null value in a `volatile` field.
         */
        static <T> Nullable<T> ofVolatileNull() {
            return ofVolatile(null);
        }

        /**
         * Creates a `Box.Nullable` holding the given possibly-null value in a non-`volatile` field.
         */
        static <T> Nullable<T> of(@javax.annotation.Nullable T init) {
            //checkNotNull(init, "Dude! Are you trying to initiate a box with a null value?");
            return new Default<>(init);
        }

        /**
         * Creates a `Box.Nullable` holding null value in a non-`volatile` field.
         */
        static <T> Nullable<T> ofNull() {
            return of(null);
        }

        /**
         * Creates a `Box.Nullable` from a `Supplier` and a `Consumer`.
         */
        static <T> Nullable<T> from(Supplier<T> getter, Consumer<T> setter) {
            Objects.requireNonNull(getter);
            Objects.requireNonNull(setter);
            return new Nullable<T>() {
                @Override
                public T get() {
                    return getter.get();
                }

                @Override
                public void set(T value) {
                    setter.accept(value);
                }

                @Override
                public String toString() {
                    return "Box.Nullable.from[" + get() + "]";
                }
            };
        }

        /**
         * Sets the value which will later be returned by get().
         */
        void set(@javax.annotation.Nullable T value);

        /**
         * Delegates to set().
         *
         * @deprecated Provided to satisfy the {@code Function} interface; use {@link #set} instead.
         */
        @Deprecated
        default void accept(@javax.annotation.Nullable T value) {
            set(value);
        }

        /**
         * Shortcut for doing a set() on the result of a get().
         */
        default T modify(Function<? super T, ? extends T> mutator) {
            T modified = mutator.apply(get());
            set(modified);
            return modified;
        }

        /**
         * Maps one {@code Box} to another {@code Box}, preserving any {@link #modify(Function)} guarantees of the underlying Box.
         */
        default <R> Nullable<R> map(ConverterNullable<T, R> converter) {
            return new Mapped<>(this, converter);
        }

        final class Mapped<T, R> implements Nullable<R> {
            private final Nullable<T> delegate;
            private final ConverterNullable<T, R> converter;

            Mapped(Nullable<T> delegate, ConverterNullable<T, R> converter) {
                this.delegate = delegate;
                this.converter = converter;
            }

            @Override
            public R get() {
                return converter.convert(delegate.get());
            }

            @Override
            public void set(R value) {
                delegate.set(converter.revert(value));
            }

            /**
             * Shortcut for doing a set() on the result of a get().
             */
            @Override
            public R modify(Function<? super R, ? extends R> mutator) {
                Nullable<R> result = Nullable.of(null);
                delegate.modify(input -> {
                    R unmappedResult = mutator.apply(converter.convert(input));
                    result.set(unmappedResult);
                    return converter.revert(unmappedResult);
                });
                return result.get();
            }

            @Override
            public String toString() {
                return "[" + delegate + " mapped to " + get() + " by " + converter + "]";
            }
        }

        class Volatile<T> implements Nullable<T> {
            private volatile T obj;

            private Volatile(T init) {
                set(init);
            }

            @Override
            public T get() {
                return obj;
            }

            @Override
            public void set(@javax.annotation.Nullable T obj) {
                this.obj = obj;
            }

            @Override
            public String toString() {
                return "Box.Nullable.ofVolatile[" + get() + "]";
            }
        }

        class Default<T> implements Nullable<T> {
            private T obj;

            private Default(T init) {
                this.obj = init;
            }

            @Override
            public T get() {
                return obj;
            }

            @Override
            public void set(T obj) {
                this.obj = obj;
            }

            @Override
            public String toString() {
                return "Box.Nullable.of[" + get() + "]";
            }
        }
    }

    /**
     * A `Box` for primitive doubles.
     */
    interface Dbl extends DoubleSupplier, DoubleConsumer, Box<Double> {
        /**
         * Creates a `Box.Dbl` holding the given value in a non-`volatile` field.
         */
        static Dbl of(double value) {
            return new Default(value);
        }

        /**
         * Creates a `Box.Dbl` from a `DoubleSupplier` and a `DoubleConsumer`.
         */
        static Dbl from(DoubleSupplier getter, DoubleConsumer setter) {
            return new Dbl() {
                @Override
                public double getAsDouble() {
                    return getter.getAsDouble();
                }

                @Override
                public void set(double value) {
                    setter.accept(value);
                }

                @Override
                public String toString() {
                    return "Box.Dbl.from[" + get() + "]";
                }
            };
        }

        /**
         * Sets the value which will later be returned by get().
         */
        void set(double value);

        @Override
        double getAsDouble();

        /**
         * Delegates to {@link #getAsDouble()}.
         *
         * @deprecated Provided to satisfy {@code Box<Double>}; use {@link #getAsDouble()} instead.
         */
        @Override
        @Deprecated
        default Double get() {
            return getAsDouble();
        }

        /**
         * Delegates to {@link #set(double)}.
         *
         * @deprecated Provided to satisfy {@code Box<Double>}; use {@link #set(double)} instead.
         */
        @Override
        @Deprecated
        default void set(Double value) {
            set(value.doubleValue());
        }

        /**
         * Delegates to {@link #set(double)}.
         *
         * @deprecated Provided to satisfy the {@link DoubleConsumer}; use {@link #set(double)} instead.
         */
        @Deprecated
        @Override
        default void accept(double value) {
            set(value);
        }

        class Default implements Dbl {
            private double obj;

            private Default(double init) {
                set(init);
            }

            @Override
            public double getAsDouble() {
                return obj;
            }

            @Override
            public void set(double obj) {
                this.obj = obj;
            }

            @Override
            public String toString() {
                return "Box.Dbl.of[" + getAsDouble() + "]";
            }
        }
    }

    /**
     * A `Box` for primitive ints.
     */
    interface Int extends IntSupplier, IntConsumer, Box<Integer> {
        /**
         * Creates a `Box.Int` holding the given value in a non-`volatile` field.
         */
        static Int of(int value) {
            return new Default(value);
        }

        /**
         * Creates a `Box.Int` from a `IntSupplier` and a `IntConsumer`.
         */
        static Int from(IntSupplier getter, IntConsumer setter) {
            return new Int() {
                @Override
                public int getAsInt() {
                    return getter.getAsInt();
                }

                @Override
                public void set(int value) {
                    setter.accept(value);
                }

                @Override
                public String toString() {
                    return "Box.Int.from[" + get() + "]";
                }
            };
        }

        /**
         * Sets the value which will later be returned by {@link #getAsInt()}.
         */
        void set(int value);

        @Override
        int getAsInt();

        /**
         * Delegates to {@link #getAsInt()}.
         *
         * @deprecated Provided to satisfy {@code Box<Integer>}; use {@link #getAsInt()} instead.
         */
        @Override
        @Deprecated
        default Integer get() {
            return getAsInt();
        }

        /**
         * Delegates to {@link #set(int)}.
         *
         * @deprecated Provided to satisfy {@code Box<Integer>}; use {@link #set(int)} instead.
         */
        @Override
        @Deprecated
        default void set(Integer value) {
            set(value.intValue());
        }

        /**
         * Delegates to {@link #set}.
         *
         * @deprecated Provided to satisfy the {@link IntConsumer} interface; use {@link #set(int)} instead.
         */
        @Deprecated
        @Override
        default void accept(int value) {
            set(value);
        }

        class Default implements Int {
            private int obj;

            private Default(int init) {
                set(init);
            }

            @Override
            public int getAsInt() {
                return obj;
            }

            @Override
            public void set(int obj) {
                this.obj = obj;
            }

            @Override
            public String toString() {
                return "Box.Int.of[" + get() + "]";
            }
        }
    }

    /**
     * A `Box` for primitive longs.
     */
    interface Lng extends LongSupplier, LongConsumer, Box<Long> {
        /**
         * Creates a `Box.Long` holding the given value in a non-`volatile` field.
         */
        static Lng of(long value) {
            return new Default(value);
        }

        /**
         * Creates a `Box.Long` from a `LongSupplier` and a `LongConsumer`.
         */
        static Lng from(LongSupplier getter, LongConsumer setter) {
            return new Lng() {
                @Override
                public long getAsLong() {
                    return getter.getAsLong();
                }

                @Override
                public void set(long value) {
                    setter.accept(value);
                }

                @Override
                public String toString() {
                    return "Box.Long.from[" + get() + "]";
                }
            };
        }

        /**
         * Sets the value which will later be returned by {@link #getAsLong()}.
         */
        void set(long value);

        @Override
        long getAsLong();

        /**
         * Auto-boxed getter.
         *
         * @deprecated Provided to satisfy {@code Box<Long>} interface; use {@link #getAsLong()} instead.
         */
        @Override
        @Deprecated
        default Long get() {
            return getAsLong();
        }

        /**
         * Delegates to {@link #set(long)}.
         *
         * @deprecated Provided to satisfy {@code Box<Long>} interface; use {@link #set(long)} instead.
         */
        @Override
        @Deprecated
        default void set(Long value) {
            set(value.longValue());
        }

        /**
         * Delegates to {@link #set(long)}.
         *
         * @deprecated Provided to satisfy {@link LongConsumer} interface; use {@link #set(long)} instead.
         */
        @Deprecated
        @Override
        default void accept(long value) {
            set(value);
        }

        class Default implements Lng {
            private long obj;

            private Default(long init) {
                set(init);
            }

            @Override
            public long getAsLong() {
                return obj;
            }

            @Override
            public void set(long obj) {
                this.obj = obj;
            }

            @Override
            public String toString() {
                return "Box.Long.of[" + get() + "]";
            }
        }
    }

    final class Mapped<T, R> implements Box<R> {
        private final Box<T> delegate;
        private final Converter<T, R> converter;

        Mapped(Box<T> delegate, Converter<T, R> converter) {
            this.delegate = delegate;
            this.converter = converter;
        }

        @Override
        public R get() {
            return converter.convertNonNull(delegate.get());
        }

        @Override
        public void set(R value) {
            delegate.set(converter.revertNonNull(value));
        }

        /**
         * Shortcut for doing a set() on the result of a get().
         */
        @Override
        public R modify(Function<? super R, ? extends R> mutator) {
            Nullable<R> result = Nullable.of(null);
            delegate.modify(input -> {
                R unmappedResult = mutator.apply(converter.convertNonNull(input));
                result.set(unmappedResult);
                return converter.revertNonNull(unmappedResult);
            });
            return result.get();
        }

        @Override
        public String toString() {
            return "[" + delegate + " mapped to " + get() + " by " + converter + "]";
        }
    }

    final class Volatile<T> implements Box<T> {
        private volatile T obj;

        private Volatile(T init) {
            set(init);
        }

        @Override
        public T get() {
            return obj;
        }

        @Override
        public void set(T obj) {
            this.obj = Objects.requireNonNull(obj);
        }

        @Override
        public String toString() {
            return "Box.ofVolatile[" + get() + "]";
        }
    }

    final class Default<T> implements Box<T> {

        private T obj;

        private Default(T init) {
            set(init);
        }

        @Override
        public T get() {
            return obj;
        }

        @Override
        public void set(T obj) {
            this.obj = Objects.requireNonNull(obj);
        }

        @Override
        public String toString() {
            return "Box.of[" + get() + "]";
        }
    }
}
