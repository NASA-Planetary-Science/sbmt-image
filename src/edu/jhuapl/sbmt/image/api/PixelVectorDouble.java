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
     * Return the effective value present at the specified index, taking into
     * account whether the value {@link #isValid()} and/or
     * {@link #isInBounds()}. Implementations shall not throw an exception if
     * the index is out of bounds, rather they shall return the result of
     * calling the {@link #getOutOfBoundsValue()} method in that case. Call this
     * method and not {@link #getStoredValue(int)} most of the time.
     *
     * @param index the index
     * @return the current value
     */
    double get(int index);

    /**
     * Return the value stored at the specified index, ignoring whether the
     * value {@link #isValid()} and/or {@link #isInBounds()}. This method exists
     * mainly to make it possible to make an exact duplicate of the pixel.
     * <p>
     * If the specified index is out of bounds in this pixel, i.e., if it is
     * outside the half-open range [0, size() ), this method shall return the
     * result of calling {@link #getOutOfBoundsValue()}.
     *
     * @param index the index
     * @return the current value
     */
    double getStoredValue(int index);

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
