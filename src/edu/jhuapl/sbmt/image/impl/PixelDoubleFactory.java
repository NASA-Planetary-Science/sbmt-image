package edu.jhuapl.sbmt.image.impl;

import com.google.common.base.Preconditions;

import edu.jhuapl.sbmt.image.api.PixelDouble;

/**
 * Factory for creatng {@link PixelDouble} instances. The base implementation
 * does this by extending {@link BasicPixelDouble}.
 *
 * @author James Peachey
 *
 */
public class PixelDoubleFactory
{

    public PixelDoubleFactory()
    {
        super();
    }

    public PixelDouble of(double value, double outOfBoundsValue, Double invalidValue)
    {
        return new BasicPixelDouble(value) {

            @Override
            public double get()
            {
                if (!isInBounds())
                {
                    return getOutOfBoundsValue();
                }

                return isValid() || invalidValue == null ? getStoredValue() : invalidValue;
            }

            @Override
            public double getOutOfBoundsValue()
            {
                return outOfBoundsValue;
            }

        };
    }

    public PixelDouble of(PixelDouble pixel)
    {
        Preconditions.checkNotNull(pixel);

        double value = pixel.getStoredValue();
        boolean valid = pixel.isValid();
        boolean inBounds = pixel.isInBounds();
        double outOfBoundsValue = pixel.getOutOfBoundsValue();

        pixel = new BasicPixelDouble(value) {

            @Override
            public double getOutOfBoundsValue()
            {
                return outOfBoundsValue;
            }

        };

        pixel.set(value);
        pixel.setIsValid(valid);
        pixel.setInBounds(inBounds);

        return pixel;
    }
}
