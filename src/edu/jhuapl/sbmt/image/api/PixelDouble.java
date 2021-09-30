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
     * Return the current value of this pixel.
     *
     * @return the current value of this pixel
     */
    double get();

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
