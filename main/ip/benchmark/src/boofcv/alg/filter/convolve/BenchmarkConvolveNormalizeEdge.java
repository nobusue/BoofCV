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

package boofcv.alg.filter.convolve;

import boofcv.alg.filter.convolve.normalized.ConvolveNormalizedNaive;
import boofcv.alg.misc.ImageTestingOps;
import boofcv.factory.filter.kernel.FactoryKernelGaussian;
import boofcv.struct.convolve.Kernel1D_F32;
import boofcv.struct.convolve.Kernel1D_I32;
import boofcv.struct.convolve.Kernel2D_F32;
import boofcv.struct.convolve.Kernel2D_I32;
import boofcv.struct.image.ImageFloat32;
import boofcv.struct.image.ImageSInt16;
import boofcv.struct.image.ImageSInt32;
import boofcv.struct.image.ImageUInt8;
import com.google.caliper.Param;
import com.google.caliper.Runner;
import com.google.caliper.SimpleBenchmark;

import java.util.Random;

/**
 * Benchmark for different convolution which renormalize along the image edges.
 *
 * @author Peter Abeles
 */
@SuppressWarnings({"UnusedDeclaration"})
public class BenchmarkConvolveNormalizeEdge extends SimpleBenchmark {
	static private int imgWidth = 640;
	static private int imgHeight = 480;

	static private Kernel2D_F32 kernel2D_F32;
	static private Kernel1D_F32 kernelF32;
	static private ImageFloat32 imgFloat32;
	static private ImageFloat32 out_F32;
	static private Kernel1D_I32 kernelI32;
	static private Kernel2D_I32 kernel2D_I32;
	static private ImageUInt8 imgInt8;
	static private ImageSInt16 imgInt16;
	static private ImageUInt8 out_I8;
	static private ImageSInt16 out_I16;
	static private ImageSInt32 out_I32;

	// iterate through different sized kernel radius
	@Param({"1", "2", "3", "5","10"}) private int radius;

	public BenchmarkConvolveNormalizeEdge() {
		imgInt8 = new ImageUInt8(imgWidth,imgHeight);
		imgInt16 = new ImageSInt16(imgWidth,imgHeight);
		out_I32 = new ImageSInt32(imgWidth,imgHeight);
		out_I16 = new ImageSInt16(imgWidth,imgHeight);
		out_I8 = new ImageUInt8(imgWidth,imgHeight);
		imgFloat32 = new ImageFloat32(imgWidth,imgHeight);
		out_F32 = new ImageFloat32(imgWidth,imgHeight);

		Random rand = new Random(234234);
		ImageTestingOps.randomize(imgInt8,rand, 0, 100);
		ImageTestingOps.randomize(imgInt16,rand,0,200);
		ImageTestingOps.randomize(imgFloat32,rand,0,200);
	}

	@Override protected void setUp() throws Exception {
		kernelF32 = FactoryKernelGaussian.gaussian(Kernel1D_F32.class, -1, radius);
		kernelI32 = FactoryKernelGaussian.gaussian(Kernel1D_I32.class, -1, radius);
		kernel2D_F32 = FactoryKernelGaussian.gaussian(Kernel2D_F32.class,-1,radius);
		kernel2D_I32 = FactoryKernelGaussian.gaussian(Kernel2D_I32.class, -1, radius);
	}

	public int timeHorizontal_Naive_F32(int reps) {
		for( int i = 0; i < reps; i++ )
			ConvolveNormalizedNaive.horizontal(kernelF32, imgFloat32, out_F32);
		return 0;
	}

	public int timeHorizontal_F32(int reps) {
		for( int i = 0; i < reps; i++ )
			ConvolveNormalized.horizontal(kernelF32,imgFloat32,out_F32);
		return 0;
	}

	public int timeHorizontal_I8(int reps) {
		for( int i = 0; i < reps; i++ )
			ConvolveNormalized.horizontal(kernelI32,imgInt8,out_I8);
		return 0;
	}

	public int timeHorizontal_I16(int reps) {
		for( int i = 0; i < reps; i++ )
			ConvolveNormalized.horizontal(kernelI32,imgInt16,out_I16);
		return 0;
	}

	public int timeVertical_F32(int reps) {
		for( int i = 0; i < reps; i++ )
			ConvolveNormalized.vertical(kernelF32, imgFloat32, out_F32);
		return 0;
	}

	public int timeVertical_I8(int reps) {
		for( int i = 0; i < reps; i++ )
			ConvolveNormalized.vertical(kernelI32,imgInt8,out_I8);
		return 0;
	}

	public int timeVertical_I16(int reps) {
		for( int i = 0; i < reps; i++ )
			ConvolveNormalized.vertical(kernelI32,imgInt16,out_I16);
		return 0;
	}

	public static void main( String args[] ) {
		System.out.println("=========  Profile Image Size "+ imgWidth +" x "+ imgHeight +" ==========");

		Runner.main(BenchmarkConvolveMean.class, args);
	}
}
