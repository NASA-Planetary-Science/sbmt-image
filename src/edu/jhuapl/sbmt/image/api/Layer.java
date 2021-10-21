package edu.jhuapl.sbmt.image.api;

import java.util.List;
import java.util.Set;

/**
 * A collection of arbitrarily complex data whose elements are indexed in two
 * dimensions, I and J. Beyond this 2-index look-up, implementations are free to
 * structure and store their data in any desired form. Callers may obtain the
 * data using an instance of {@link Pixel}.
 *
 * @see Pixel
 *
 * @author James Peachey
 *
 */
public interface Layer
{

    /**
     * Return the size of the layer in the I-th dimension. The size is a
     * one-past-last upper bound on the I index.
     *
     * @return the number of in-bounds values of the I index
     */
    int iSize();

    /**
     * Return the size of the layer in the J-th dimension. The size is a
     * one-past-last upper bound on the J index.
     *
     * @return the number of in-bounds values of the J index
     */
    int jSize();

    /**
     * Return the dimensionality and sizes of data elements located at each pair
     * of indices (I, J). Scalar implementations shall return a list with just
     * one element, equal to the integer 1. Vector implementations shall return
     * a list wieh just one element; the number indicates the maximum size of
     * any vector in the layer. Higher-dimensional data structures shall return
     * a list with one entry for each dimension. No implementation shall return
     * a null list, nor shall any list entry be null or non-positive.
     * <p>
     * Data associated with a particular pair of indices are permitted to have
     * variable size in any dimesion. The sizes in the returned list give the
     * maximum size of data elements in each dimension.
     *
     * @return the size information.
     */
    List<Integer> dataSizes();

    /**
     * Return a flag that indicates whether the data identified by the specified
     * indices are valid to use when performing operations with this layer. The
     * default implementation simply returns the result of calling
     * {@link #isInBounds(int, int)}.
     * <p>
     * When overriding the default behavior, implementations are free to return
     * false for additional conditions, such as missing, infinite, or special
     * values, but the contract of this method requires that all implementations
     * always return false whenever {@link #isInBounds(int, int)} returns false.
     *
     * @param i the I index
     * @param j the J index
     * @return true if the data associated with these indices are valid/usable
     */
    boolean isValid(int i, int j);

    /**
     * Return a flag that indicates whether the specified indices are in-bounds
     * (true) or out-of-bounds (false).
     * <p>
     * The I index is in-bounds if it is in the half-open range [0, iSize() ).
     * The J index is in-bounds if it is in the half-open range [0, jSize() ).
     *
     * @param i the I index
     * @param j the J index
     * @return true if both indices are in bounds, false if either is not
     */
    default boolean isInBounds(int i, int j)
    {
        return i >= 0 && i < iSize() && j >= 0 && j < jSize();
    }

    /**
     * Return a set of the types of sub-interfaces that the
     * {@link #get(int, int, Pixel)} method accepts for the pixel argument.
     * <p>
     * Implementations are free to return an empty set, but they may not return
     * null
     *
     * @return the interface types
     */
    Set<Class<?>> getPixelTypes();

    /**
     * Return a flag that indicates whether this implementation's
     * {@link #get(int, int, Pixel)} method can get data using pixels that
     * implement the specified type.
     *
     * @param pixelType the {@link Class} designating the type to check
     * @return true if this layer's {@link #get(int, int, Pixel)} method accepts
     *         pixels that have the type pixelType
     */
    default boolean isGetAccepts(Class<?> pixelType)
    {
        return getPixelTypes().contains(pixelType);
    }

    /**
     * Retrieve data associated with the specified indices, and use it to set
     * values in the specified {@link Pixel} instance. Implementations shall
     * always attempt to set all the values in the pixel, even if
     * {@link #isValid(int, int)} returns false for any reason.
     * <p>
     * If an implementation cannot or does not know how to set the values of the
     * specified {@link Pixel} instance using its data, it shall throw an
     * UnsupportedOperationException.
     *
     * @param i the I index
     * @param j the J index
     * @param p the pixel, which will be mutated by this method
     * @param throws UnsupportedOperationException if the layer implementation
     *            does not know how to handle the specified instance of the
     *            pixel
     */
    void get(int i, int j, Pixel p);

}
