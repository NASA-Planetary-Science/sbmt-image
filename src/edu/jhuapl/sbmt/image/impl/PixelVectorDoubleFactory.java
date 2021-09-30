package edu.jhuapl.sbmt.image.impl;

import com.google.common.base.Preconditions;

import edu.jhuapl.sbmt.image.api.PixelVectorDouble;

/**
 * Factory for creatng {@link PixelVectorDouble} instances. The base
 * implementation does this by extending {@link BasicPixelVectorDouble}.
 *
 * @author James Peachey
 *
 */
public class PixelVectorDoubleFactory
{

    public PixelVectorDouble of(int size, double outOfBoundsValue)
    {
        Preconditions.checkArgument(size >= 0);

        return new BasicPixelVectorDouble(size) {

            @Override
            public double getOutOfBoundsValue()
            {
                return outOfBoundsValue;
            }

        };
    }

    public PixelVectorDouble of(int size, double outOfBoundsValue, double invalidValue)
    {
        Preconditions.checkArgument(size >= 0);

        return new BasicPixelVectorDouble(size) {

            @Override
            public double get(int index)
            {
                // Check if this pixel has been flagged as being out of bounds
                // (presumably by its layer). If so, return the out
                // of-bounds-value.
                if (!isInBounds())
                {
                    return getOutOfBoundsValue();
                }

                // Check whether the index argument is out of bounds, in which
                // case
                // also return the out-of-bounds value.
                if (!checkIndex(index, 0, size()))
                {
                    return getOutOfBoundsValue();
                }

                return isValid() ? doGet(index) : invalidValue;
            }

            @Override
            public double getOutOfBoundsValue()
            {
                return outOfBoundsValue;
            }

        };
    }

}
