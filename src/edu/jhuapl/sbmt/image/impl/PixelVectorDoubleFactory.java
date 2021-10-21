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

    public PixelVectorDoubleFactory()
    {
        super();
    }

    public PixelVectorDouble of(int size, double outOfBoundsValue, Double invalidValue)
    {
        Preconditions.checkArgument(size >= 0);

        return new BasicPixelVectorDouble(size, true, true) {

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

        double outOfBoundsValue = pixel.getOutOfBoundsValue();

        if (pixel instanceof BasicPixelVectorDouble basicPixel)
        {
            return new BasicPixelVectorDouble(basicPixel) {

                @Override
                public double getOutOfBoundsValue()
                {
                    return outOfBoundsValue;
                }

            };
        }
        else
        {
            int size = pixel.size();

            BasicPixelVectorDouble newPixel = new BasicPixelVectorDouble(size, pixel.isValid(), pixel.isInBounds()) {

                @Override
                public double getOutOfBoundsValue()
                {
                    return outOfBoundsValue;
                }

            };

            for (int k = 0; k < size; ++k)
            {
                newPixel.get(k).set(pixel.get(k));
            }

            newPixel.setIsValid(pixel.isValid());
            newPixel.setInBounds(pixel.isInBounds());

            return newPixel;
        }

    }

}
