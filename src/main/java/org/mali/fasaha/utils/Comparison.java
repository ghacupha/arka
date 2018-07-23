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

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.util.Comparator;

/**
 * Safe representation of the result of a comparison (better than int).
 *
 * @author edwin_njeru
 * @version $Id: $Id
 */
public enum Comparison {
    LESSER, EQUAL, GREATER;

    /**
     * Returns a Comparison result from the two given Comparables.
     *
     * @param a a T object.
     * @param b a T object.
     * @return a {@link org.mali.fasaha.utils.Comparison} object.
     */
    public static <T extends Comparable<T>> Comparison compare(T a, T b) {
        return from(a.compareTo(b));
    }

    /**
     * Returns a Comparison result from the two values using a Comparator.
     *
     * @param comparator a {@link java.util.Comparator} object.
     * @param a a T object.
     * @param b a T object.
     * @return a {@link org.mali.fasaha.utils.Comparison} object.
     * @param <T> a T object.
     */
    public static <T> Comparison compare(Comparator<T> comparator, T a, T b) {
        return from(comparator.compare(a, b));
    }

    /**
     * Returns a Comparison from the given result of a call to <code>Comparable.compareTo()</code> or <code>Comparator.compare</code>.
     *
     * @param compareToResult a int.
     * @return a {@link org.mali.fasaha.utils.Comparison} object.
     */
    @SuppressFBWarnings(value = "UC_USELESS_CONDITION", justification = "Throwing Unhandled keeps the full-enumeration more explicit.")
    public static Comparison from(int compareToResult) {
        if (compareToResult == 0) {
            return Comparison.EQUAL;
        } else if (compareToResult < 0) {
            return Comparison.LESSER;
        } else if (compareToResult > 0) {
            return Comparison.GREATER;
        } else {
            throw Unhandled.integerException(compareToResult);
        }
    }

    /**
     * Returns the appropriate T based on the Comparison value.
     *
     * @param lesser a T object.
     * @param equal a T object.
     * @param greater a T object.
     * @return a T object.
     * @param <T> a T object.
     */
    public <T> T lesserEqualGreater(T lesser, T equal, T greater) {
        switch (this) {
            case LESSER:
                return lesser;
            case EQUAL:
                return equal;
            case GREATER:
                return greater;
            default:
                throw Unhandled.enumException(this);
        }
    }
}
