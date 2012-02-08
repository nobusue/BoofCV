/*
 * Copyright (c) 2011-2012, Peter Abeles. All Rights Reserved.
 *
 * This file is part of BoofCV (http://boofcv.org).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package boofcv.alg.filter.derivative;

import boofcv.struct.image.ImageSingleBand;
import boofcv.struct.sparse.SparseImageSample;
import boofcv.testing.BoofTesting;

import static org.junit.Assert.assertTrue;

/**
 * @author Peter Abeles
 */
public class GeneralSparseSampleTests <T extends ImageSingleBand>
	extends GeneralSparseOperatorTests<T>
{
	SparseImageSample<T> alg;

	protected GeneralSparseSampleTests(Class<T> inputType,
									   SparseImageSample<T> alg ) {
		super(inputType);
		this.alg = alg;
	}

	public void performAllTests() {
		testSubImage();
		isInBounds(alg);
	}

	/**
	 * Provide a sub-image as an input image and see if it produces the expected results
	 */
	public void testSubImage() {

		alg.setImage(input);
		double expected = alg.compute(width / 2, height / 2);

		T subImage = BoofTesting.createSubImageOf(input);
		alg.setImage(subImage);
		double found = alg.compute(width/2,height/2);

		assertTrue(expected==found);
	}
}
