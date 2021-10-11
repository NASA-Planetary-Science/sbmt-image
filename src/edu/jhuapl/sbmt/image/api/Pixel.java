package edu.jhuapl.sbmt.image.api;

/**
 * Representation of arbitrary data that has settable properties indicating
 * whether the data should be considered "valid", i.e., suitable to be rendered
 * or used in calculations.
 * <p>
 * This interface is used by the {@link Layer} interface to indicate whether
 * data at a particular location are intrinsically "valid" in some
 * implementation-defined sense. For example, "hot" pixels may be flagged as
 * invalid.
 * <p>
 * Another example of invalid pixels are out-of-bounds pixels. Because the
 * {@link Layer} interface is designed ALWAYS to set the values of a
 * {@link Pixel}, even if its index coordinates are not in-bounds for the layer,
 * the {@link Pixel} interface also has a flag indicating whether or not the
 * pixel is in-bounds with respect to its {@link Layer}.
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
     * use when performing operations with this layer. The default
     * implementation simply returns the result of calling
     * {@link #isInBounds()}.
     * <p>
     * When overriding the default behavior, implementations are free to return
     * false for additional conditions, such as missing, infinite, or special
     * values, but the contract of this method requires that all implementations
     * always return false whenever {@link #isInBounds()} returns false.
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
     * out-of-bounds (false) in its parent {@link Layer}.
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

}
