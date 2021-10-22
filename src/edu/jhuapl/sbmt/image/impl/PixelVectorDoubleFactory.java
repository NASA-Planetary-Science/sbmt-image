package edu.jhuapl.sbmt.image.impl;

import com.google.common.base.Preconditions;

import edu.jhuapl.sbmt.image.api.PixelVector;

/**
 * Factory for creatng {@link PixelVector} instances. The base
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

    public PixelVector of(int size, double outOfBoundsValue, Double invalidValue)
    {
        Preconditions.checkArgument(size >= 0);

        return new BasicPixelVectorDouble(size, true, true, invalidValue) {

            @Override
            public double getOutOfBoundsValue()
            {
                return outOfBoundsValue;
            }

        };
    }

}
