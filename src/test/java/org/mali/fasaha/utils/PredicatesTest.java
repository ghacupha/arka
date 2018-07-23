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

import com.google.common.annotations.GwtCompatible;
import com.google.common.annotations.GwtIncompatible;
import com.google.common.collect.ImmutableSet;
import com.google.common.testing.EqualsTester;
import com.google.common.testing.NullPointerTester;
import junit.framework.AssertionFailedError;
import junit.framework.TestCase;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Pattern;

import static com.google.common.base.CharMatcher.WHITESPACE;
import static com.google.common.collect.Lists.newArrayList;

/**
 * Ripped verbatim from Goole Guava 18.0.
 * 
 * Unit test for {@link Predicates}.
 */
@GwtCompatible(emulated = true)
public class PredicatesTest extends TestCase {
	private static final Predicate<Integer> TRUE = Predicates.alwaysTrue();
	private static final Predicate<Integer> FALSE = Predicates.alwaysFalse();
	private static final Predicate<Integer> NEVER_REACHED = new Predicate<Integer>() {
		@Override
		public boolean test(Integer i) {
			throw new AssertionFailedError(
					"This predicate should never have been evaluated");
		}
	};

	/** Instantiable predicate with reasonable hashCode() and equals() methods. */
	static class IsOdd implements Predicate<Integer>, Serializable {
		private static final long serialVersionUID = 0x150ddL;

		@Override
		public boolean test(Integer i) {
			return (i.intValue() & 1) == 1;
		}

		@Override
		public int hashCode() {
			return 0x150dd;
		}

		@Override
		public boolean equals(Object obj) {
			return obj instanceof IsOdd;
		}

		@Override
		public String toString() {
			return "IsOdd";
		}
	}

	/**
	 * Generates a new Predicate per call.
	 *
	 * <p>Creating a new Predicate each time helps catch cases where code is
	 * using {@code x == y} instead of {@code x.equals(y)}.
	 */
	private static IsOdd isOdd() {
		return new IsOdd();
	}

	/*
	 * Tests for Predicates.alwaysTrue().
	 */

	public void testAlwaysTrue_apply() {
		assertEvalsToTrue(Predicates.alwaysTrue());
	}

	public void testAlwaysTrue_equality() throws Exception {
		new EqualsTester()
				.addEqualityGroup(TRUE, Predicates.alwaysTrue())
				.addEqualityGroup(isOdd())
				.addEqualityGroup(Predicates.alwaysFalse())
				.testEquals();
	}

	/*
	 * Tests for Predicates.alwaysFalse().
	 */

	public void testAlwaysFalse_apply() throws Exception {
		assertEvalsToFalse(Predicates.alwaysFalse());
	}

	public void testAlwaysFalse_equality() throws Exception {
		new EqualsTester()
				.addEqualityGroup(FALSE, Predicates.alwaysFalse())
				.addEqualityGroup(isOdd())
				.addEqualityGroup(Predicates.alwaysTrue())
				.testEquals();
	}

	/*
	 * Tests for Predicates.not(predicate).
	 */

	public void testNot_apply() {
		assertEvalsToTrue(Predicates.not(FALSE));
		assertEvalsToFalse(Predicates.not(TRUE));
		assertEvalsLikeOdd(Predicates.not(Predicates.not(isOdd())));
	}

	public void testNot_equalityForNotOfKnownValues() {
		new EqualsTester()
				.addEqualityGroup(TRUE, Predicates.alwaysTrue())
				.addEqualityGroup(FALSE)
				.addEqualityGroup(Predicates.not(TRUE))
				.testEquals();

		new EqualsTester()
				.addEqualityGroup(FALSE, Predicates.alwaysFalse())
				.addEqualityGroup(TRUE)
				.addEqualityGroup(Predicates.not(FALSE))
				.testEquals();

		new EqualsTester()
				.addEqualityGroup(Predicates.isNull(), Predicates.isNull())
				.addEqualityGroup(Predicates.notNull())
				.addEqualityGroup(Predicates.not(Predicates.isNull()))
				.testEquals();

		new EqualsTester()
				.addEqualityGroup(Predicates.notNull(), Predicates.notNull())
				.addEqualityGroup(Predicates.isNull())
				.addEqualityGroup(Predicates.not(Predicates.notNull()))
				.testEquals();
	}

	/*
	 * Tests for all the different flavors of Predicates.and().
	 */
	public void testAnd_applyNoArgs() {
		assertEvalsToTrue(Predicates.and());
	}

	public void testAnd_applyOneArg() {
		assertEvalsLikeOdd(Predicates.and(isOdd()));
	}

	public void testAnd_applyBinary() {
		assertEvalsLikeOdd(Predicates.and(isOdd(), TRUE));
		assertEvalsLikeOdd(Predicates.and(TRUE, isOdd()));
		assertEvalsToFalse(Predicates.and(FALSE, NEVER_REACHED));
	}

	public void testAnd_applyTernary() {
		assertEvalsLikeOdd(Predicates.and(isOdd(), TRUE, TRUE));
		assertEvalsLikeOdd(Predicates.and(TRUE, isOdd(), TRUE));
		assertEvalsLikeOdd(Predicates.and(TRUE, TRUE, isOdd()));
		assertEvalsToFalse(Predicates.and(TRUE, FALSE, NEVER_REACHED));
	}

	public void testAnd_applyIterable() {
		Collection<Predicate<Integer>> empty = Arrays.asList();
		assertEvalsToTrue(Predicates.and(empty));
		assertEvalsLikeOdd(Predicates.and(Arrays.asList(isOdd())));
		assertEvalsLikeOdd(Predicates.and(Arrays.asList(TRUE, isOdd())));
		assertEvalsToFalse(Predicates.and(Arrays.asList(FALSE, NEVER_REACHED)));
	}

	@SuppressWarnings({"unchecked", "rawtypes"}) // varargs
	public void testAnd_arrayDefensivelyCopied() {
		Predicate[] array = {Predicates.alwaysFalse()};
		Predicate<Object> predicate = Predicates.and(array);
		assertFalse(predicate.test(1));
		array[0] = Predicates.alwaysTrue();
		assertFalse(predicate.test(1));
	}

	public void testAnd_listDefensivelyCopied() {
		List<Predicate<Object>> list = newArrayList();
		Predicate<Object> predicate = Predicates.and(list);
		assertTrue(predicate.test(1));
		list.add(Predicates.alwaysFalse());
		assertTrue(predicate.test(1));
	}

	public void testAnd_iterableDefensivelyCopied() {
		final List<Predicate<Object>> list = newArrayList();
		Iterable<Predicate<Object>> iterable = new Iterable<Predicate<Object>>() {
			@Override
			public Iterator<Predicate<Object>> iterator() {
				return list.iterator();
			}
		};
		Predicate<Object> predicate = Predicates.and(iterable);
		assertTrue(predicate.test(1));
		list.add(Predicates.alwaysFalse());
		assertTrue(predicate.test(1));
	}

	/*
	 * Tests for all the different flavors of Predicates.or().
	 */
	public void testOr_applyNoArgs() {
		assertEvalsToFalse(Predicates.or());
	}

	public void testOr_applyOneArg() {
		assertEvalsToTrue(Predicates.or(TRUE));
		assertEvalsToFalse(Predicates.or(FALSE));
	}

	public void testOr_applyBinary() {
		Predicate<Integer> falseOrFalse = Predicates.or(FALSE, FALSE);
		Predicate<Integer> falseOrTrue = Predicates.or(FALSE, TRUE);
		Predicate<Integer> trueOrAnything = Predicates.or(TRUE, NEVER_REACHED);

		assertEvalsToFalse(falseOrFalse);
		assertEvalsToTrue(falseOrTrue);
		assertEvalsToTrue(trueOrAnything);
	}

	public void testOr_applyTernary() {
		assertEvalsLikeOdd(Predicates.or(isOdd(), FALSE, FALSE));
		assertEvalsLikeOdd(Predicates.or(FALSE, isOdd(), FALSE));
		assertEvalsLikeOdd(Predicates.or(FALSE, FALSE, isOdd()));
		assertEvalsToTrue(Predicates.or(FALSE, TRUE, NEVER_REACHED));
	}

	public void testOr_applyIterable() {
		Predicate<Integer> vacuouslyFalse = Predicates.or(Collections.<Predicate<Integer>> emptyList());
		Predicate<Integer> troo = Predicates.or(Collections.singletonList(TRUE));
		/*
		 * newLinkedList() takes varargs. TRUE and FALSE are both instances of
		 * Predicate<Integer>, so the call is safe.
		 */
		Predicate<Integer> trueAndFalse = Predicates.or(Arrays.asList(TRUE, FALSE));

		assertEvalsToFalse(vacuouslyFalse);
		assertEvalsToTrue(troo);
		assertEvalsToTrue(trueAndFalse);
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	public void testOr_arrayDefensivelyCopied() {
		Predicate[] array = {Predicates.alwaysFalse()};
		Predicate<Object> predicate = Predicates.or(array);
		assertFalse(predicate.test(1));
		array[0] = Predicates.alwaysTrue();
		assertFalse(predicate.test(1));
	}

	public void testOr_listDefensivelyCopied() {
		List<Predicate<Object>> list = newArrayList();
		Predicate<Object> predicate = Predicates.or(list);
		assertFalse(predicate.test(1));
		list.add(Predicates.alwaysTrue());
		assertFalse(predicate.test(1));
	}

	public void testOr_iterableDefensivelyCopied() {
		final List<Predicate<Object>> list = newArrayList();
		Iterable<Predicate<Object>> iterable = new Iterable<Predicate<Object>>() {
			@Override
			public Iterator<Predicate<Object>> iterator() {
				return list.iterator();
			}
		};
		Predicate<Object> predicate = Predicates.or(iterable);
		assertFalse(predicate.test(1));
		list.add(Predicates.alwaysTrue());
		assertFalse(predicate.test(1));
	}

	/*
	 * Tests for Predicates.equalTo(x).
	 */

	public void testIsEqualTo_apply() {
		Predicate<Integer> isOne = Predicates.equalTo(1);

		assertTrue(isOne.test(1));
		assertFalse(isOne.test(2));
		assertFalse(isOne.test(null));
	}

	public void testIsEqualToNull_apply() {
		Predicate<Integer> isNull = Predicates.equalTo(null);
		assertTrue(isNull.test(null));
		assertFalse(isNull.test(1));
	}

	public void testIsEqualToNull_equality() {
		new EqualsTester()
				.addEqualityGroup(Predicates.equalTo(null), Predicates.equalTo(null))
				.addEqualityGroup(Predicates.equalTo(1))
				.addEqualityGroup(Predicates.equalTo("null"))
				.testEquals();
	}

	/**
	 * Tests for Predicates.instanceOf(x).
	 * TODO: Fix the comment style after fixing annotation stripper to remove
	 * comments properly.  Currently, all tests before the comments are removed
	 * as well.
	 */

	@GwtIncompatible("Predicates.instanceOf")
	public void testIsInstanceOf_apply() {
		Predicate<Object> isInteger = Predicates.instanceOf(Integer.class);

		assertTrue(isInteger.test(1));
		assertFalse(isInteger.test(2.0f));
		assertFalse(isInteger.test(""));
		assertFalse(isInteger.test(null));
	}

	@GwtIncompatible("Predicates.instanceOf")
	public void testIsInstanceOf_subclass() {
		Predicate<Object> isNumber = Predicates.instanceOf(Number.class);

		assertTrue(isNumber.test(1));
		assertTrue(isNumber.test(2.0f));
		assertFalse(isNumber.test(""));
		assertFalse(isNumber.test(null));
	}

	@GwtIncompatible("Predicates.instanceOf")
	public void testIsInstanceOf_interface() {
		Predicate<Object> isComparable = Predicates.instanceOf(Comparable.class);

		assertTrue(isComparable.test(1));
		assertTrue(isComparable.test(2.0f));
		assertTrue(isComparable.test(""));
		assertFalse(isComparable.test(null));
	}

	@GwtIncompatible("Predicates.assignableFrom")
	public void testIsAssignableFrom_apply() {
		Predicate<Class<?>> isInteger = Predicates.assignableFrom(Integer.class);

		assertTrue(isInteger.test(Integer.class));
		assertFalse(isInteger.test(Float.class));

		try {
			isInteger.test(null);
			fail();
		} catch (NullPointerException expected) {}
	}

	@GwtIncompatible("Predicates.assignableFrom")
	public void testIsAssignableFrom_subclass() {
		Predicate<Class<?>> isNumber = Predicates.assignableFrom(Number.class);

		assertTrue(isNumber.test(Integer.class));
		assertTrue(isNumber.test(Float.class));
	}

	@GwtIncompatible("Predicates.assignableFrom")
	public void testIsAssignableFrom_interface() {
		Predicate<Class<?>> isComparable = Predicates.assignableFrom(Comparable.class);

		assertTrue(isComparable.test(Integer.class));
		assertTrue(isComparable.test(Float.class));
	}

	/*
	 * Tests for Predicates.isNull()
	 */
	public void testIsNull_apply() {
		Predicate<Integer> isNull = Predicates.isNull();
		assertTrue(isNull.test(null));
		assertFalse(isNull.test(1));
	}

	public void testIsNull_equality() {
		new EqualsTester()
				.addEqualityGroup(Predicates.isNull(), Predicates.isNull())
				.addEqualityGroup(Predicates.notNull())
				.testEquals();
	}

	public void testNotNull_apply() {
		Predicate<Integer> notNull = Predicates.notNull();
		assertFalse(notNull.test(null));
		assertTrue(notNull.test(1));
	}

	public void testNotNull_equality() {
		new EqualsTester()
				.addEqualityGroup(Predicates.notNull(), Predicates.notNull())
				.addEqualityGroup(Predicates.isNull())
				.testEquals();
	}

	public void testIn_apply() {
		Collection<Integer> nums = Arrays.asList(1, 5);
		Predicate<Integer> isOneOrFive = Predicates.in(nums);

		assertTrue(isOneOrFive.test(1));
		assertTrue(isOneOrFive.test(5));
		assertFalse(isOneOrFive.test(3));
		assertFalse(isOneOrFive.test(null));
	}

	public void testIn_handlesNullPointerException() {
		class CollectionThatThrowsNPE<T> extends ArrayList<T> {
			private static final long serialVersionUID = 1L;

			@Override
			public boolean contains(Object element) {
				Objects.requireNonNull(element);
				return super.contains(element);
			}
		}
		Collection<Integer> nums = new CollectionThatThrowsNPE<Integer>();
		Predicate<Integer> isFalse = Predicates.in(nums);
		assertFalse(isFalse.test(null));
	}

	public void testIn_handlesClassCastException() {
		class CollectionThatThrowsCCE<T> extends ArrayList<T> {
			private static final long serialVersionUID = 1L;

			@Override
			public boolean contains(Object element) {
				throw new ClassCastException("");
			}
		}
		Collection<Integer> nums = new CollectionThatThrowsCCE<Integer>();
		nums.add(3);
		Predicate<Integer> isThree = Predicates.in(nums);
		assertFalse(isThree.test(3));
	}

	/*
	 * Tests that compilation will work when applying explicit types.
	 */
	@SuppressWarnings("unused") // compilation test
	public void testIn_compilesWithExplicitSupertype() {
		Collection<Number> nums = ImmutableSet.of();
		Predicate<Number> p1 = Predicates.in(nums);
		Predicate<Object> p2 = Predicates.<Object> in(nums);
		// The next two lines are not expected to compile.
		// Predicate<Integer> p3 = Predicates.in(nums);
		// Predicate<Integer> p4 = Predicates.<Integer>in(nums);
	}

	// enum singleton pattern
	private enum TrimStringFunction implements Function<String, String> {
		INSTANCE;

		@Override
		public String apply(String string) {
			return WHITESPACE.trimFrom(string);
		}
	}

	public void testCompose() {
		Function<String, String> trim = TrimStringFunction.INSTANCE;
		Predicate<String> equalsFoo = Predicates.equalTo("Foo");
		Predicate<String> trimEqualsFoo = Predicates.compose(equalsFoo, trim);

		assertTrue(trimEqualsFoo.test("Foo"));
		assertTrue(trimEqualsFoo.test("   Foo   "));
		assertFalse(trimEqualsFoo.test("Foo-b-que"));
	}

	/**
	 * Tests for Predicates.contains(Pattern) and .containsPattern(String).
	 * We assume the regex level works, so there are only trivial tests of that
	 * aspect.
	 * TODO: Fix comment style once annotation stripper is fixed.
	 */

	@GwtIncompatible("Predicates.containsPattern")
	public void testContainsPattern_apply() {
		Predicate<CharSequence> isFoobar = Predicates.containsPattern("^Fo.*o.*bar$");
		assertTrue(isFoobar.test("Foxyzoabcbar"));
		assertFalse(isFoobar.test("Foobarx"));
	}

	@GwtIncompatible("Predicates.containsPattern")
	public void testContains_apply() {
		Predicate<CharSequence> isFoobar = Predicates.contains(Pattern.compile("^Fo.*o.*bar$"));

		assertTrue(isFoobar.test("Foxyzoabcbar"));
		assertFalse(isFoobar.test("Foobarx"));
	}

	@GwtIncompatible("NullPointerTester")
	public void testContainsPattern_nulls() throws Exception {
		NullPointerTester tester = new NullPointerTester();
		Predicate<CharSequence> isWooString = Predicates.containsPattern("Woo");

		tester.testAllPublicInstanceMethods(isWooString);
	}

	@GwtIncompatible("NullPointerTester")
	public void testContains_nulls() throws Exception {
		NullPointerTester tester = new NullPointerTester();
		Predicate<CharSequence> isWooPattern = Predicates.contains(Pattern.compile("Woo"));

		tester.testAllPublicInstanceMethods(isWooPattern);
	}

	public void assertEqualHashCode(
			Predicate<? super Integer> expected, Predicate<? super Integer> actual) {
		assertEquals(actual + " should hash like " + expected, expected.hashCode(), actual.hashCode());
	}

	private static void assertEvalsToTrue(Predicate<? super Integer> predicate) {
		assertTrue(predicate.test(0));
		assertTrue(predicate.test(1));
		assertTrue(predicate.test(null));
	}

	private static void assertEvalsToFalse(Predicate<? super Integer> predicate) {
		assertFalse(predicate.test(0));
		assertFalse(predicate.test(1));
		assertFalse(predicate.test(null));
	}

	private static void assertEvalsLikeOdd(Predicate<? super Integer> predicate) {
		assertEvalsLike(isOdd(), predicate);
	}

	private static void assertEvalsLike(
			Predicate<? super Integer> expected,
			Predicate<? super Integer> actual) {
		assertEvalsLike(expected, actual, 0);
		assertEvalsLike(expected, actual, 1);
		assertEvalsLike(expected, actual, null);
	}

	private static <T> void assertEvalsLike(
			Predicate<? super T> expected,
			Predicate<? super T> actual,
			T input) {
		Boolean expectedResult = null;
		RuntimeException expectedRuntimeException = null;
		try {
			expectedResult = expected.test(input);
		} catch (RuntimeException e) {
			expectedRuntimeException = e;
		}

		Boolean actualResult = null;
		RuntimeException actualRuntimeException = null;
		try {
			actualResult = actual.test(input);
		} catch (RuntimeException e) {
			actualRuntimeException = e;
		}

		assertEquals(expectedResult, actualResult);
		if (expectedRuntimeException != null) {
			assertNotNull(actualRuntimeException);
			assertEquals(
					expectedRuntimeException.getClass(),
					actualRuntimeException.getClass());
		}
	}
}
