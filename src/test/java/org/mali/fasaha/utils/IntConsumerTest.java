package org.mali.fasaha.utils;

import org.junit.Before;
import org.junit.Test;
import org.mali.fasaha.utils.Throwing.IntConsumer;
import org.mali.fasaha.utils.Throwing.Runnable;
import org.mali.fasaha.utils.Throwing.Specific;

import java.util.Arrays;
import java.util.function.Consumer;
import java.util.stream.IntStream;

import static org.junit.Assert.*;

public class IntConsumerTest {

    @Test
    public void intConsumer() throws Exception {

        Throwing.IntConsumer calc = x -> {

            System.out.println(String.format("Accepting: %s", x));

            double y = Double.parseDouble(String.valueOf(x));

            y = 1 / y;

            System.out.println(String.format("Result: %s", y));

            /*
             * As you might imagine testing a void method is not fun. But in this
             * test I know which variable the Consumer is going to accept, so I can
             * dare create an assertion here. Feel free to report me to uncle Bob.
             * Warning: If this test fails, your build tools will still "pass" it,
             * and quietly report an assertion error. Agail feel free to report me
             * to uncle bob
             */
            assertEquals(0.0125, y, 0.0000);
        };

        Errors.suppress().wrap(() -> calc.accept(80));

    }
}