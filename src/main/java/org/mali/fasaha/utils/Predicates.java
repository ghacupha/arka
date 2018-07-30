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

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Pattern;

/**
 * Helper functions for manipulating {@link java.util.function.Predicate}, copied from Guava.
 * <p>
 * The function signatures below are identical to Google's Guava 18.0, except that guava's
 * functional interfaces have been swapped with Java 8's. It is tested against the same test
 * suite as Google Guava to ensure functional compatibility.
 * <p>
 * Most of the implementation has been replaced with lambdas, which means that the following
 * functionality has been removed: equals(), hashCode(), toString(), GWT, and serialization.
 * <p>
 * Lambdas don't support these methods, and there isn't much reason why they should, so we
 * removed them.
 *
 * @author edwin_njeru
 * @version $Id: $Id
 */
public final class Predicates {
    private Predicates() {
    }

    /**
     * Returns a predicate that always evaluates to {@code true}.
     *
     * @return a {@link java.util.function.Predicate} object.
     * @param <T> a T object.
     */
    public static <T> Predicate<T> alwaysTrue() {
        return t -> true;
    }

    /**
     * Returns a predicate that always evaluates to {@code false}.
     *
     * @return a {@link java.util.function.Predicate} object.
     * @param <T> a T object.
     */
    public static <T> Predicate<T> alwaysFalse() {
        return t -> false;
    }

    /**
     * Returns a predicate that evaluates to {@code true} if the object reference being tested is null.
     *
     * @return a {@link java.util.function.Predicate} object.
     * @param <T> a T object.
     */
    public static <T> Predicate<T> isNull() {
        return Objects::isNull;
    }

    /**
     * Returns a predicate that evaluates to {@code true} if the object reference being tested is not null.
     *
     * @return a {@link java.util.function.Predicate} object.
     * @param <T> a T object.
     */
    public static <T> Predicate<T> notNull() {
        return Objects::nonNull;
    }

    /**
     * Returns a predicate that evaluates to {@code true} if the given predicate evaluates to
     * {@code false}.
     *
     * @param predicate a {@link java.util.function.Predicate} object.
     * @return a {@link java.util.function.Predicate} object.
     * @param <T> a T object.
     */
    public static <T> Predicate<T> not(Predicate<T> predicate) {
        return predicate.negate();
    }

    /**
     * Returns a predicate that evaluates to {@code true} if each of its
     * components evaluates to {@code true}. The components are evaluated in
     * order, and evaluation will be "short-circuited" as soon as a false
     * predicate is found. It defensively copies the iterable passed in, so future
     * changes to it won't alter the behavior of this predicate. If {@code
     * components} is empty, the returned predicate will always evaluate to {@code
     * true}.
     *
     * @param components a {@link java.lang.Iterable} object.
     * @return a {@link java.util.function.Predicate} object.
     * @param <T> a T object.
     */
    public static <T> Predicate<T> and(Iterable<? extends Predicate<? super T>> components) {
        return new AndPredicate<>(defensiveCopy(components));
    }

    /**
     * Returns a predicate that evaluates to {@code true} if each of its components evaluates to
     * {@code true}. The components are evaluated in order, and evaluation will be "short-circuited"
     * as soon as a false predicate is found. It defensively copies the array passed in, so
     * future changes to it won't alter the behavior of this predicate. If {@code components} is empty,
     * the returned predicate will always evaluate to {@code true}.
     *
     * @param components a {@link java.util.function.Predicate} object.
     * @return a {@link java.util.function.Predicate} object.
     * @param <T> a T object.
     */
    @SafeVarargs
    public static <T> Predicate<T> and(Predicate<? super T>... components) {
        return new AndPredicate<T>(defensiveCopy(components));
    }

    /**
     * Returns a predicate that evaluates to {@code true} if both of its components evaluate to
     * {@code true}. The components are evaluated in order, and evaluation will be "short-circuited"
     * as soon as a false predicate is found.
     *
     * @param first a {@link java.util.function.Predicate} object.
     * @param second a {@link java.util.function.Predicate} object.
     * @return a {@link java.util.function.Predicate} object.
     * @param <T> a T object.
     */
    public static <T> Predicate<T> and(Predicate<? super T> first, Predicate<? super T> second) {
        return new AndPredicate<>(Predicates.<T>asList(Objects.requireNonNull(first), Objects.requireNonNull(second)));
    }

    /**
     * Returns a predicate that evaluates to {@code true} if any one of its components evaluates to
     * {@code true}. The components are evaluated in order, and evaluation will be "short-circuited" as
     * soon as a true predicate is found. It defensively copies the iterable passed in, so future
     * changes to it won't alter the behavior of this predicate. If {@code components} is empty,
     * the returned predicate will always evaluate to {@code false}.
     *
     * @param components a {@link java.lang.Iterable} object.
     * @return a {@link java.util.function.Predicate} object.
     * @param <T> a T object.
     */
    public static <T> Predicate<T> or(Iterable<? extends Predicate<? super T>> components) {
        return new OrPredicate<>(defensiveCopy(components));
    }

    /**
     * Returns a predicate that evaluates to {@code true} if any one of its components evaluates to {@code true}. The components are evaluated in order, and evaluation will be "short-circuited" as
     * soon as a true predicate is found. It defensively copies the array passed in, so future changes to it won't alter the behavior of this predicate. If {@code components} is empty, the returned
     * predicate will always evaluate to {@code false}.
     *
     * @param components a {@link java.util.function.Predicate} object.
     * @return a {@link java.util.function.Predicate} object.
     * @param <T> a T object.
     */
    @SafeVarargs
    public static <T> Predicate<T> or(Predicate<? super T>... components) {
        return new OrPredicate<T>(defensiveCopy(components));
    }

    /**
     * Returns a predicate that evaluates to {@code true} if either of its components evaluates to
     * {@code true}. The components are evaluated in order, and evaluation will be "short-circuited"
     * as soon as a true predicate is found.
     *
     * @param first a {@link java.util.function.Predicate} object.
     * @param second a {@link java.util.function.Predicate} object.
     * @return a {@link java.util.function.Predicate} object.
     * @param <T> a T object.
     */
    public static <T> Predicate<T> or(Predicate<? super T> first, Predicate<? super T> second) {
        return new OrPredicate<>(Predicates.<T>asList(Objects.requireNonNull(first), Objects.requireNonNull(second)));
    }

    /**
     * Returns a predicate that evaluates to {@code true} if the object being tested {@code equals()}
     * the given target or both are null.
     *
     * @param target a T object.
     * @return a {@link java.util.function.Predicate} object.
     * @param <T> a T object.
     */
    public static <T> Predicate<T> equalTo(@Nullable T target) {
        return target == null ? isNull() : t -> Objects.equals(t, target);
    }

    /**
     * Returns a predicate that evaluates to {@code true} if the object being tested is an instance of
     * the given class. If the object being tested is {@code null} this predicate evaluates to {@code
     * false}.
     *
     * @param clazz a {@link java.lang.Class} object.
     * @return a {@link java.util.function.Predicate} object.
     */
    public static Predicate<Object> instanceOf(Class<?> clazz) {
        return t -> clazz.isInstance(t);
    }

    /**
     * Returns a predicate that evaluates to {@code true} if the class being tested is assignable
     * from the given class.  The returned predicate does not allow null inputs.
     *
     * @since 10.0
     * @param clazz a {@link java.lang.Class} object.
     * @return a {@link java.util.function.Predicate} object.
     */
    public static Predicate<Class<?>> assignableFrom(Class<?> clazz) {
        return t -> clazz.isAssignableFrom(t);
    }

    /**
     * Returns a predicate that evaluates to {@code true} if the object reference being tested is a
     * member of the given collection. It does not defensively copy the collection passed in, so future
     * changes to it will alter the behavior of the predicate.
     *
     * <p>This method can technically accept any {@code Collection<?>}, but using
     * a typed collection helps prevent bugs. This approach doesn't block any potential users
     * since it is always possible to use {@code Predicates.<Object>in()}.
     *
     * @param target the collection that may contain the function input
     * @param <T> a T object.
     * @return a {@link java.util.function.Predicate} object.
     */
    public static <T> Predicate<T> in(Collection<? extends T> target) {
        return t -> {
            try {
                return target.contains(t);
            } catch (ClassCastException | NullPointerException e) {
                return false;
            }
        };
    }

    /**
     * Returns the composition of a function and a predicate. For every {@code x}, the generated
     * predicate returns {@code predicate(function(x))}.
     *
     * @return the composition of the provided function and predicate
     * @param predicate a {@link java.util.function.Predicate} object.
     * @param function a {@link java.util.function.Function} object.
     * @param <A> a A object.
     * @param <B> a B object.
     */
    public static <A, B> Predicate<A> compose(Predicate<B> predicate, Function<A, ? extends B> function) {
        return t -> predicate.test(function.apply(t));
    }

    /**
     * Returns a predicate that evaluates to {@code true} if the {@code CharSequence} being tested contains any match for the given regular expression pattern. The test used is equivalent to {@code
     * Pattern.compile(pattern).matcher(arg).find()}
     *
     * @throws java.util.regex.PatternSyntaxException if the pattern is invalid
     * @since 3.0
     * @param pattern a {@link java.lang.String} object.
     * @return a {@link java.util.function.Predicate} object.
     */
    public static Predicate<CharSequence> containsPattern(String pattern) {
        return contains(Pattern.compile(pattern));
    }

    /**
     * Returns a predicate that evaluates to {@code true} if the {@code CharSequence} being tested contains any match for the given regular expression pattern. The test used is equivalent to {@code
     * pattern.matcher(arg).find()}
     *
     * @since 3.0
     * @param pattern a {@link java.util.regex.Pattern} object.
     * @return a {@link java.util.function.Predicate} object.
     */
    public static Predicate<CharSequence> contains(Pattern pattern) {
        return t -> pattern.matcher(t)
                           .find();
    }

    private static <T> List<Predicate<? super T>> asList(Predicate<? super T> first, Predicate<? super T> second) {
        return Arrays.<Predicate<? super T>>asList(first, second);
    }

    @SafeVarargs
    private static <T> List<T> defensiveCopy(T... array) {
        return defensiveCopy(Arrays.asList(array));
    }

    static <T> List<T> defensiveCopy(Iterable<T> iterable) {
        ArrayList<T> list = new ArrayList<>();
        for (T element : iterable) {
            list.add(Objects.requireNonNull(element));
        }
        return list;
    }

    /**
     * @see Predicates#and(Iterable)
     */
    private static class AndPredicate<T> implements Predicate<T> {
        private final List<? extends Predicate<? super T>> components;

        private AndPredicate(List<? extends Predicate<? super T>> components) {
            this.components = components;
        }

        @Override
        public boolean test(@Nullable T t) {
            // Avoid using the Iterator to avoid generating garbage (issue 820).
            for (int i = 0; i < components.size(); i++) {
                if (!components.get(i)
                               .test(t)) {
                    return false;
                }
            }
            return true;
        }
    }

    /**
     * @see Predicates#or(Iterable)
     */
    private static class OrPredicate<T> implements Predicate<T> {
        private final List<? extends Predicate<? super T>> components;

        private OrPredicate(List<? extends Predicate<? super T>> components) {
            this.components = components;
        }

        @Override
        public boolean test(@Nullable T t) {
            // Avoid using the Iterator to avoid generating garbage (issue 820).
            for (int i = 0; i < components.size(); i++) {
                if (components.get(i)
                              .test(t)) {
                    return true;
                }
            }
            return false;
        }
    }
}
