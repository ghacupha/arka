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

import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.Optional;

public class MoreCollectorsTest {
	@Test
	public void testSingleOrEmpty() {
		testCase(Optional.empty());
		testCase(Optional.of(1), 1);
		testCase(Optional.empty(), 1, 1);
		testCase(Optional.empty(), 1, 1, 1);
	}

	private void testCase(Optional<Integer> expected, Integer... values) {
		// test the short-circuiting operator
		Assert.assertEquals(expected, MoreCollectors.singleOrEmptyShortCircuiting(Arrays.stream(values)));
		// and the simpler one
		Assert.assertEquals(expected, Arrays.stream(values).collect(MoreCollectors.singleOrEmpty()));
	}

	@Test
	public void testCodePointsToString() {
		String spacesRemoved = MoreCollectors.codePointsToString("a b c".codePoints().filter(c -> c != ' '));
		Assert.assertEquals("abc", spacesRemoved);
	}
}
