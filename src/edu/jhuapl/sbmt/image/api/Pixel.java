package edu.jhuapl.sbmt.image.api;

/**
 * Representation of arbitrary data that has settable properties indicating
 * whether the data should be considered "valid", i.e., suitable to be rendered
 * or used in calculations.
 * <p>
 * This interface is used by the {@link Layer} interface to return information
 * about the data located at a specific (I, J) index pair.
 * <p>
 * Although this interface was designed to work with {@link Layer} instances, it
 * could be used in other contexts.
 *
 * @author James Peachey
 *
 */
public interface Pixel
{
    /**
     * Return a flag that indicates whether the data in this pixel are valid to
     * use when performing operations with this layer.
     * <p>
     * Implementations are free to return false for conditions such as missing,
     * infinite, or special values. Implementations must return false if
     * {@link #isInBounds()} returns false.
     *
     * @return true if the data associated with these indices are valid/usable
     */
    boolean isValid();

    /**
     * Set the flag that indicates whether the data associated with this pixel
     * are valid to use, i.e., for calculations or rendering.
     *
     * @param valid the new value for the flag
     */
    void setIsValid(boolean valid);

    /**
     * Return a flag that indicates whether this pixel is in-bounds (true) or
     * out-of-bounds (false) in its parent {@link Layer}. A pixel that is an
     * element of a higher rank pixel may also be marked out of bounds within
     * that higher rank pixel.
     * <p>
     * The I index is in-bounds if it is in the half-open range [0, iSize() ).
     * The J index is in-bounds if it is in the half-open range [0, jSize() ).
     *
     * @param i the I index
     * @param j the J index
     * @return true if both indices are in bounds, false if either is not
     */
    boolean isInBounds();

    /**
     * Set the flag that indicates whether the data associated with this pixel
     * are in-bounds in their parent collection. Pixels that are not in-bounds
     * are also not valid.
     *
     * @param inBounds the new value for the flag
     */
    void setInBounds(boolean inBounds);

    /**
     * Assign all attributes from the specified source pixel to this one.
     * Implementations may not support assignment from all source pixel types,
     * and may throw {@link IllegalArgumentException} if passed an incompatible
     * source pixel.
     *
     * @param source the source pixel
     */
    void assignFrom(Pixel source);

}
