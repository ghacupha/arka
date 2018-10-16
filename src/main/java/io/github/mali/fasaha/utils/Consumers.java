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

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Helper functions for manipulating {@link java.util.function.Consumer}.
 *     For instance should you desire to do totally nothing about a list you could do the following:
 *     <pre>
 *     {@code
 *     List<Integer> myIntList = ImmutableList.of(6,5,4,8);
 *     myIntList.forEach(Consumers.doNothing());
 *     }
 *     </pre>
 *     Ok that seems mundane, but if you were streaming it, you could have done alot of operations from the
 *     list, by the time you call the terminal operator "forEach". You could also syntanctically avoid empty
 *     lambdas
 *     You might want to something as you consume the output from the terminal, in a way creating a function
 *     or even a function and consumer inside a terminal operator. The {@link #compose(Function, Consumer)} function
 *     might come in handy as shown here:
 *     <pre>
 *         {@code
 *           List<Integer> myIntList = ImmutableList.of(6,5,4,8);
 *
 *           StringBuilder outPut = new StringBuilder();
 *           StringBuilder outPut2 = new StringBuilder();
 *
 *           // You could:
 *
 *           // 1. do something with the function and nothing with the consumer
 *           myIntList.forEach(Consumers.compose(outPut::append, Consumers.doNothing()));
 *           assertEquals("6548", outPut.toString());
 *
 *           // 2. do something with both function and consumer, accidentally creating a hashing function :)
 *           myIntList.forEach(Consumers.compose(outPut2::append, outPut2::append));
 *           assertEquals("665665466566548665665466566548", outPut2.toString());
 *         }
 *     </pre>
 *     Right. Then may be one day you will want to do something really unique, if that day has not come you could
 *     skip this:
 *     <pre>
 *         {@code
 *          List<Integer> myIntList = ImmutableList.of(6,5,4,8);
 *          myIntList.forEach(Consumers.redirectable(countOutputs()));
 *
 *          private Supplier<Consumer<Integer>> countOutputs() {
 *            return new Supplier<Consumer<Integer>>() {
 *              public Consumer<Integer> get() {
 *                  listCount++;
 *                 return new Consumer<Integer>() {
 *                      public void accept(final Integer integer) {
 *                         callCounter++;
 *                         System.out.println(String.format("Calling list count item: %s", integer));
 *                     }
 *                 };
 *               }
 *           };
 *        }
 *      }
 *    </pre>
 *    Needless to say that could lead to a lot of confusing code so, don't try this unnecessarily. With
 *    great power comes great responsibility.
 *
 *
 * @author edwin_njeru
 * @version $Id: $Id
 */
public class Consumers {
    /**
     * A Consumer which does nothing.
     *
     * @return a {@link java.util.function.Consumer} object.
     * @param <T> a T object.
     */
    public static <T> Consumer<T> doNothing() {
        return value -> {
        };
    }

    /**
     * The equivalent of {@link java.util.function.Function#compose}, but for Consumer.
     *
     * @param function a {@link java.util.function.Function} object.
     * @param consumer a {@link java.util.function.Consumer} object.
     * @return a {@link java.util.function.Consumer} object.
     * @param <T> a T object.
     * @param <R> a R object.
     */
    public static <T, R> Consumer<T> compose(Function<? super T, ? extends R> function, Consumer<? super R> consumer) {
        return value -> consumer.accept(function.apply(value));
    }

    /**
     * A Consumer which always passes its input to whatever Consumer is supplied by target.
     * <p>
     * By passing something mutable, such as a {@link Box}, you can redirect the returned consumer.
     *
     * @param target a {@link java.util.function.Supplier} object.
     * @return a {@link java.util.function.Consumer} object.
     * @param <T> a T object.
     */
    public static <T> Consumer<T> redirectable(Supplier<Consumer<T>> target) {
        return value -> target.get()
                              .accept(value);
    }
}
