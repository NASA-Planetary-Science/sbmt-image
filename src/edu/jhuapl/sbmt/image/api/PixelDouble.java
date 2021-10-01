package edu.jhuapl.sbmt.image.api;

/**
 * Extension of {@link Pixel} that holds one mutable scalar double value.
 *
 * @author James Peachey
 *
 */
public interface PixelDouble extends Pixel
{
    /**
     * Return the current effective value of this pixel, taking into account
     * whether it {@link #isInBounds()} and/or {@link #isValid()}. Call this,
     * not {@link #getStoredValue()} most of the time.
     *
     * @return the current effective value of this pixel
     */
    double get();

    /**
     * Return the current stored value of this pixel, not accounting for whether
     * it is {@link #isInBounds()} or {@link #isValid()}. This method exists
     * mainly to make it possible to make an exact duplicate of the pixel.
     *
     * @return
     */
    double getStoredValue();

    /**
     * Set the current value of this pixel.
     *
     * @param value the new value
     */
    void set(double value);

    /**
     * Return the value that should be assigned to this value if it has been
     * determined to be out of bounds.
     *
     * @return the out-of-bounds value
     */
    double getOutOfBoundsValue();

}
