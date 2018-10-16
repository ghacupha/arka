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

import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Creates {@code Stream}s that iterate across a tree defined by a {@link TreeDef} in various orders.
 *
 * @author edwin_njeru
 * @version $Id: $Id
 */
public class TreeStream {
    private TreeStream() {
    }

    /**
     * Creates a {@code Stream} that starts at {@code node} and ends at its root parent.
     *
     * @param treeDef a {@link TreeDef.Parented} object.
     * @param node a T object.
     * @return a {@link java.util.stream.Stream} object.
     * @param <T> a T object.
     */
    public static <T> Stream<T> toParent(TreeDef.Parented<T> treeDef, T node) {
        return StreamSupport.stream(TreeIterable.toParent(treeDef, node)
                                                .spliterator(), false);
    }

    /**
     * Creates a {@code Stream} that starts at {@code node} and iterates deeper into the tree in a bread-first order.
     *
     * @param treeDef a {@link TreeDef} object.
     * @param node a T object.
     * @return a {@link java.util.stream.Stream} object.
     * @param <T> a T object.
     */
    public static <T> Stream<T> breadthFirst(TreeDef<T> treeDef, T node) {
        return StreamSupport.stream(TreeIterable.breadthFirst(treeDef, node)
                                                .spliterator(), false);
    }

    /**
     * Creates a {@code Stream} that starts at {@code node} and iterates deeper into the tree in a depth-first order.
     *
     * @param treeDef a {@link TreeDef} object.
     * @param node a T object.
     * @return a {@link java.util.stream.Stream} object.
     * @param <T> a T object.
     */
    public static <T> Stream<T> depthFirst(TreeDef<T> treeDef, T node) {
        return StreamSupport.stream(TreeIterable.depthFirst(treeDef, node)
                                                .spliterator(), false);
    }
}
