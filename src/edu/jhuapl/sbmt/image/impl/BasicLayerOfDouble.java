package edu.jhuapl.sbmt.image.impl;

import java.util.Set;

import com.google.common.collect.ImmutableSet;

import edu.jhuapl.sbmt.image.api.Layer;
import edu.jhuapl.sbmt.image.api.Pixel;
import edu.jhuapl.sbmt.image.api.PixelDouble;
import edu.jhuapl.sbmt.image.api.PixelVector;

/**
 * Abstract base implementation of {@link Layer} that assumes each pixel
 * contains one scalar double value. Supported {@link Pixel} sub-types are
 * {@link PixelDouble} and {@link PixelVector}.
 *
 * @author James Peachey
 *
 */
public abstract class BasicLayerOfDouble extends BasicLayer
{
    private static final Set<Class<?>> AcceptedPixelTypes = ImmutableSet.of(PixelDouble.class, PixelVector.class);

    /**
     * Constructor that creates a layer using the specified dimensions. The
     * caller must ensure iSize and jSize are non-negative.
     *
     * @param iSize the number of in-bounds values of the I index
     * @param jSize the number of in-bounds values of the J index
     */
    protected BasicLayerOfDouble(int iSize, int jSize)
    {
        super(iSize, jSize);
    }

    /**
     * Returns a set containing <code>{@link PixelDouble}.class</code> and
     * <code>{@link PixelVector}.class</code>. Note that the
     * {@link #get(int, int, Pixel)} implementation will only succeed for vector
     * pixels if their 0-th element is a {@link PixelDouble} instance.
     */
    @Override
    public Set<Class<?>> getPixelTypes()
    {
        return AcceptedPixelTypes;
    }

    @Override
    protected void getElement(int i, int j, int k, Pixel p)
    {
        if (p instanceof PixelDouble pd)
        {
            double value = doGetDouble(i, j);
            pd.set(value);
            pd.setIsValid(isValid(i, j, value));
        }
        else
        {
            throw new IllegalArgumentException();
        }
    }

    protected abstract double doGetDouble(int i, int j);

    protected boolean isValid(int i, int j, double value)
    {
        return true;
    }

    @Override
    public String toString()
    {
        return "Double Layer(" + iSize() + ", " + jSize() + ")";
    }

}
