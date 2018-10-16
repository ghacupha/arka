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
package io.github.mali.fasaha.utils;

import com.google.common.collect.ImmutableList;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static org.junit.Assert.*;

public class ConsumersTest {

    private List<Integer> myIntList;

    private int listCount;

    private int callCounter;

    @Before
    public void setUp() throws Exception {

        myIntList = ImmutableList.of(6,5,4,8);

        listCount = 0;

        callCounter = 0;
    }

    @Test
    public void doNothing() {

        myIntList.forEach(Consumers.doNothing());
    }

    @Test
    public void compose() {

        StringBuilder outPut = new StringBuilder();

        StringBuilder outPut2 = new StringBuilder();

        // do something with the function and nothing with the consumer
        myIntList.forEach(Consumers.compose(outPut::append, Consumers.doNothing()));

        assertEquals("6548", outPut.toString());

        // do something with both function and consumer
        myIntList.forEach(Consumers.compose(outPut2::append, outPut2::append));

        assertEquals("665665466566548665665466566548", outPut2.toString());
    }

    @Test
    public void redirectable() {

        myIntList.forEach(Consumers.redirectable(countOutputs()));

        System.out.println(String.format("List count: %s", listCount));

        assertEquals(4, listCount);
        assertEquals(4, callCounter);

    }


    private Supplier<Consumer<Integer>> countOutputs() {

        return new Supplier<Consumer<Integer>>() {
            @Override
            public Consumer<Integer> get() {

                listCount++;

                return new Consumer<Integer>() {
                    @Override
                    public void accept(final Integer integer) {

                        callCounter++;

                        System.out.println(String.format("Calling list count item: %s", integer));
                    }
                };
            }
        };
    }


}