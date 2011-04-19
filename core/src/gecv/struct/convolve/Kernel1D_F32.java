package gecv.struct.convolve;


/**
 * This is a kernel in a 1D convolution.  The kernel's width is the number of elements in it
 * and must be an odd number.  A kernel's radius is defined as the width divided by two.
 * All elements in this kernel are floating point numbers.
 *
 * @author Peter Abeles
 */
public class Kernel1D_F32 {

	public float data[];
	public int width;

	/**
	 * Creates a new kernel whose initial values are specified by data and width.  The length
	 * of its internal data will be width.  Data must be at least as long as width.
	 *
	 * @param data  The value of the kernel. Not modified.  Reference is not saved.
	 * @param width The kernels width.  Must be odd.
	 */
	public Kernel1D_F32(float data[], int width) {
		if (width % 2 == 0 && width <= 0)
			throw new IllegalArgumentException("invalid width");

		this.width = width;

		this.data = new float[width];
		System.arraycopy(data, 0, this.data, 0, width);
	}

	/**
	 * Create a kernel whose elements are all equal to zero.
	 *
	 * @param width How wide the kernel is.  Must be odd.
	 */
	public Kernel1D_F32(int width) {
		if (width % 2 == 0 && width <= 0)
			throw new IllegalArgumentException("invalid width");
		data = new float[width];
		this.width = width;
	}

	/**
	 * Create a kernel with the specified values.
	 *
	 * @param width How wide the kernel is.  Must be odd.
	 * @param value The kernel
	 */
	public Kernel1D_F32(int width, float... value) {
		if (width % 2 == 0 && width <= 0)
			throw new IllegalArgumentException("invalid width");
		data = new float[width];
		this.width = width;
		System.arraycopy(value, 0, data, 0, width);
	}

	protected Kernel1D_F32() {
	}

	/**
	 * Creates a kernel whose elements are the specified data array and has
	 * the specified width.
	 *
	 * @param data  The array who will be the kernel's data.  Reference is saved.
	 * @param width The kernel's width.
	 * @return A new kernel.
	 */
	public static Kernel1D_F32 wrap(float data[], int width) {
		if (width % 2 == 0 && width <= 0 && width > data.length)
			throw new IllegalArgumentException("invalid width");

		Kernel1D_F32 ret = new Kernel1D_F32();
		ret.data = data;
		ret.width = width;

		return ret;
	}

	public float get(int i) {
		return data[i];
	}

	public float[] getData() {
		return data;
	}

	/**
	 * The kernel's width.  This is an odd number.
	 *
	 * @return Kernel's width.
	 */
	public int getWidth() {
		return width;
	}

	/**
	 * The radius is defined as the width divided by two.
	 *
	 * @return The kernel's radius.
	 */
	public int getRadius() {
		return width / 2;
	}

	public void print() {
		for (int i = 0; i < width; i++) {
			System.out.printf("%6.3f ", data[i]);
		}
		System.out.println();
	}
}