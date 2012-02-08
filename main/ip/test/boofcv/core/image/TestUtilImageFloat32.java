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

package boofcv.core.image;

import boofcv.alg.misc.ImageTestingOps;
import boofcv.struct.image.ImageFloat32;
import boofcv.testing.BoofTesting;
import org.junit.Test;

import java.util.Random;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Peter Abeles
 */
public class TestUtilImageFloat32 {

	Random rand = new Random(234234);

	@Test
	public void fill() {
		ImageFloat32 image = new ImageFloat32(10, 20);

		BoofTesting.checkSubImage(this, "checkFill", true, image);
	}

	public void checkFill(ImageFloat32 image) {
		ImageTestingOps.fill(image, 1.1f);

		for (int y = 0; y < image.getHeight(); y++) {
			for (int x = 0; x < image.getWidth(); x++) {
				assertEquals(image.get(x, y), 1.1f);
			}
		}
	}

	@Test
	public void randomize() {
		ImageFloat32 image = new ImageFloat32(10, 20);

		BoofTesting.checkSubImage(this, "checkRandomize", false, image);
	}

	public void checkRandomize(ImageFloat32 image) {
		ImageTestingOps.randomize(image, rand, -20f, 20f);

		for (int y = 0; y < image.getHeight(); y++) {
			for (int x = 0; x < image.getWidth(); x++) {
				assertTrue(image.get(x, y) != 0.0);
			}
		}
	}
}
