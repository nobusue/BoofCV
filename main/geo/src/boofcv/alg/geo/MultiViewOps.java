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

package boofcv.alg.geo;

import boofcv.struct.geo.TrifocalTensor;
import georegression.geometry.GeometryMath_F64;
import georegression.struct.point.Point2D_F64;
import georegression.struct.point.Point3D_F64;
import georegression.struct.point.Vector3D_F64;
import georegression.struct.se.Se3_F64;
import org.ejml.data.DenseMatrix64F;
import org.ejml.factory.DecompositionFactory;
import org.ejml.factory.QRDecomposition;
import org.ejml.factory.SingularValueDecomposition;
import org.ejml.ops.CommonOps;
import org.ejml.ops.SingularOps;
import org.ejml.simple.SimpleMatrix;
import org.ejml.simple.SimpleSVD;

/**
 * <p>
 * Contains commonly used operations used in 2-view and 3-view perspective geometry.
 * </p>
 *
 * <p>
 * LINES:  lines on the image place are represented in homogeneous or generic form as a 3D vector. If a point in
 * homogeneous coordinates is on a line and the dot product is computed the result will be zero.
 * </p>
 *
 * @author Peter Abeles
 */
public class MultiViewOps {

	/**
	 * Creates a trifocal tensor from two camera matrices. IMPORTANT: It is assumed that the first camera
	 * has the following camera matrix P1 = [I|0], where I is an identify matrix.
	 *
	 * @param P2 Camera matrix from view 1 to view 2
	 * @param P3 Camera matrix from view 1 to view 3
	 * @param ret Storage for trifocal tensor.  If null a new instance will be created.
	 * @return The trifocal tensor
	 */
	public static TrifocalTensor createTrifocal( DenseMatrix64F P2 , DenseMatrix64F P3 , TrifocalTensor ret ) {
		if( ret == null )
			ret = new TrifocalTensor();

		for( int col = 0; col < 3; col++ ) {
			DenseMatrix64F T = ret.getT(col);

			int index = 0;
			for( int i = 0; i < 3; i++ ) {
				for( int j = 0; j < 3; j++ ) {
					T.data[index++] = P2.get(i,col)*P3.get(j,3) - P2.get(i,3)*P3.get(j,col);
				}
			}
		}

		return ret;
	}

	/**
	 * <p>
	 * Trifocal tensor with line-line-line correspondence:<br>
	 * (l2<sup>T</sup>*[T1,T2,T3]*L2)*[l1]<sub>x</sub> = 0
	 * </p>
	 *
	 * @param tensor Trifocal tensor
	 * @param l1 A line in the first view.
	 * @param l2 A line in the second view.
	 * @param l3 A line in the third view.
	 * @param ret Storage for output.  If null a new instance will be declared.
	 * @return Result of applying the constraint.  With perfect inputs will be zero.
	 */
	public static Vector3D_F64 constraintTrifocal( TrifocalTensor tensor ,
												   Vector3D_F64 l1 , Vector3D_F64 l2 , Vector3D_F64 l3 ,
												   Vector3D_F64 ret )
	{
		if( ret == null )
			ret = new Vector3D_F64();

		double x = GeometryMath_F64.innerProd(l2, tensor.T1, l3);
		double y = GeometryMath_F64.innerProd(l2, tensor.T2, l3);
		double z = GeometryMath_F64.innerProd(l2, tensor.T3, l3);

		GeometryMath_F64.cross(new Vector3D_F64(x, y, z), l1, ret);

		return ret;
	}

	/**
	 * <p>
	 * Trifocal tensor with point-line-line correspondence:<br>
	 * (l2<sup>T</sup>*(sum p1<sup>i</sup>*T<sub>i</sub>)*l3 = 0
	 * </p>
	 *
	 * @param tensor Trifocal tensor
	 * @param p1 A point in the first view.
	 * @param l2 A line in the second view.
	 * @param l3 A line in the third view.
	 * @return Result of applying the constraint.  With perfect inputs will be zero.
	 */
	public static double constraintTrifocal( TrifocalTensor tensor ,
											 Point2D_F64 p1 , Vector3D_F64 l2 , Vector3D_F64 l3 )
	{
		DenseMatrix64F sum = new DenseMatrix64F(3,3);

		CommonOps.add(p1.x,tensor.T1,sum,sum);
		CommonOps.add(p1.y,tensor.T2,sum,sum);
		CommonOps.add(tensor.T3, sum, sum);

		return GeometryMath_F64.innerProd(l2,sum,l3);
	}

	/**
	 * <p>
	 * Trifocal tensor with point-line-point correspondence:<br>
	 * (l2<sup>T</sup>(sum p1<sup>i</sup>*T<sub>i</sub>)[p3]<sub>x</sub> = 0
	 * </p>
	 *
	 * @param tensor Trifocal tensor
	 * @param p1 A point in the first view.
	 * @param l2 A line in the second view.
	 * @param p3 A point in the third view.
	 * @return Result of applying the constraint.  With perfect inputs will be zero.
	 */
	public static Vector3D_F64 constraintTrifocal( TrifocalTensor tensor ,
												   Point2D_F64 p1 , Vector3D_F64 l2 , Point2D_F64 p3 ,
												   Vector3D_F64 ret )
	{
		if( ret == null )
			ret = new Vector3D_F64();

		DenseMatrix64F sum = new DenseMatrix64F(3,3);

		CommonOps.add(p1.x,tensor.T1,sum,sum);
		CommonOps.add(p1.y,tensor.T2,sum,sum);
		CommonOps.add(tensor.T3,sum,sum);

		Vector3D_F64 tempV = new Vector3D_F64();
		GeometryMath_F64.multTran(sum, l2, tempV);

		GeometryMath_F64.cross(tempV, new Vector3D_F64(p3.x, p3.y, 1), ret);

		return ret;
	}

	/**
	 * <p>
	 * Trifocal tensor with point-point-line correspondence:<br>
	 * [p2]<sub>x</sub>(sum p1<sup>i</sup>*T<sub>i</sub>)*l3 = 0
	 * </p>
	 *
	 * @param tensor Trifocal tensor
	 * @param p1 A point in the first view.
	 * @param p2 A point in the second view.
	 * @param l3 A line in the third view.
	 * @return Result of applying the constraint.  With perfect inputs will be zero.
	 */
	public static Vector3D_F64 constraintTrifocal( TrifocalTensor tensor ,
												   Point2D_F64 p1 , Point2D_F64 p2 , Vector3D_F64 l3 ,
												   Vector3D_F64 ret )
	{
		if( ret == null )
			ret = new Vector3D_F64();

		DenseMatrix64F sum = new DenseMatrix64F(3,3);

		CommonOps.add(p1.x,tensor.T1,sum,sum);
		CommonOps.add(p1.y,tensor.T2,sum,sum);
		CommonOps.add(tensor.T3,sum,sum);

		DenseMatrix64F cross2 = GeometryMath_F64.crossMatrix(p2.x,p2.y,1,null);

		DenseMatrix64F temp = new DenseMatrix64F(3,3);

		CommonOps.mult(cross2,sum,temp);
		GeometryMath_F64.mult(temp, l3, ret);

		return ret;
	}

	/**
	 * <p>
	 * Trifocal tensor with point-point-point correspondence:<br>
	 * [p2]<sub>x</sub>(sum p1<sup>i</sup>*T<sub>i</sub>)[p3]<sub>x</sub> = 0
	 * </p>
	 *
	 * @param tensor Trifocal tensor
	 * @param p1 A point in the first view.
	 * @param p2 A point in the second view.
	 * @param p3 A point in the third view.
	 * @param ret Optional storage for output. 3x3 matrix.  Modified.
	 * @return Result of applying the constraint.  With perfect inputs will be zero.
	 */
	public static DenseMatrix64F constraintTrifocal( TrifocalTensor tensor ,
													 Point2D_F64 p1 , Point2D_F64 p2 , Point2D_F64 p3 ,
													 DenseMatrix64F ret )
	{
		if( ret == null )
			ret = new DenseMatrix64F(3,3);

		DenseMatrix64F sum = new DenseMatrix64F(3,3);

		CommonOps.add(p1.x,tensor.T1,p1.y,tensor.T2,sum);
		CommonOps.add(sum,tensor.T3,sum);

		DenseMatrix64F cross2 = GeometryMath_F64.crossMatrix(p2.x,p2.y,1,null);
		DenseMatrix64F cross3 = GeometryMath_F64.crossMatrix(p3.x,p3.y,1,null);

		DenseMatrix64F temp = new DenseMatrix64F(3,3);

		CommonOps.mult(cross2,sum,temp);
		CommonOps.mult(temp, cross3, ret);

		return ret;
	}

	/**
	 * <p>
	 * Computes the epipoles of the first camera in the second and third images.  Epipoles are found
	 * in homogeneous coordinates.
	 * </p>
	 *
	 * <p>
	 * Properties:
	 * <ul>
	 *     <li> e2<sup>T</sup>*F12 = 0
	 *     <li> e3<sup>T</sup>*F13 = 0
	 * </ul>
	 * where F1i is a fundamental matrix from image 1 to i.
	 * </p>
	 *
	 * @param tensor Trifocal tensor.  Not Modified
	 * @param e2  Output: Epipole in image 2. Homogeneous coordinates. Modified
	 * @param e3  Output: Epipole in image 3. Homogeneous coordinates. Modified
	 */
	public static void extractEpipoles( TrifocalTensor tensor , Point3D_F64 e2 , Point3D_F64 e3 ) {
		SingularValueDecomposition<DenseMatrix64F> svd = DecompositionFactory.svd(3,3,true,true,false);
		if( svd.inputModified() ) {
			tensor = tensor.copy();
		}

		DenseMatrix64F u1 = new DenseMatrix64F(3,1);
		DenseMatrix64F u2 = new DenseMatrix64F(3,1);
		DenseMatrix64F u3 = new DenseMatrix64F(3,1);
		DenseMatrix64F v1 = new DenseMatrix64F(3,1);
		DenseMatrix64F v2 = new DenseMatrix64F(3,1);
		DenseMatrix64F v3 = new DenseMatrix64F(3,1);

		svd.decompose(tensor.T1);
		SingularOps.nullVector(svd, true, v1);
		SingularOps.nullVector(svd, false,u1);

		svd.decompose(tensor.T2);
		SingularOps.nullVector(svd,true,v2);
		SingularOps.nullVector(svd,false,u2);

		svd.decompose(tensor.T3);
		SingularOps.nullVector(svd,true,v3);
		SingularOps.nullVector(svd,false,u3);

		DenseMatrix64F U = new DenseMatrix64F(3,3);
		DenseMatrix64F V = new DenseMatrix64F(3,3);

		for( int i = 0; i < 3; i++ ) {
			U.set(i,0,u1.get(i));
			U.set(i,1,u2.get(i));
			U.set(i,2,u3.get(i));

			V.set(i, 0, v1.get(i));
			V.set(i, 1, v2.get(i));
			V.set(i, 2, v3.get(i));
		}

		DenseMatrix64F tempE = new DenseMatrix64F(3,1);

		svd.decompose(U);
		SingularOps.nullVector(svd, false, tempE);
		e2.set(tempE.get(0),tempE.get(1),tempE.get(2));

		svd.decompose(V);
		SingularOps.nullVector(svd, false, tempE);
		e3.set(tempE.get(0),tempE.get(1),tempE.get(2));
	}

	/**
	 * Extract the fundamental matrices between views 1 + 2 and views 1 + 3.  The returned Fundamental
	 * matrices will have the following properties: x<sub>i</sub><sup>T</sup>*Fi*x<sub>1</sub> = 0, where i is view 2 or 3.
	 *
	 * @param tensor Trifocal tensor.  Not modified.
	 * @param F2 Output: Fundamental matrix for views 1 and 2. Modified.
	 * @param F3 Output: Fundamental matrix for views 1 and 3. Modified.
	 */
	public static void extractFundamental( TrifocalTensor tensor , DenseMatrix64F F2 , DenseMatrix64F F3 ) {
		// extract the epipoles
		Point3D_F64 e2 = new Point3D_F64();
		Point3D_F64 e3 = new Point3D_F64();

		extractEpipoles(tensor, e2, e3);

		// storage for intermediate results
		Point3D_F64 temp0 = new Point3D_F64();
		Point3D_F64 column = new Point3D_F64();

		// compute the Fundamental matrices one column at a time
		for( int i = 0; i < 3; i++ ) {
			DenseMatrix64F T = tensor.getT(i);

			GeometryMath_F64.mult(T,e3,temp0);
			GeometryMath_F64.cross(e2,temp0,column);

			F2.set(0,i,column.x);
			F2.set(1,i,column.y);
			F2.set(2,i,column.z);

			GeometryMath_F64.multTran(T,e2,temp0);
			GeometryMath_F64.cross(e3,temp0,column);

			F3.set(0,i,column.x);
			F3.set(1,i,column.y);
			F3.set(2,i,column.z);
		}
	}

	/**
	 * Extract the camera matrices up to a common projective transform.  The camera matrix for the
	 * first view is assumed to be P1 = [I|0].
	 *
	 * @param tensor Trifocal tensor.  Not modified.
	 * @param P2 Output: 3x4 camera matrix for views 1 to 2. Modified.
	 * @param P3 Output: 3x4 camera matrix for views 1 to 3. Modified.
	 */
	public static void extractCameraMatrices( TrifocalTensor tensor , DenseMatrix64F P2 , DenseMatrix64F P3 ) {
		// extract the epipoles
		Point3D_F64 e2 = new Point3D_F64();
		Point3D_F64 e3 = new Point3D_F64();

		extractEpipoles(tensor, e2, e3);

		// storage for intermediate results
		Point3D_F64 temp0 = new Point3D_F64();
		Point3D_F64 column = new Point3D_F64();
		// temp1 = [e3*e3^T -I]
		DenseMatrix64F temp1 = new DenseMatrix64F(3,3);
		for( int i = 0; i < 3; i++ ) {
			for( int j = 0; j < 3; j++ ) {
				temp1.set(i,j,e3.getIndex(i)*e3.getIndex(j));
			}
			temp1.set(i,i , temp1.get(i,i) - 1);
		}

		// compute the Fundamental matrices one column at a time
		for( int i = 0; i < 3; i++ ) {
			DenseMatrix64F T = tensor.getT(i);

			GeometryMath_F64.mult(T, e3, column);
			P2.set(0,i,column.x);
			P2.set(1,i,column.y);
			P2.set(2,i,column.z);
			P2.set(i,3,e2.getIndex(i));

			GeometryMath_F64.multTran(T,e2,temp0);
			GeometryMath_F64.mult(temp1, temp0, column);

			P3.set(0,i,column.x);
			P3.set(1,i,column.y);
			P3.set(2,i,column.z);
			P3.set(i,3,e3.getIndex(i));
		}
	}

	/**
	 * <p>
	 * Computes an essential matrix from a rotation and translation.  This motion
	 * is the motion from the first camera frame into the second camera frame.  The essential
	 * matrix 'E' is defined as:<br>
	 * E = hat(T)*R<br>
	 * where hat(T) is the skew symmetric cross product matrix for vector T.
	 * </p>
	 *
	 * @param R Rotation matrix.
	 * @param T Translation vector.
	 * @return Essential matrix
	 */
	public static DenseMatrix64F computeEssential( DenseMatrix64F R , Vector3D_F64 T )
	{
		DenseMatrix64F E = new DenseMatrix64F(3,3);

		DenseMatrix64F T_hat = GeometryMath_F64.crossMatrix(T, null);
		CommonOps.mult(T_hat, R, E);

		return E;
	}

	/**
	 * Computes a Fundamental matrix given an Essential matrix and the camera calibration matrix.
	 *
	 * @param E Essential matrix
	 * @param K Intrinsic camera calibration matirx
	 * @return Fundamental matrix
	 */
	public static DenseMatrix64F computeFundamental( DenseMatrix64F E , DenseMatrix64F K ) {
		DenseMatrix64F K_inv = new DenseMatrix64F(3,3);
		CommonOps.invert(K,K_inv);

		DenseMatrix64F F = new DenseMatrix64F(3,3);
		DenseMatrix64F temp = new DenseMatrix64F(3,3);

		CommonOps.multTransA(K_inv,E,temp);
		CommonOps.mult(temp,K_inv,F);

		return F;
	}

	/**
	 * <p>
	 * Computes a homography matrix from a rotation, translation, plane normal and plane distance:<br>
	 * H = R+(1/d)*T*N<sup>T</sup>
	 * </p>
	 *
	 * @param R Rotation matrix.
	 * @param T Translation vector.
	 * @param d Distance of closest point on plane to camera
	 * @param N Normal of plane
	 * @return Calibrated homography matrix
	 */
	public static DenseMatrix64F computeHomography( DenseMatrix64F R , Vector3D_F64 T ,
													double d , Vector3D_F64 N )
	{
		DenseMatrix64F H = new DenseMatrix64F(3,3);

		GeometryMath_F64.outerProd(T,N,H);
		CommonOps.divide(d,H);
		CommonOps.addEquals(H, R);

		return H;
	}

	/**
	 * <p>
	 * Computes a homography matrix from a rotation, translation, plane normal, plane distance, and
	 * calibration matrix:<br>
	 * H = K*(R+(1/d)*T*N<sup>T</sup>)*K<sup>-1</sup>
	 * </p>
	 *
	 * @param R Rotation matrix.
	 * @param T Translation vector.
	 * @param d Distance of closest point on plane to camera
	 * @param N Normal of plane
	 * @param K Intrinsic calibration matrix
	 * @return Uncalibrated homography matrix
	 */
	public static DenseMatrix64F computeHomography( DenseMatrix64F R , Vector3D_F64 T ,
													double d , Vector3D_F64 N ,
													DenseMatrix64F K )
	{
		DenseMatrix64F temp = new DenseMatrix64F(3,3);
		DenseMatrix64F K_inv = new DenseMatrix64F(3,3);

		DenseMatrix64F H = computeHomography(R,T,d,N);

		// apply calibration matrix to R
		CommonOps.mult(K,H,temp);

		CommonOps.invert(K,K_inv);
		CommonOps.mult(temp,K_inv,H);

		return H;
	}

	/**
	 * <p>
	 * Extracts the epipoles from an essential or fundamental matrix.  The epipoles are extracted
	 * from the left and right null space of the provided matrix.  Note that the found epipoles are
	 * in homogeneous coordinates.  If the epipole is at infinity then z=0
	 * </p>
	 *
	 * <p>
	 * Left: e<sub>2</sub><sup>T</sup>*F = 0 <br>
	 * Right: F*e<sub>1</sub> = 0
	 * </p>
	 *
	 * @param F Fundamental or Essential 3x3 matrix.  Not modified.
	 * @param e1 Output: Right epipole in homogeneous coordinates, Modified.
	 * @param e2 Output: Left epipole in homogeneous coordinates, Modified.
	 */
	public static void extractEpipoles( DenseMatrix64F F , Point3D_F64 e1 , Point3D_F64 e2 ) {
		SimpleMatrix f = SimpleMatrix.wrap(F);
		SimpleSVD svd = f.svd();

		SimpleMatrix U = svd.getU();
		SimpleMatrix V = svd.getV();

		e2.set(U.get(0,2),U.get(1,2),U.get(2,2));
		e1.set(V.get(0,2),V.get(1,2),V.get(2,2));
	}

	/**
	 * <p>
	 * Given a fundamental matrix a pair of projection matrices [R|T] can be extracted.  There are multiple
	 * solutions which can be found, the canonical projection matrix is defined as: <br>
	 * <pre>
	 * P=[I|0] and P'= [M|-M*t] = [[e']*F + e'*v^t | lambda*e']
	 * </pre>
	 * where e' is the epipole F<sup>T</sup>e' = 0, [e'] is the cross product matrix for the enclosed vector,
	 * v is an arbitrary 3-vector and lambda is a non-zero scalar.
	 * </p>
	 *
	 * <p>
	 * Page 256 in R. Hartley, and A. Zisserman, "Multiple View Geometry in Computer Vision", 2nd Ed, Cambridge 2003
	 * </p>
	 *
	 * @see #extractEpipoles
	 *
	 * @param F A fundamental matrix
	 * @param v Arbitrary 3-vector.  Just pick some value, say (1,1,1).
	 * @param lambda A non zero scalar.  Try one.
	 * @param e2 Left epipole of fundamental matrix, F<sup>T</sup>*e2 = 0.
	 * @return The canonical camera matrix P'
	 */
	public static DenseMatrix64F canonicalCamera( DenseMatrix64F F , Point3D_F64 e2, Vector3D_F64 v , double lambda ) {

		DenseMatrix64F crossMatrix = new DenseMatrix64F(3,3);
		GeometryMath_F64.crossMatrix(e2, crossMatrix);

		DenseMatrix64F outer = new DenseMatrix64F(3,3);
		GeometryMath_F64.outerProd(e2,v,outer);

		DenseMatrix64F KR = new DenseMatrix64F(3,3);
		CommonOps.mult(crossMatrix, F, KR);
		CommonOps.add(KR, outer, KR);

		DenseMatrix64F P = new DenseMatrix64F(3,4);
		CommonOps.insert(KR,P,0,0);

		P.set(0,3,lambda*e2.x);
		P.set(1,3,lambda*e2.y);
		P.set(2,3,lambda*e2.z);

		return P;
	}

	/**
	 * <p>
	 * Decomposes a camera matrix P=A*[R|T], where A is an upper triangular camera calibration
	 * matrix, R is a rotation matrix, and T is a translation vector.
	 *
	 * <ul>
	 * <li> NOTE: There are multiple valid solutions to this problem and only one solution is returned.
	 * <li> NOTE: The camera center will be on the plane at infinity.
	 * </ul>
	 * </p>
	 *
	 * @param P Camera matrix, 3 by 4. Input
	 * @param K Camera calibration matrix, 3 by 3.  Output.
	 * @param pose The rotation and translation. Output.
	 */
	public static void decomposeCameraMatrix(DenseMatrix64F P, DenseMatrix64F K, Se3_F64 pose) {
		DenseMatrix64F KR = new DenseMatrix64F(3,3);
		CommonOps.extract(P, 0, 3, 0, 3, KR, 0, 0);

		QRDecomposition<DenseMatrix64F> qr = DecompositionFactory.qr(3, 3);

		if( !CommonOps.invert(KR) )
			throw new RuntimeException("Inverse failed!  Bad input?");

		if( !qr.decompose(KR) )
			throw new RuntimeException("QR decomposition failed!  Bad input?");

		DenseMatrix64F U = qr.getQ(null,false);
		DenseMatrix64F B = qr.getR(null, false);

		if( !CommonOps.invert(U,pose.getR()) )
			throw new RuntimeException("Inverse failed!  Bad input?");

		Point3D_F64 KT = new Point3D_F64(P.get(0,3),P.get(1,3),P.get(2,3));
		GeometryMath_F64.mult(B, KT, pose.getT());

		if( !CommonOps.invert(B,K) )
			throw new RuntimeException("Inverse failed!  Bad input?");

		CommonOps.scale(1.0/K.get(2,2),K);
	}
}