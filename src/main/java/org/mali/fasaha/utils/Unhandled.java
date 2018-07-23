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

/**
 * Exceptions designed for checking programming errors (e.g. unexpected default or else clauses).
 *
 * @author edwin_njeru
 * @version $Id: $Id
 */
public class Unhandled extends IllegalArgumentException {
    private static final long serialVersionUID = 0L;

    /**
     * <p>Constructor for Unhandled.</p>
     *
     * @param message a {@link java.lang.String} object.
     */
    public Unhandled(String message) {
        super(message);
    }

    /**
     * <p>classException.</p>
     *
     * @param o a {@link java.lang.Object} object.
     * @return a {@link org.mali.fasaha.utils.Unhandled} object.
     */
    public static Unhandled classException(Object o) {
        if (o == null) {
            return new Unhandled("Unhandled class 'null'");
        } else {
            if (o instanceof Class) {
                return new Unhandled("Unhandled class '" + ((Class<?>) o).getName() + "'");
            } else {
                return new Unhandled("Unhandled class '" + o.getClass()
                                                            .getName() + "'");
            }
        }
    }

    /**
     * <p>enumException.</p>
     *
     * @param e a {@link java.lang.Enum} object.
     * @return a {@link org.mali.fasaha.utils.Unhandled} object.
     */
    public static Unhandled enumException(Enum<?> e) {
        if (e == null) {
            return new Unhandled("Unhandled enum value 'null'");
        } else {
            return new Unhandled("Unhandled enum value '" + e.name() + "' for enum class '" + e.getClass() + "'");
        }
    }

    /**
     * <p>byteException.</p>
     *
     * @param b a byte.
     * @return a {@link org.mali.fasaha.utils.Unhandled} object.
     */
    public static Unhandled byteException(byte b) {
        return new Unhandled("Unhandled byte '" + b + "'");
    }

    /**
     * <p>charException.</p>
     *
     * @param c a char.
     * @return a {@link org.mali.fasaha.utils.Unhandled} object.
     */
    public static Unhandled charException(char c) {
        return new Unhandled("Unhandled char '" + c + "'");
    }

    /**
     * <p>shortException.</p>
     *
     * @param s a short.
     * @return a {@link org.mali.fasaha.utils.Unhandled} object.
     */
    public static Unhandled shortException(short s) {
        return new Unhandled("Unhandled short '" + s + "'");
    }

    /**
     * <p>integerException.</p>
     *
     * @param i a int.
     * @return a {@link org.mali.fasaha.utils.Unhandled} object.
     */
    public static Unhandled integerException(int i) {
        return new Unhandled("Unhandled integer '" + i + "'");
    }

    /**
     * <p>floatException.</p>
     *
     * @param f a float.
     * @return a {@link org.mali.fasaha.utils.Unhandled} object.
     */
    public static Unhandled floatException(float f) {
        return new Unhandled("Unhandled float '" + f + "'");
    }

    /**
     * <p>doubleException.</p>
     *
     * @param d a double.
     * @return a {@link org.mali.fasaha.utils.Unhandled} object.
     */
    public static Unhandled doubleException(double d) {
        return new Unhandled("Unhandled double '" + d + "'");
    }

    /**
     * <p>stringException.</p>
     *
     * @param str a {@link java.lang.String} object.
     * @return a {@link org.mali.fasaha.utils.Unhandled} object.
     */
    public static Unhandled stringException(String str) {
        return new Unhandled("Unhandled string '" + str + "'");
    }

    /**
     * <p>objectException.</p>
     *
     * @param obj a {@link java.lang.Object} object.
     * @return a {@link org.mali.fasaha.utils.Unhandled} object.
     */
    public static Unhandled objectException(Object obj) {
        return new Unhandled("Unhandled object '" + obj + "'");
    }
}
