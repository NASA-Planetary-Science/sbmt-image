package edu.jhuapl.sbmt.image.impl;

import edu.jhuapl.sbmt.image.api.Pixel;

/**
 * Basic implementation of the {@link Pixel} interface that uses simple fields
 * to store the relevant flags. It is not abstract, but is mainly useful as a
 * base class for more specific pixel implementations.
 *
 * @author James Peachey
 *
 */
public class BasicPixel implements Pixel
{

    private volatile boolean isValid;
    private volatile boolean inBounds;

    protected BasicPixel(boolean isValid, boolean inBounds)
    {
        super();

        this.isValid = isValid;
        this.inBounds = inBounds;
    }

    @Override
    public boolean isValid()
    {
        if (!isInBounds())
        {
            return false;
        }

        return isValid;
    }

    @Override
    public void setIsValid(boolean isValid)
    {
        this.isValid = isValid;
    }

    @Override
    public boolean isInBounds()
    {
        return inBounds;
    }

    @Override
    public void setInBounds(boolean inBounds)
    {
        this.inBounds = inBounds;

    }

}
