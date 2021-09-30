package edu.jhuapl.sbmt.image.impl;

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

    public PixelDouble of(double value, double outOfBoundsValue)
    {
        return new BasicPixelDouble(value) {

            @Override
            public double getOutOfBoundsValue()
            {
                return outOfBoundsValue;
            }

        };
    }

    public PixelDouble of(double value, double outOfBoundsValue, double invalidValue)
    {
        return new BasicPixelDouble(value) {

            @Override
            public double get()
            {
                if (!isInBounds())
                {
                    return getOutOfBoundsValue();
                }

                return isValid() ? doGet() : invalidValue;
            }

            @Override
            public double getOutOfBoundsValue()
            {
                return outOfBoundsValue;
            }

        };
    }
}
