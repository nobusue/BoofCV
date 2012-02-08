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

import boofcv.misc.AutoTypeImage;
import boofcv.misc.CodeGeneratorBase;

import java.io.FileNotFoundException;

/**
 * @author Peter Abeles
 */
public class GenerateImplSsdCorner extends CodeGeneratorBase  {
	String className;
	String typeInput;
	String typeOutput;
	String dataInput;
	String dataOutput;
	String sumType;

	@Override
	public void generate() throws FileNotFoundException {
		createFile(AutoTypeImage.F32,AutoTypeImage.F32);
		createFile(AutoTypeImage.S16,AutoTypeImage.S32);
	}

	public void createFile( AutoTypeImage input , AutoTypeImage output ) throws FileNotFoundException {
		className = "ImplSsdCorner_"+input.getAbbreviatedType();

		typeInput = input.getImageName();
		typeOutput = output.getImageName();
		dataInput = input.getDataType();
		dataOutput = output.getDataType();
		sumType = input.getSumType();

		printPreamble();
		printProcess();
		printHorizontal();
		printVertical();

		out.println("}");
	}

	private void printPreamble() throws FileNotFoundException {
		setOutputFile(className);
		out.print("import boofcv.alg.feature.detect.intensity.GradientCornerIntensity;\n" +
				"import boofcv.struct.image." + typeInput + ";\n");
		if (typeInput.compareTo(typeOutput) != 0)
			out.print("import boofcv.struct.image." + typeOutput + ";\n");
		if( typeInput.compareTo("ImageFloat32") != 0 && typeOutput.compareTo("ImageFloat32") != 0 ) {
			out.print("import boofcv.struct.image.ImageFloat32;\n");
		}

		out.print("\n" +
				"\n" +
				"/**\n" +
				" * <p>\n" +
				" * Several corner detector algorithms work by computing a symmetric matrix whose elements are composed of the convolution\n" +
				" * of the image's gradient squared.  This is done for X*X, X*Y, and X*X.  Once the matrix has been constructed\n" +
				" * it is used to estimate how corner like the pixel under consideration is.  This class provides a generalized\n" +
				" * interface for performing these calculations in an optimized manor.\n" +
				" * </p>\n" +
				" * \n" +
				" * <p>\n" +
				" * NOTE: Image borders are not processed.  The zeros in the image border need to be taken in account when\n" +
				" * extract features using algorithms such as non-max suppression.\n" +
				" * </p>\n" +
				" * \n" +
				" * <p>\n" +
				" * DO NOT MODIFY.  Code has been automatically generated by {@link GenerateImplSsdCorner}.\n" +
				" * </p>\n" +
				" *\n" +
				" * @author Peter Abeles\n" +
				" */\n" +
				"public abstract class "+className+" implements GradientCornerIntensity<"+typeInput+"> {\n" +
				"\n" +
				"\t// input image gradient\n" +
				"\tprotected " + typeInput + " derivX;\n" +
				"\tprotected " + typeInput + " derivY;\n" +
				"\n" +
				"\t// radius of detected features\n" +
				"\tprotected int radius;\n" +
				"\n" +
				"\t// temporary storage for intensity derivatives summations\n" +
				"\tprivate " + typeOutput + " horizXX = new "+typeOutput+"(1,1);\n" +
				"\tprivate " + typeOutput + " horizXY = new "+typeOutput+"(1,1);\n" +
				"\tprivate " + typeOutput + " horizYY = new "+typeOutput+"(1,1);\n" +
				"\n" +
				"\t// temporary storage for convolution along in the vertical axis.\n" +
				"\tprivate " + sumType + " tempXX[] = new "+sumType+"[1];\n" +
				"\tprivate " + sumType + " tempXY[] = new "+sumType+"[1];\n" +
				"\tprivate " + sumType + " tempYY[] = new "+sumType+"[1];\n" +
				"\n" +
				"\t// the intensity of the found features in the image\n" +
				"\tprivate ImageFloat32 featureIntensity = new ImageFloat32(1,1);\n" +
				"\n" +
				"\t// defines the A matrix, from which the eignevalues are computed\n" +
				"\tprotected " + sumType + " totalXX, totalYY, totalXY;\n" +
				"\n" +
				"\t// used to keep track of where it is in the image\n" +
				"\tprotected int x, y;\n" +
				"\n" +
				"\tpublic "+className+"( int windowRadius) {\n" +
				"\t\tthis.radius = windowRadius;\n" +
				"\t}\n" +
				"\n" +
				"\tpublic void setImageShape( int imageWidth, int imageHeight ) {\n" +
				"\t\thorizXX.reshape(imageWidth,imageHeight);\n" +
				"\t\thorizYY.reshape(imageWidth,imageHeight);\n" +
				"\t\thorizXY.reshape(imageWidth,imageHeight);\n" +
				"\n" +
				"\t\tfeatureIntensity.reshape(imageWidth,imageHeight);\n" +
				"\n" +
				"\t\tif( tempXX.length < imageWidth ) {\n" +
				"\t\t\ttempXX = new "+sumType+"[imageWidth];\n" +
				"\t\t\ttempXY = new "+sumType+"[imageWidth];\n" +
				"\t\t\ttempYY = new "+sumType+"[imageWidth];\n" +
				"\t\t}\n" +
				"\t}\n"+
				"\n" +
				"\t@Override\n" +
				"\tpublic int getCanonicalRadius() {\n" +
				"\t\treturn radius;\n" +
				"\t}\n" +
				"\n" +
				"\t@Override\n" +
				"\tpublic ImageFloat32 getIntensity() {\n" +
				"\t\treturn featureIntensity;\n" +
				"\t}\n" +
				"\t\n" +
				"\t/**\n" +
				"\t * Computes the pixel's corner intensity.\n" +
				"\t * @return corner intensity.\n" +
				"\t */\n" +
				"\tprotected abstract float computeIntensity();\n" +
				"\n" +
				"\t@Override\n" +
				"\tpublic int getIgnoreBorder() {\n" +
				"\t\treturn radius;\n" +
				"\t}\n" +
				"\n");
	}

	public void printProcess() {
		out.print("\t@Override\n" +
				"\tpublic void process("+typeInput+" derivX, "+typeInput+" derivY) {\n" +
				"\t\tif( tempXX == null ) {\n" +
				"\t\t\tif (derivX.getWidth() != derivY.getWidth() || derivX.getHeight() != derivY.getHeight()) {\n" +
				"\t\t\t\tthrow new IllegalArgumentException(\"Input image sizes do not match\");\n" +
				"\t\t\t}\n" +
				"\t\t\tsetImageShape(derivX.getWidth(),derivX.getHeight());\n" +
				"\t\t} else if (derivX.getWidth() != horizXX.getWidth() || derivX.getHeight() != horizXX.getHeight()) {\n" +
				"\t\t\tsetImageShape(derivX.getWidth(),derivX.getHeight());\n" +
				"\t\t}\n" +
				"\t\tthis.derivX = derivX;\n" +
				"\t\tthis.derivY = derivY;\n" +
				"\n" +
				"\t\thorizontal();\n" +
				"\t\tvertical();\n" +
				"\t}\n\n");
	}

	protected void printHorizontal() {
		out.print("/**\n" +
				"\t * Compute the derivative sum along the x-axis while taking advantage of duplicate\n" +
				"\t * calculations for each window.\n" +
				"\t */\n" +
				"\tprivate void horizontal() {\n" +
				"\t\t" + dataInput + "[] dataX = derivX.data;\n" +
				"\t\t" + dataInput + "[] dataY = derivY.data;\n" +
				"\n" +
				"\t\t" + dataOutput + "[] hXX = horizXX.data;\n" +
				"\t\t" + dataOutput + "[] hXY = horizXY.data;\n" +
				"\t\t" + dataOutput + "[] hYY = horizYY.data;\n" +
				"\n" +
				"\t\tfinal int imgHeight = derivX.getHeight();\n" +
				"\t\tfinal int imgWidth = derivX.getWidth();\n" +
				"\n" +
				"\t\tint windowWidth = radius * 2 + 1;\n" +
				"\n" +
				"\t\tint radp1 = radius + 1;\n" +
				"\n" +
				"\t\tfor (int row = 0; row < imgHeight; row++) {\n" +
				"\n" +
				"\t\t\tint pix = row * imgWidth;\n" +
				"\t\t\tint end = pix + windowWidth;\n" +
				"\n" +
				"\t\t\t" + sumType + " totalXX = 0;\n" +
				"\t\t\t" + sumType + " totalXY = 0;\n" +
				"\t\t\t" + sumType + " totalYY = 0;\n" +
				"\n" +
				"\t\t\tint indexX = derivX.startIndex + row * derivX.stride;\n" +
				"\t\t\tint indexY = derivY.startIndex + row * derivY.stride;\n" +
				"\n" +
				"\t\t\tfor (; pix < end; pix++) {\n" +
				"\t\t\t\t"+dataInput+" dx = dataX[indexX++];\n" +
				"\t\t\t\t"+dataInput+" dy = dataY[indexY++];\n" +
				"\n" +
				"\t\t\t\ttotalXX += dx * dx;\n" +
				"\t\t\t\ttotalXY += dx * dy;\n" +
				"\t\t\t\ttotalYY += dy * dy;\n" +
				"\t\t\t}\n" +
				"\n" +
				"\t\t\thXX[pix - radp1] = totalXX;\n" +
				"\t\t\thXY[pix - radp1] = totalXY;\n" +
				"\t\t\thYY[pix - radp1] = totalYY;\n" +
				"\n" +
				"\t\t\tend = row * imgWidth + imgWidth;\n" +
				"\t\t\tfor (; pix < end; pix++, indexX++, indexY++) {\n" +
				"\n" +
				"\t\t\t\t"+dataInput+" dx = dataX[indexX - windowWidth];\n" +
				"\t\t\t\t"+dataInput+" dy = dataY[indexY - windowWidth];\n" +
				"\n" +
				"\t\t\t\t// saving these multiplications in an array to avoid recalculating them made\n" +
				"\t\t\t\t// the algorithm about 50% slower\n" +
				"\t\t\t\ttotalXX -= dx * dx;\n" +
				"\t\t\t\ttotalXY -= dx * dy;\n" +
				"\t\t\t\ttotalYY -= dy * dy;\n" +
				"\n" +
				"\t\t\t\tdx = dataX[indexX];\n" +
				"\t\t\t\tdy = dataY[indexY];\n" +
				"\n" +
				"\t\t\t\ttotalXX += dx * dx;\n" +
				"\t\t\t\ttotalXY += dx * dy;\n" +
				"\t\t\t\ttotalYY += dy * dy;\n" +
				"\n" +
				"\t\t\t\thXX[pix - radius] = totalXX;\n" +
				"\t\t\t\thXY[pix - radius] = totalXY;\n" +
				"\t\t\t\thYY[pix - radius] = totalYY;\n" +
				"\t\t\t}\n" +
				"\t\t}\n" +
				"\t}\n\n");
	}

	public void printVertical() {
		out.print("\t/**\n" +
				"\t * Compute the derivative sum along the y-axis while taking advantage of duplicate\n" +
				"\t * calculations for each window and avoiding cache misses. Then compute the eigen values\n" +
				"\t */\n" +
				"\tprivate void vertical() {\n" +
				"\t\t"+sumType+"[] hXX = horizXX.data;\n" +
				"\t\t"+sumType+"[] hXY = horizXY.data;\n" +
				"\t\t"+sumType+"[] hYY = horizYY.data;\n" +
				"\t\tfinal float[] inten = featureIntensity.data;\n" +
				"\n" +
				"\t\tfinal int imgHeight = horizXX.getHeight();\n" +
				"\t\tfinal int imgWidth = horizXX.getWidth();\n" +
				"\n" +
				"\t\tfinal int kernelWidth = radius * 2 + 1;\n" +
				"\n" +
				"\t\tfinal int startX = radius;\n" +
				"\t\tfinal int endX = imgWidth - radius;\n" +
				"\n" +
				"\t\tfinal int backStep = kernelWidth * imgWidth;\n" +
				"\n" +
				"\t\tfor (x = startX; x < endX; x++) {\n" +
				"\t\t\tint srcIndex = x;\n" +
				"\t\t\tint destIndex = imgWidth * radius + x;\n" +
				"\t\t\ttotalXX = totalXY = totalYY = 0;\n" +
				"\n" +
				"\t\t\tint indexEnd = srcIndex + imgWidth * kernelWidth;\n" +
				"\t\t\tfor (; srcIndex < indexEnd; srcIndex += imgWidth) {\n" +
				"\t\t\t\ttotalXX += hXX[srcIndex];\n" +
				"\t\t\t\ttotalXY += hXY[srcIndex];\n" +
				"\t\t\t\ttotalYY += hYY[srcIndex];\n" +
				"\t\t\t}\n" +
				"\n" +
				"\t\t\ttempXX[x] = totalXX;\n" +
				"\t\t\ttempXY[x] = totalXY;\n" +
				"\t\t\ttempYY[x] = totalYY;\n" +
				"\n" +
				"\t\t\ty = radius;\n" +
				"\t\t\t// compute the eigen values\n" +
				"\t\t\tinten[destIndex] = computeIntensity();\n" +
				"\t\t\tdestIndex += imgWidth;\n" +
				"\t\t\ty++;\n" +
				"\t\t}\n" +
				"\n" +
				"\t\t// change the order it is processed in to reduce cache misses\n" +
				"\t\tfor (y = radius + 1; y < imgHeight - radius; y++) {\n" +
				"\t\t\tint srcIndex = (y + radius) * imgWidth + startX;\n" +
				"\t\t\tint destIndex = y * imgWidth + startX;\n" +
				"\n" +
				"\t\t\tfor (x = startX; x < endX; x++, srcIndex++, destIndex++) {\n" +
				"\t\t\t\ttotalXX = tempXX[x] - hXX[srcIndex - backStep];\n" +
				"\t\t\t\ttempXX[x] = totalXX += hXX[srcIndex];\n" +
				"\t\t\t\ttotalXY = tempXY[x] - hXY[srcIndex - backStep];\n" +
				"\t\t\t\ttempXY[x] = totalXY += hXY[srcIndex];\n" +
				"\t\t\t\ttotalYY = tempYY[x] - hYY[srcIndex - backStep];\n" +
				"\t\t\t\ttempYY[x] = totalYY += hYY[srcIndex];\n" +
				"\n" +
				"\t\t\t\tinten[destIndex] = computeIntensity();\n" +
				"\t\t\t}\n" +
				"\t\t}\n" +
				"\t}\n");
	}

	public static void main( String args[] ) throws FileNotFoundException {
		GenerateImplSsdCorner gen = new GenerateImplSsdCorner();

		gen.generate();
	}
}
