/*
 * Copyright (c) 2011-2013, Peter Abeles. All Rights Reserved.
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

package boofcv.struct.distort;

import georegression.struct.point.Point2D_F64;

/**
 * Applies a transform to a 2D point.
 *
 * @author Peter Abeles
 */
public interface PointTransform_F64 {

	/**
	 * Applies transformation
	 *
	 * @param x x-coordinate of point
	 * @param y y-coordinate of point
	 * @param out Transformed point location.
	 */
	public void compute(double x, double y, Point2D_F64 out);
}
