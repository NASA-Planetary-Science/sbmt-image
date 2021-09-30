package edu.jhuapl.sbmt.image.api;

/**
 * Extension of {@link Pixel} that holds a vector. In this context, "vector"
 * means a singly-indexed collection of double values.
 *
 * @author James Peachey
 *
 */
public interface PixelVectorDouble extends Pixel
{

    /**
     * @return the number of elements associated with this pixel
     */
    int size();

    /**
     * Return the element present at the specified index. Implementations shall
     * not throw an exception if the index is out of bounds, rather they shall
     * return the result of calling the {@link #getOutOfBoundsValue()} method in
     * that case.
     *
     * @param index the index
     * @return the current value
     */
    double get(int index);

    /**
     * Set the element present at the specified index to the specified value.
     * Implementations shall throw the appropriate exception if the index is out
     * of bounds for this pixel, that is if the index is not in the half-open
     * range [0, size() ).
     *
     * @param index the index
     * @param value the new value
     * @throws IndexOutOfBoundsException if the index is out of bounds
     */
    void set(int index, double value);

    /**
     * Return the value that the {@link #get(int)} method returns if this pixel
     * is out of bounds in its layer, that is, if {@link #isInBounds()} returns
     * false.
     * <p>
     * This value shall ALSO returned if the argument to {@link #get(int)} is
     * out of bounds within the pixel.
     *
     * @return the out-of-bounds value
     */
    double getOutOfBoundsValue();

}
