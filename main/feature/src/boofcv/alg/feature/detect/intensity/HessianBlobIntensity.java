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

package boofcv.alg.feature.detect.intensity;

import boofcv.alg.InputSanityCheck;
import boofcv.alg.feature.detect.intensity.impl.ImplHessianBlobIntensity;
import boofcv.struct.image.ImageFloat32;
import boofcv.struct.image.ImageSInt16;

/**
 * <p>
 * Detects "blob" intensity using the image's second derivative.  The Hessian (second derivative)
 * matrix is defined as [ I<sub>xx</sub> , I<sub>xy</sub> ; I<sub>xy</sub> , I<sub>yy</sub>],
 * where the subscript indicates a partial derivative of the input image.  The trace and determinant of this matrix
 * is commonly used to detect interest point intensities.  These tend to be at a peak for blobs
 * and circular type objects.  The trace is commonly referred to as the Laplacian.
 * </p>
 *
 * <p>
 * <ul>
 * <li>Determinant: D<sub>xx</sub>*D<sub>yy</sub> + D<sub>xy</sub><sup>2</sup></li>
 * <li>Trace: |D<sub>xx</sub> + D<sub>yy</sub>|</li>
 * </ul>
 * </p>
 *
 * @author Peter Abeles
 */
public class HessianBlobIntensity {

	/**
	 * Different types of Hessian blob detectors
	 */
	public static enum Type
	{
		DETERMINANT,
		TRACE,
//		QUICK // todo add this in the future.  Compute using 3x3 convolution kernel from raw input image
	}

	/**
	 * Feature intensity using the Hessian matrix's determinant.
	 *
	 * @param featureIntensity Output feature intensity. Modified.
	 * @param hessianXX Second derivative along x-axis. Not modified.
	 * @param hessianYY Second derivative along y-axis. Not modified.
	 * @param hessianXY Second derivative along x-axis and y-axis. Not modified.
	 */
	public static void determinant( ImageFloat32 featureIntensity , ImageFloat32 hessianXX, ImageFloat32 hessianYY , ImageFloat32 hessianXY )
	{
		InputSanityCheck.checkSameShape(featureIntensity,hessianXX,hessianYY,hessianXY);

		ImplHessianBlobIntensity.determinant(featureIntensity,hessianXX,hessianYY,hessianXY);
	}

	/**
	 * Feature intensity using the trace of the Hessian matrix.  This is also known as the Laplacian.
	 *
	 * @param featureIntensity Output feature intensity. Modified.
	 * @param hessianXX Second derivative along x-axis. Not modified.
	 * @param hessianYY Second derivative along y-axis. Not modified.
	 */
	public static void trace( ImageFloat32 featureIntensity , ImageFloat32 hessianXX, ImageFloat32 hessianYY )
	{
		InputSanityCheck.checkSameShape(featureIntensity,hessianXX,hessianYY);

		ImplHessianBlobIntensity.trace(featureIntensity,hessianXX,hessianYY);
	}

	/**
	 * Feature intensity using the Hessian matrix's determinant.
	 *
	 * @param featureIntensity Output feature intensity. Modified.
	 * @param hessianXX Second derivative along x-axis. Not modified.
	 * @param hessianYY Second derivative along y-axis. Not modified.
	 * @param hessianXY Second derivative along x-axis and y-axis. Not modified.
	 */
	public static void determinant( ImageFloat32 featureIntensity , ImageSInt16 hessianXX, ImageSInt16 hessianYY , ImageSInt16 hessianXY )
	{
		InputSanityCheck.checkSameShape(featureIntensity,hessianXX,hessianYY,hessianXY);

		ImplHessianBlobIntensity.determinant(featureIntensity,hessianXX,hessianYY,hessianXY);
	}

	/**
	 * Feature intensity using the trace of the Hessian matrix.  This is also known as the Laplacian.
	 *
	 * @param featureIntensity Output feature intensity. Modified.
	 * @param hessianXX Second derivative along x-axis. Not modified.
	 * @param hessianYY Second derivative along y-axis. Not modified.
	 */
	public static void trace( ImageFloat32 featureIntensity , ImageSInt16 hessianXX, ImageSInt16 hessianYY )
	{
		InputSanityCheck.checkSameShape(featureIntensity,hessianXX,hessianYY);

		ImplHessianBlobIntensity.trace(featureIntensity,hessianXX,hessianYY);
	}
}
