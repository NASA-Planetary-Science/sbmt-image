package edu.jhuapl.sbmt.image.api;

public interface PixelVector extends Pixel
{
    /**
     * @return the number of elements associated with this pixel
     */
    int size();

    /**
     * Return a {@link Pixel} instance that gives the properties of the pixel
     * at the specified index in the vector.
     *
     * @param index
     * @return the pixel
     * @throws IndexOutOfBoundsException if the index is outside the half-open range [0, size())
     */
    Pixel get(int index);
}
