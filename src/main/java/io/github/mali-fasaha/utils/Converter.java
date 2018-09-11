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
 * The application of the class is pretty standard but the internal implementation is designed to
 * allow definition using a functional interface of typical functional approaches, such as the one
 * shown:
 * <pre>
 *     {@code
 *     Converter<String, Integer> stringIntegerConverter =
 *         Converter.from(Integer::valueOf, String::valueOf, "stringIntegerConverter");
 *     }
 * </pre>
 * The Converter interface may also be defined using the good old anonymous class:
 * <pre>
 *     {@code
 *      Converter<String, Integer> integerConverter = new Converter<String, Integer>() {
 *             public Integer convertNonNull(final String s) {
 *
 *                 return Integer.valueOf(s);
 *             }
 *
 *             public String revertNonNull(final Integer integer) {
 *
 *                 return String.valueOf(integer);
 *             }
 *         };
 *     }
 * </pre>
 * Once defined the converter can be used to convert Type A to Type B, using the
 * {@link #convertNonNull(Object)} method.
 * <pre>
 *     {@code
 *     assertEquals(Integer.valueOf("654"), stringIntegerConverter.convertNonNull("654"));
 *     }
 * </pre>
 * The same definition can be used to do reverse conversions from Type B to Type A, using the
 * {@link #revertNonNull(Object)} method.
 * <pre>
 *     {@code
 *     assertEquals(String.valueOf(654), stringIntegerConverter.revertNonNull(654));
 *     }
 * </pre>
 * You could achieve the same result by creating a reverse converter first, using the the
 * {@link #reverse()} method, like the method here:
 * <pre>
 *     {@code
 *     Converter<Integer, String> integerStringConverter = stringIntegerConverter.reverse();
 *     assertEquals("7895", integerStringConverter.convertNonNull(7895));
 *     }
 * </pre>
 * The {@link #andThen(Converter)} enables you to chain converters coming up with one
 * that does something entirely different. This could save the developer a lot of code.
 * <br>
 * In the illustration below we are going to chain a StringToInteger converter with an
 * IntegerToDouble converter. What is happening is that the output from the StringToInteger
 * converter is fed into the IntegerToDouble Converter. Effectively we will have created a
 * kind of StringToDoubleConverter:
 * <pre>
 * {@code
 * List<String> stringList = ImmutableList.of("10", "45", "65", "89");
 *
 * Converter<Integer, Double> intDoubleConverter =
 *             Converter.from(Integer::doubleValue, Double::intValue, "intDoubleConverter");
 * }
 * </pre>
 * Now we come in with {@link #andThen(Converter)} method and use it to extend the stringIntegerConverter
 * we defined before with the new intDoubleConverter, like so:
 * <pre>
 *     {@code
 *     Converter<String, Double> stringDoubleConverter = stringIntegerConverter.andThen(intDoubleConverter);
 *
 *     List<Double> doubles = new ArrayList<>();
 *
 *     stringDoubleConverter.convertAll(stringList).forEach(doubles::add);
 *     }
 * </pre>
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
