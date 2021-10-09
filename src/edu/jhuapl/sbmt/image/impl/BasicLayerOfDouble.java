package edu.jhuapl.sbmt.image.impl;

import java.util.List;
import java.util.Set;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import edu.jhuapl.sbmt.image.api.Layer;
import edu.jhuapl.sbmt.image.api.Pixel;
import edu.jhuapl.sbmt.image.api.PixelDouble;
import edu.jhuapl.sbmt.image.api.PixelVectorDouble;

/**
 * Abstract base implementation of {@link Layer} that assumes each pixel
 * contains one scalar double value. Supported {@link Pixel} sub-types are
 * {@link PixelDouble} and {@link PixelVectorDouble}.
 *
 * @author James Peachey
 *
 */
public abstract class BasicLayerOfDouble extends BasicLayer
{
    private static final Set<Class<?>> AcceptedPixelTypes = ImmutableSet.of(PixelDouble.class, PixelVectorDouble.class);

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

    @Override
    public List<Integer> dataSizes()
    {
        return ImmutableList.of();
    }

    /**
     * Returns a set containing supported pixel types
     * {@link PixelDouble}<code>.class</code> and
     * {@link PixelVectorDouble}<code>.class</code>.
     */
    @Override
    public Set<Class<?>> getPixelTypes()
    {
        return AcceptedPixelTypes;
    }

    @Override
    protected void get(int i, int j, PixelDouble pd)
    {
        pd.set(doGetDouble(i, j));
    }

    @Override
    protected void get(int i, int j, PixelVectorDouble pvd)
    {
        double value = doGetDouble(i, j);
        double outOfBoundsValue = pvd.getOutOfBoundsValue();

        for (int k = 0; k < pvd.size(); ++k)
        {
            pvd.set(k, value);
            value = outOfBoundsValue;
        }
    }

    protected abstract double doGetDouble(int i, int j);

    @Override
    public String toString()
    {
        return "Layer of double (" + iSize() + ", " + jSize() + ")";
    }

}
