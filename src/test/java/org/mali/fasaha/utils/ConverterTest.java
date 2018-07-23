package org.mali.fasaha.utils;

import com.google.common.collect.ImmutableList;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import static java.lang.Integer.*;
import static org.junit.Assert.*;

public class ConverterTest {

    private Converter<String, Integer> stringIntegerConverter;

    @Before
    public void setUp() throws Exception {

        stringIntegerConverter = Converter.from(Integer::valueOf, String::valueOf, "stringIntegerConverter");
    }

    @Test
    public void from() {

        Converter<String, Integer> stringIntegerConverter = Converter.from(Integer::valueOf, String::valueOf, "stringIntegerConverter");

        Converter<String, Integer> integerConverter = new Converter<String, Integer>() {
            @Override
            public Integer convertNonNull(final String s) {

                return Integer.valueOf(s);
            }

            @Override
            public String revertNonNull(final Integer integer) {

                return String.valueOf(integer);
            }
        };

        assertEquals(String.valueOf(654), stringIntegerConverter.revertNonNull(654));
        assertEquals(Integer.valueOf("654"), stringIntegerConverter.convertNonNull("654"));
        assertEquals(String.valueOf(654), integerConverter.revertNonNull(654));
        assertEquals(Integer.valueOf("654"), integerConverter.convertNonNull("654"));

    }

    @Test
    public void convertNonNull() {

        assertEquals(Integer.valueOf("654"), stringIntegerConverter.convertNonNull("654"));
    }

    @Test
    public void revertNonNull() {

        assertEquals(String.valueOf(654), stringIntegerConverter.revertNonNull(654));
    }

    // @formatter:off
    @Test
    public void andThen() {
        List<String> stringList = ImmutableList.of("10", "45", "65", "89");

        Converter<Integer, Double> intDoubleConverter =
            Converter.from(Integer::doubleValue, Double::intValue, "intDoubleConverter");

        Converter<String, Double> stringDoubleConverter =
            stringIntegerConverter.andThen(intDoubleConverter);

        List<Double> doubles = new ArrayList<>();
        stringDoubleConverter.convertAll(stringList).forEach(doubles::add);

        assertEquals(4, doubles.size());
        assertEquals(10.00, doubles.get(0), 0);
        assertEquals(45.00, doubles.get(1), 0);
        assertEquals(65.00, doubles.get(2), 0);
        assertEquals(89.00, doubles.get(3), 0);
        assertSame(doubles.get(0).getClass(), Double.class);
        assertSame(doubles.get(1).getClass(), Double.class);
        assertSame(doubles.get(2).getClass(), Double.class);
        assertSame(doubles.get(3).getClass(), Double.class);

    }
    // @formatter:on

    @Test
    public void reverse() {
    }

    @Test
    public void convertAll() {
    }
}