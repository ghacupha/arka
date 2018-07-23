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

import java.io.Serializable;
import java.util.Iterator;
import java.util.function.Function;

import static java.util.Objects.requireNonNull;

/**
 * A {@link org.mali.fasaha.utils.Converter} which may receive null and may return null.
 *
 * @param <A> Parameter type A to be converted to type B
 * @param <B> Parameter type B into which we have converted type A from
 *
 * @author edwin_njeru
 * @version $Id: $Id
 */
public interface Converter<A, B> {

    /**
     * Creates a converter using the given functions, with the given name shown in "toString()".
     *
     * @param forwardFunction a {@link java.util.function.Function} object.
     * @param backwardFunction a {@link java.util.function.Function} object.
     * @param name a {@link java.lang.String} object.
     * @return a {@link org.mali.fasaha.utils.Converter} object.
     * @param <A> a A object.
     * @param <B> a B object.
     */
    public static <A, B> Converter<A, B> from(Function<? super A, ? extends B> forwardFunction, Function<? super B, ? extends A> backwardFunction, String name) {
        return new FunctionBasedConverter<>(forwardFunction, backwardFunction, name);
    }

    /**
     * Returns a representation of {@code a} as an instance of type {@code B}. If {@code a} cannot be converted, an unchecked exception (such as {@link java.lang.IllegalArgumentException}) should be thrown.
     *
     * @param a the instance to convert; never null
     * @return the converted instance; never null
     */
    B convertNonNull(A a);

    /**
     * Returns a representation of {@code b} as an instance of type {@code A}. If {@code b} cannot be converted, an unchecked exception (such as {@link java.lang.IllegalArgumentException}) should be thrown.
     *
     * @param b the instance to convert; never null
     * @return the converted instance; never null
     * @throws java.lang.UnsupportedOperationException if backward conversion is not implemented; this should be very rare. Note that if backward conversion is not only unimplemented but unimplement<i>able</i>
     *                                       (for example, consider a {@code Converter<Chicken, ChickenNugget>}), then this is not logically a {@code Converter} at all, and should just implement
     *                                       {@link java.util.function.Function}.
     */
    A revertNonNull(B b);

    /**
     * Returns a converter whose {@code convert} method applies {@code secondConverter} to the result of this converter. Its {@code reverse} method applies the converters in reverse order.
     *
     * <p>The returned converter is serializable if {@code this} converter and {@code secondConverter} are.
     *
     * @param andThen a {@link org.mali.fasaha.utils.Converter} object.
     * @return a {@link org.mali.fasaha.utils.Converter} object.
     * @param <C> a C object.
     */
    default <C> Converter<A, C> andThen(Converter<B, C> andThen) {
        return new ConverterComposition<>(this, andThen);
    }

    /**
     * Returns the reversed view of this converter, where the {@link #convertNonNull(Object)} and {@link #revertNonNull(Object)} methods are swapped.
     *
     * @return a {@link org.mali.fasaha.utils.Converter} object.
     */
    default Converter<B, A> reverse() {
        return new ReverseConverter<B, A>(this);
    }

    /**
     * Returns an iterable that applies {@code convert} to each element of {@code fromIterable}. The conversion is done lazily.
     *
     * <p>The returned iterable's iterator supports {@code remove()} if the input iterator does. After
     * a successful {@code remove()} call, {@code fromIterable} no longer contains the corresponding element.
     *
     * @param fromIterable a {@link java.lang.Iterable} object.
     * @return a {@link java.lang.Iterable} object.
     */
    default Iterable<B> convertAll(Iterable<? extends A> fromIterable) {
        requireNonNull(fromIterable);
        return () -> new Iterator<B>() {

            final Iterator<? extends A> fromIterator = fromIterable.iterator();

            @Override
            public boolean hasNext() {
                return fromIterator.hasNext();
            }

            @Override
            public B next() {
                return convertNonNull(fromIterator.next());
            }

            @Override
            public void remove() {
                fromIterator.remove();
            }
        };
    }

    /**
     * This is an implementation of the {@code Converter<A, B>} backed by {@link java.util.function.Function}
     * @param <A> Parameter type A
     * @param <B> Parameter type B
     */
    static final class FunctionBasedConverter<A, B> implements Converter<A, B>, Serializable {

        private static final long serialVersionUID = 1L;

        final Function<? super A, ? extends B> forwardFunction;
        final Function<? super B, ? extends A> backwardFunction;
        final String name;

        private FunctionBasedConverter(Function<? super A, ? extends B> forwardFunction, Function<? super B, ? extends A> backwardFunction, String name) {
            this.forwardFunction = requireNonNull(forwardFunction);
            this.backwardFunction = requireNonNull(backwardFunction);
            this.name = requireNonNull(name);
        }

        @Override
        public B convertNonNull(A a) {
            return requireNonNull(forwardFunction.apply(a));
        }

        @Override
        public A revertNonNull(B b) {
            return requireNonNull(backwardFunction.apply(b));
        }

        @Override
        public boolean equals(Object object) {
            if (object instanceof FunctionBasedConverter) {
                FunctionBasedConverter<?, ?> that = (FunctionBasedConverter<?, ?>) object;
                return this.forwardFunction.equals(that.forwardFunction) && this.backwardFunction.equals(that.backwardFunction);
            } else {
                return false;
            }
        }

        @Override
        public int hashCode() {
            return forwardFunction.hashCode() * 31 + backwardFunction.hashCode();
        }

        @Override
        public String toString() {
            return name;
        }
    }

    /**
     * Creates {@code Converter<A,C>} from {@code Converter<A, B>} and {@code Converter<B, C>}
     *
     * @param <A> Parameter Type A
     * @param <B> Parameter Type B
     * @param <C> Parameter Type C
     */
    static final class ConverterComposition<A, B, C> implements Converter<A, C>, Serializable {

        private static final long serialVersionUID = 1L;

        final Converter<A, B> first;
        final Converter<B, C> second;

        private ConverterComposition(Converter<A, B> first, Converter<B, C> second) {
            this.first = requireNonNull(first);
            this.second = requireNonNull(second);
        }

        @Override
        public C convertNonNull(A a) {
            return second.convertNonNull(first.convertNonNull(a));
        }

        @Override
        public A revertNonNull(C c) {
            return first.revertNonNull(second.revertNonNull(c));
        }

        @Override
        public boolean equals(Object object) {
            if (object instanceof ConverterComposition) {
                ConverterComposition<?, ?, ?> that = (ConverterComposition<?, ?, ?>) object;
                return this.first.equals(that.first) && this.second.equals(that.second);
            } else {
                return false;
            }
        }

        @Override
        public int hashCode() {
            return 31 * first.hashCode() + second.hashCode();
        }

        @Override
        public String toString() {
            return first + ".andThen(" + second + ")";
        }
    }

    /**
     * Converts converter from {@code Converter<A,B>} to {@code Converter<B,A>} reversing the
     * original parameters.
     * @param <A> Right side parameter
     * @param <B> Left side parameter
     */
    static class ReverseConverter<A, B> implements Converter<A, B>, Serializable {

        private static final long serialVersionUID = 1L;

        final Converter<B, A> original;

        /**
         * Creates a reverse converter using a {@code Converter<B, A>}
         *
         * @param original The converter with which we are to create this ReverseConverter
         */
        ReverseConverter(Converter<B, A> original) {
            this.original = original;
        }

        /**
         * Converts parameter A to parameter type B
         * @param a the instance to convert; never null
         * @return converter value of Type B
         */
        @Override
        public B convertNonNull(A a) {
            return original.revertNonNull(a);
        }

        /**
         * Converts parameter B to parameter type A
         * @param b the instance to convert; never null
         * @return convert value of Type A
         */
        @Override
        public A revertNonNull(B b) {
            return original.convertNonNull(b);
        }

        /**
         * Returns the reverse of the Converter
         * @return {@code Converter<B, A>}
         */
        @Override
        public Converter<B, A> reverse() {
            return original;
        }

        @Override
        public boolean equals(Object object) {
            if (object instanceof ReverseConverter) {
                ReverseConverter<?, ?> that = (ReverseConverter<?, ?>) object;
                return this.original.equals(that.original);
            } else {
                return false;
            }
        }

        @Override
        public int hashCode() {
            return ~original.hashCode();
        }

        @Override
        public String toString() {
            return original.toString() + ".reverse()";
        }
    }
}
