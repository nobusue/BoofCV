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

package boofcv.alg.filter.basic.impl;

import boofcv.misc.AutoTypeImage;
import boofcv.misc.CodeGeneratorBase;
import boofcv.misc.CodeGeneratorUtil;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;


/**
 * @author Peter Abeles
 */
public class GenerateImplGrayImageOps extends CodeGeneratorBase {
	String className = "ImplGrayImageOps";

	PrintStream out;
	AutoTypeImage imageType;
	String sumType;
	String typeCast;
	String bitWise;

	public GenerateImplGrayImageOps() throws FileNotFoundException {
		out = new PrintStream(new FileOutputStream(className + ".java"));
	}

	@Override
	public void generate() throws FileNotFoundException {
		printPreamble();

		printAll(AutoTypeImage.F32);
		printAll(AutoTypeImage.U8);
		printAll(AutoTypeImage.S16);
		printAll(AutoTypeImage.U16);
		printAll(AutoTypeImage.S32);

		out.print("\n" +
				"}\n");
	}

	private void printPreamble() {
		out.print(CodeGeneratorUtil.copyright);
		out.print("package boofcv.alg.filter.basic.impl;\n" +
				"\n" +
				"import boofcv.struct.image.*;\n" +
				"\n" +
				"/**\n" +
				" * <p>\n" +
				" * Contains implementations of algorithms in {@link boofcv.alg.filter.basic.GrayImageOps}.\n" +
				" * </p>\n" +
				" * \n" +
				" * <p>\n" +
				" * WARNING: Do not modify.  Automatically generated by {@link GenerateImplGrayImageOps}.\n" +
				" * </p>\n" +
				" */\n" +
				"public class "+className+" {\n\n");

	}

	public void printAll( AutoTypeImage imageType ) {
		this.imageType = imageType;
		typeCast = imageType.getTypeCastFromSum();
		sumType = imageType.getSumType();
		bitWise = imageType.getBitWise();

		printInvert();
		printBrighten();
		printStretch();
	}

	private void printInvert() {
		out.print("\tpublic static void invert("+imageType.getImageName()+" input, "+sumType+" max , "+imageType.getImageName()+" output) {\n" +
				"\n" +
				"\t\tfor (int y = 0; y < input.height; y++) {\n" +
				"\t\t\tint indexSrc = input.startIndex + input.stride*y;\n" +
				"\t\t\tint indexDst = output.startIndex + output.stride*y;\n" +
				"\n" +
				"\t\t\tfor (int x = 0; x < input.width; x++) {\n" +
				"\t\t\t\toutput.data[indexDst++] = "+typeCast+"(max - (input.data[indexSrc++]"+bitWise+"));\n" +
				"\t\t\t}\n" +
				"\t\t}\n" +
				"\t}\n\n");
	}

	private void printBrighten() {
		out.print("\tpublic static void brighten("+imageType.getImageName()+" input, "+sumType+" beta, "+sumType+" max , "+imageType.getImageName()+" output ) {\n" +
				"\n" +
				"\t\tfor (int y = 0; y < input.height; y++) {\n" +
				"\t\t\tint indexSrc = input.startIndex + input.stride*y;\n" +
				"\t\t\tint indexDst = output.startIndex + output.stride*y;\n" +
				"\n" +
				"\t\t\tfor (int x = 0; x < input.width; x++) {\n" +
				"\t\t\t\t"+sumType+" val = (input.data[indexSrc++]"+bitWise+") + beta;\n" +
				"\t\t\t\tif (val > max) val = max;\n" +
				"\t\t\t\tif (val < 0) val = 0;\n" +
				"\t\t\t\toutput.data[indexDst++] = "+typeCast+"val;\n" +
				"\t\t\t}\n" +
				"\t\t}\n" +
				"\t}\n\n");
	}

	private void printStretch() {
		out.print("\tpublic static void stretch("+imageType.getImageName()+" input, double gamma, "+sumType+" beta, "+sumType+" max , "+imageType.getImageName()+" output ) {\n" +
				"\t\tfor (int y = 0; y < input.height; y++) {\n" +
				"\t\t\tint indexSrc = input.startIndex + input.stride*y;\n" +
				"\t\t\tint indexDst = output.startIndex + output.stride*y;\n" +
				"\n" +
				"\t\t\tfor (int x = 0; x < input.width; x++) {\n" +
				"\t\t\t\t"+sumType+" val = ("+sumType+")((input.data[indexSrc++]"+bitWise+")* gamma) + beta;\n" +
				"\t\t\t\tif (val > max) val = max;\n" +
				"\t\t\t\tif (val < 0) val = 0;\n" +
				"\t\t\t\toutput.data[indexDst++] = "+typeCast+"val;\n" +
				"\t\t\t}\n" +
				"\t\t}\n" +
				"\t}\n\n");
	}

	public static void main( String args[] ) throws FileNotFoundException {
		GenerateImplGrayImageOps app = new GenerateImplGrayImageOps();
		app.generate();
	}
}
