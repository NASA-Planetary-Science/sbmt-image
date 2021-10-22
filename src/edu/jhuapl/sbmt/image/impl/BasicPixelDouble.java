package edu.jhuapl.sbmt.image.impl;

import com.google.common.base.Preconditions;

import edu.jhuapl.sbmt.image.api.Pixel;
import edu.jhuapl.sbmt.image.api.PixelDouble;
import edu.jhuapl.sbmt.image.api.PixelVector;

/**
 * Implementation of {@link PixelDouble} that inherits its general {@link Pixel}
 * functionality from {@link BasicPixel}.
 *
 * @author James Peachey
 *
 */
public abstract class BasicPixelDouble extends BasicPixel implements PixelDouble
{
    private volatile double value;

    protected BasicPixelDouble(double value, boolean isValid, boolean inBounds)
    {
        super(isValid, inBounds);

        this.value = value;
    }

    @Override
    public double get()
    {
        if (!isInBounds())
        {
            return getOutOfBoundsValue();
        }

        return getStoredValue();
    }

    @Override
    public double getStoredValue()
    {
        return value;
    }

    @Override
    public void set(double value)
    {
        this.value = value;
    }


    @Override
    public void assignFrom(Pixel source)
    {
        Preconditions.checkNotNull(source);

        boolean isValid = true;
        boolean inBounds = true;
        if (source instanceof PixelVector pv)
        {
            if (pv.size() > 0)
            {
                source = pv.get(0);
            }
            isValid = pv.isValid();
            inBounds = pv.isInBounds();
        }

        if (source instanceof PixelDouble pd)
        {
            set(pd.get());
            setIsValid(isValid && pd.isValid());
            setInBounds(inBounds && pd.isInBounds());
        }
        else
        {
            throw new IllegalArgumentException("Cannot assign to a scalar double pixel from pixel of type " + source.getClass());
        }
    }

    @Override
    public String toString()
    {
        String formattedValue = String.format("%.3g", get());
        String stringFormat = "%9s";
        if (!isInBounds())
        {
            formattedValue = String.format(stringFormat, "(O) " + formattedValue);
        }
        else if (!isValid())
        {
            formattedValue = String.format(stringFormat, "(I) " + formattedValue);
        }
        else
        {
            formattedValue = String.format(stringFormat, formattedValue);
        }

        return formattedValue;
    }

}
