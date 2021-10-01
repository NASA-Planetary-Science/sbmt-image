package edu.jhuapl.sbmt.image.impl;

import java.util.ArrayList;
import java.util.List;

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

    public PixelVectorDoubleFactory()
    {
        super();
    }

    public PixelVectorDouble of(int size, double outOfBoundsValue, Double invalidValue)
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

                return isValid()|| invalidValue == null ? getStoredValue(index) : invalidValue;
            }

            @Override
            public double getOutOfBoundsValue()
            {
                return outOfBoundsValue;
            }

        };
    }

    public PixelVectorDouble of(PixelVectorDouble pixel)
    {
        Preconditions.checkNotNull(pixel);

        int size = pixel.size();

        List<Double> values = new ArrayList<>(size);
        for (int k = 0; k < size; ++k) {
            values.add(Double.valueOf(pixel.getStoredValue(k)));
        }

        boolean valid = pixel.isValid();
        boolean inBounds = pixel.isInBounds();
        double outOfBoundsValue = pixel.getOutOfBoundsValue();

        pixel = new BasicPixelVectorDouble(size) {

            @Override
            public double getOutOfBoundsValue()
            {
                return outOfBoundsValue;
            }

        };

        for (int k = 0; k < size; ++k) {
            pixel.set(k, values.get(k).doubleValue());
        }
        pixel.setIsValid(valid);
        pixel.setInBounds(inBounds);

        return pixel;
    }

}
