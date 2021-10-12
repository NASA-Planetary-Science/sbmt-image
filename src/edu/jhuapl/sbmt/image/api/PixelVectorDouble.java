package edu.jhuapl.sbmt.image.api;

/**
 * Extension of {@link Pixel} that holds a vector. In this context, "vector"
 * means a singly-indexed collection of double pixel values.
 *
 * @author James Peachey
 *
 */
public interface PixelVectorDouble extends PixelVector
{

    /**
     * @return the number of elements associated with this pixel
     */
    @Override
    int size();

    /**
     * Return a {@link PixelDouble} instance that gives the properties of the pixel
     * at the specified index in the vector.
     *
     * @param index
     * @return the pixel
     * @throws IndexOutOfBoundsException if the index is outside the half-open range [0, size())
     */
    @Override
    PixelDouble get(int index);

    /**
     * Return the value that the {@link #getDouble(int)} method returns if this
     * pixel is out of bounds in its layer, that is, if {@link #isInBounds()}
     * returns false.
     * <p>
     * This value shall ALSO returned if the argument to {@link #getDouble(int)} is
     * out of bounds within the pixel.
     *
     * @return the out-of-bounds value
     */
    double getOutOfBoundsValue();

}
