package edu.jhuapl.sbmt.image.impl;

import edu.jhuapl.sbmt.image.api.Pixel;
import edu.jhuapl.sbmt.image.api.PixelDouble;
import edu.jhuapl.sbmt.image.api.PixelVectorDouble;

/**
 * Implementation of {@link PixelVectorDouble} that inherits its general
 * {@link Pixel} functionality from {@link BasicPixel}.
 *
 * @author James Peachey
 *
 */
public abstract class BasicPixelVectorDouble extends BasicPixel implements PixelVectorDouble
{

    private final double[] array;
    private static final PixelDoubleFactory ScalarFactory = new PixelDoubleFactory();

    protected BasicPixelVectorDouble(int size)
    {
        super();
        this.array = new double[size];
    }

    @Override
    public int size()
    {
        return array.length;
    }

    @Override
    public double get(int index)
    {
        // Check if this pixel has been flagged as being out of bounds
        // (presumably by its layer). If so, return the out of-bounds-value.
        if (!isInBounds())
        {
            return getOutOfBoundsValue();
        }

        // Check whether the index argument is out of bounds, in which case
        // also return the out-of-bounds value.
        if (!checkIndex(index, 0, size()))
        {
            return getOutOfBoundsValue();
        }

        return getStoredValue(index);
    }

    @Override
   public double getStoredValue(int index)
    {
        return array[index];
    }

    @Override
    public void set(int index, double value)
    {
        if (checkIndex(index, 0, size()))
        {
            doSet(index, value);
        }
        else
        {
            throw new IndexOutOfBoundsException();
        }
    }

    protected void doSet(int index, double value)
    {
        array[index] = value;
    }

    /**
     * Return a flag that indicates whether the specified index is in the
     * half-open range [minValue, maxValue).
     *
     * @param index the index value to check
     * @param minValid the minimum valid value for the index
     * @param maxValid one-past the maximum valid value for the index
     * @return true if the specified index is valid (in-bounds), false
     *         otherwise.
     */
    protected boolean checkIndex(int index, int minValid, int maxValid)
    {
        return index >= minValid && index < maxValid;
    }

    protected PixelDoubleFactory scalarFactory()
    {
        return ScalarFactory;
    }

    @Override
    public String toString()
    {
        double outOfBoundsValue = getOutOfBoundsValue();

        boolean valid = isValid();
        boolean inBounds = isInBounds();

        StringBuilder builder = new StringBuilder("(");
        String delim = "";
        for (int k = 0; k < size(); ++k)
        {
            builder.append(delim);

            PixelDouble p = scalarFactory().of(get(k), outOfBoundsValue);
            p.setIsValid(valid);
            p.setInBounds(inBounds);

            if (!checkIndex(k, 0, size()))
            {
                p.setInBounds(false);
            }

            builder.append(p);

            delim = ", ";
        }
        builder.append(")");

        return builder.toString();
    }

}
