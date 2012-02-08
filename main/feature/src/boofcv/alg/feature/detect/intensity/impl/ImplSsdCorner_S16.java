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

package boofcv.alg.feature.detect.intensity.impl;

import boofcv.alg.feature.detect.intensity.GradientCornerIntensity;
import boofcv.struct.image.ImageFloat32;
import boofcv.struct.image.ImageSInt16;
import boofcv.struct.image.ImageSInt32;


/**
 * <p>
 * Several corner detector algorithms work by computing a symmetric matrix whose elements are composed of the convolution
 * of the image's gradient squared.  This is done for X*X, X*Y, and X*X.  Once the matrix has been constructed
 * it is used to estimate how corner like the pixel under consideration is.  This class provides a generalized
 * interface for performing these calculations in an optimized manor.
 * </p>
 * 
 * <p>
 * NOTE: Image borders are not processed.  The zeros in the image border need to be taken in account when
 * extract features using algorithms such as non-max suppression.
 * </p>
 * 
 * <p>
 * DO NOT MODIFY.  Code has been automatically generated by {@link GenerateImplSsdCorner}.
 * </p>
 *
 * @author Peter Abeles
 */
public abstract class ImplSsdCorner_S16 implements GradientCornerIntensity<ImageSInt16> {

	// input image gradient
	protected ImageSInt16 derivX;
	protected ImageSInt16 derivY;

	// radius of detected features
	protected int radius;

	// temporary storage for intensity derivatives summations
	private ImageSInt32 horizXX = new ImageSInt32(1,1);
	private ImageSInt32 horizXY = new ImageSInt32(1,1);
	private ImageSInt32 horizYY = new ImageSInt32(1,1);

	// temporary storage for convolution along in the vertical axis.
	private int tempXX[] = new int[1];
	private int tempXY[] = new int[1];
	private int tempYY[] = new int[1];

	// the intensity of the found features in the image
	private ImageFloat32 featureIntensity = new ImageFloat32(1,1);

	// defines the A matrix, from which the eignevalues are computed
	protected int totalXX, totalYY, totalXY;

	// used to keep track of where it is in the image
	protected int x, y;

	public ImplSsdCorner_S16( int windowRadius) {
		this.radius = windowRadius;
	}

	public void setImageShape( int imageWidth, int imageHeight ) {
		horizXX.reshape(imageWidth,imageHeight);
		horizYY.reshape(imageWidth,imageHeight);
		horizXY.reshape(imageWidth,imageHeight);

		featureIntensity.reshape(imageWidth,imageHeight);

		if( tempXX.length < imageWidth ) {
			tempXX = new int[imageWidth];
			tempXY = new int[imageWidth];
			tempYY = new int[imageWidth];
		}
	}

	@Override
	public int getRadius() {
		return radius;
	}

	@Override
	public ImageFloat32 getIntensity() {
		return featureIntensity;
	}
	
	/**
	 * Computes the pixel's corner intensity.
	 * @return corner intensity.
	 */
	protected abstract float computeIntensity();

	@Override
	public int getIgnoreBorder() {
		return radius;
	}

	@Override
	public void process(ImageSInt16 derivX, ImageSInt16 derivY) {
		if( tempXX == null ) {
			if (derivX.getWidth() != derivY.getWidth() || derivX.getHeight() != derivY.getHeight()) {
				throw new IllegalArgumentException("Input image sizes do not match");
			}
			setImageShape(derivX.getWidth(),derivX.getHeight());
		} else if (derivX.getWidth() != horizXX.getWidth() || derivX.getHeight() != horizXX.getHeight()) {
			setImageShape(derivX.getWidth(),derivX.getHeight());
		}
		this.derivX = derivX;
		this.derivY = derivY;

		horizontal();
		vertical();
	}

/**
	 * Compute the derivative sum along the x-axis while taking advantage of duplicate
	 * calculations for each window.
	 */
	private void horizontal() {
		short[] dataX = derivX.data;
		short[] dataY = derivY.data;

		int[] hXX = horizXX.data;
		int[] hXY = horizXY.data;
		int[] hYY = horizYY.data;

		final int imgHeight = derivX.getHeight();
		final int imgWidth = derivX.getWidth();

		int windowWidth = radius * 2 + 1;

		int radp1 = radius + 1;

		for (int row = 0; row < imgHeight; row++) {

			int pix = row * imgWidth;
			int end = pix + windowWidth;

			int totalXX = 0;
			int totalXY = 0;
			int totalYY = 0;

			int indexX = derivX.startIndex + row * derivX.stride;
			int indexY = derivY.startIndex + row * derivY.stride;

			for (; pix < end; pix++) {
				short dx = dataX[indexX++];
				short dy = dataY[indexY++];

				totalXX += dx * dx;
				totalXY += dx * dy;
				totalYY += dy * dy;
			}

			hXX[pix - radp1] = totalXX;
			hXY[pix - radp1] = totalXY;
			hYY[pix - radp1] = totalYY;

			end = row * imgWidth + imgWidth;
			for (; pix < end; pix++, indexX++, indexY++) {

				short dx = dataX[indexX - windowWidth];
				short dy = dataY[indexY - windowWidth];

				// saving these multiplications in an array to avoid recalculating them made
				// the algorithm about 50% slower
				totalXX -= dx * dx;
				totalXY -= dx * dy;
				totalYY -= dy * dy;

				dx = dataX[indexX];
				dy = dataY[indexY];

				totalXX += dx * dx;
				totalXY += dx * dy;
				totalYY += dy * dy;

				hXX[pix - radius] = totalXX;
				hXY[pix - radius] = totalXY;
				hYY[pix - radius] = totalYY;
			}
		}
	}

	/**
	 * Compute the derivative sum along the y-axis while taking advantage of duplicate
	 * calculations for each window and avoiding cache misses. Then compute the eigen values
	 */
	private void vertical() {
		int[] hXX = horizXX.data;
		int[] hXY = horizXY.data;
		int[] hYY = horizYY.data;
		final float[] inten = featureIntensity.data;

		final int imgHeight = horizXX.getHeight();
		final int imgWidth = horizXX.getWidth();

		final int kernelWidth = radius * 2 + 1;

		final int startX = radius;
		final int endX = imgWidth - radius;

		final int backStep = kernelWidth * imgWidth;

		for (x = startX; x < endX; x++) {
			int srcIndex = x;
			int destIndex = imgWidth * radius + x;
			totalXX = totalXY = totalYY = 0;

			int indexEnd = srcIndex + imgWidth * kernelWidth;
			for (; srcIndex < indexEnd; srcIndex += imgWidth) {
				totalXX += hXX[srcIndex];
				totalXY += hXY[srcIndex];
				totalYY += hYY[srcIndex];
			}

			tempXX[x] = totalXX;
			tempXY[x] = totalXY;
			tempYY[x] = totalYY;

			y = radius;
			// compute the eigen values
			inten[destIndex] = computeIntensity();
			destIndex += imgWidth;
			y++;
		}

		// change the order it is processed in to reduce cache misses
		for (y = radius + 1; y < imgHeight - radius; y++) {
			int srcIndex = (y + radius) * imgWidth + startX;
			int destIndex = y * imgWidth + startX;

			for (x = startX; x < endX; x++, srcIndex++, destIndex++) {
				totalXX = tempXX[x] - hXX[srcIndex - backStep];
				tempXX[x] = totalXX += hXX[srcIndex];
				totalXY = tempXY[x] - hXY[srcIndex - backStep];
				tempXY[x] = totalXY += hXY[srcIndex];
				totalYY = tempYY[x] - hYY[srcIndex - backStep];
				tempYY[x] = totalYY += hYY[srcIndex];

				inten[destIndex] = computeIntensity();
			}
		}
	}
}
