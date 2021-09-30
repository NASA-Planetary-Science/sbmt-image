package edu.jhuapl.sbmt.image.impl;

import edu.jhuapl.sbmt.image.api.Pixel;
import edu.jhuapl.sbmt.image.api.PixelDouble;

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

    protected BasicPixelDouble(double value)
    {
        super();

        this.value = value;
    }

    @Override
    public double get()
    {
        if (!isInBounds())
        {
            return getOutOfBoundsValue();
        }

        return doGet();
    }

    protected double doGet()
    {
        return value;
    }

    @Override
    public void set(double value)
    {
        this.value = value;
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
